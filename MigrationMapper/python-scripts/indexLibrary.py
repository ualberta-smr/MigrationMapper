import ast
import json
import os
import subprocess
import sys
import tarfile
from _ast import FunctionDef, ClassDef
from pathlib import Path
from typing import Any
from zipfile import ZipFile

import ast_helper
import path_helper
from path_helper import pip_cmd, out_dir, get_lib_index_file_path


def extract_path(text: str):
    # https://stackoverflow.com/a/36702619/887149, modified for us
    all_paths = [x for x in text.split() if len(x.split(os.sep)) > 1]
    if len(all_paths) == 1:
        path = all_paths[0].rstrip("...OK")
        return Path(path)
    return None


def download(lib_spec: str):
    # pip.main(['download', sys.argv[1], "-d", "librariesClasses/py"])
    # pip.main(['install', sys.argv[1], "--target", "librariesClasses/py", "--no-deps"])
    if path_helper.lib_index_exists(lib_spec):
        return True

    archive_path = download_archive(lib_spec)

    if archive_path is None:
        return False

    lib_dir = extract_archive(archive_path)
    generate_library_index(lib_dir, lib_spec)

    return True


def generate_library_index(lib_folder: Path, lib_spec: str):
    visitor = LibCodeVisitor()
    root_module_paths: [Path] = []
    modules = []
    errors = []
    for current_dir, dirs, files in os.walk(lib_folder):
        current_dir_path = Path(current_dir)
        for file in [f for f in files if f.endswith(".py")]:
            code_file_path = current_dir_path.joinpath(file)
            if file == "__init__.py":
                root_mp = [rmp for rmp in root_module_paths if rmp in current_dir_path.parents]
                if root_mp:
                    module = current_dir_path.relative_to(root_mp[0].parent)
                    modules.append(str(module).replace(os.sep, "."))
                else:
                    root_module_paths.append(current_dir_path)
                    modules.append(current_dir_path.name)

            with open(code_file_path, encoding="utf8") as code_file:
                relative_code_path = code_file_path.relative_to(lib_folder)
                try:
                    code = code_file.read()
                    tree = ast.parse(code, filename=str(code_file_path))
                    visitor.start_visit(tree, relative_code_path)
                except SyntaxError as e:
                    errors.append(
                        f"{e.args[0]}. {relative_code_path}, line {e.lineno}-{e.end_lineno}, col {e.offset}-{e.end_offset}")
                except UnicodeDecodeError as e:
                    errors.append(str(e))

    output_path = get_lib_index_file_path(lib_spec)

    results = {"modules": modules, "functions": visitor.functions, "errors": errors}
    with open(output_path, "w") as out_file:
        j_string = json.dumps(results, indent=2, sort_keys=True)
        out_file.writelines(j_string)

    return output_path


def download_archive(lib_spec):
    result = subprocess.run([pip_cmd, 'download', lib_spec, "-d", path_helper.lib_code_dir, "--no-deps"],
                            stdout=subprocess.PIPE).stdout.decode("utf-8")
    archive_path = extract_path(result)
    return archive_path


def extract_archive(archive_path: Path):
    extracted_path = path_helper.lib_code_dir.joinpath(archive_path.stem)
    if archive_path.suffix == ".gz":
        with tarfile.open(archive_path, "r:gz") as tar:
            tar.extractall(path=extracted_path)
    elif archive_path.suffix == ".whl":
        with ZipFile(archive_path, "r") as zip:
            zip.extractall(extracted_path)

    return extracted_path


class LibCodeVisitor(ast.NodeVisitor):
    functions = {}
    current_file = None

    def start_visit(self, tree, file: Path):
        self.current_file = file
        self.visit(tree)

    def visit_FunctionDef(self, func: FunctionDef):
        if func.name not in self.functions:
            self.process_function(func)
        else:
            pass
        self.generic_visit(func)

    def visit_ClassDef(self, cls: ClassDef) -> Any:
        for child in cls.body:
            if isinstance(child, FunctionDef):
                self.process_function(child, cls)
        self.generic_visit(cls)

    def process_function(self, func: FunctionDef, cls: ClassDef = None):
        info = ast_helper.get_function_info(self.current_file, func, cls)
        if info:
            self.functions[info["name"]] = info


if __name__ == '__main__':
    sys.argv.append("flask")
    library_spec = sys.argv[1]
    out = download(library_spec)
    print(out)
