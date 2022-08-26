import ast
import json
import os
import subprocess
import sys
import tarfile
from _ast import FunctionDef
from pathlib import Path
from zipfile import ZipFile

import path_helper
from path_helper import pip_cmd, out_dir, exe_dir, get_lib_index_file_path


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
    root_module_path: Path = None
    modules = []
    for current_dir, dirs, files in os.walk(lib_folder):
        current_dir_path = Path(current_dir)
        for file in [f for f in files if f.endswith(".py")]:
            code_file_path = current_dir_path.joinpath(file)
            if file == "__init__.py":
                if root_module_path:
                    module = current_dir_path.relative_to(root_module_path.parent)
                    modules.append(str(module).replace(os.sep, "."))
                else:
                    root_module_path = current_dir_path
                    modules.append(current_dir_path.name)

            with open(code_file_path, encoding="utf8") as code_file:
                code = code_file.read()
                try:
                    tree = ast.parse(code, filename=str(code_file_path))
                    visitor.visit(tree)
                except SyntaxError as e:
                    print(f"could not parse {code_file_path}", file=sys.stderr)

    output_path = get_lib_index_file_path(lib_spec)

    results = {"modules": sorted(modules), "functions": sorted(visitor.functions)}
    with open(output_path, "w") as out_file:
        out_file.writelines(json.dumps(results, indent=2))

    return output_path


def download_archive(lib_spec):
    result = subprocess.run([pip_cmd, 'download', lib_spec, "-d", out_dir, "--no-deps"],
                            stdout=subprocess.PIPE).stdout.decode("utf-8")
    archive_path = extract_path(result)
    return archive_path


def extract_archive(archive_path: Path):
    extracted_path = out_dir.joinpath(archive_path.stem)
    if archive_path.suffix == ".gz":
        with tarfile.open(archive_path, "r:gz") as tar:
            tar.extractall(path=extracted_path)
    elif archive_path.suffix == ".whl":
        with ZipFile(archive_path, "r") as zip:
            zip.extractall(extracted_path)

    return extracted_path


class LibCodeVisitor(ast.NodeVisitor):
    skip_prefix = ("_", "test_", "setup.py")
    functions = set()

    def visit_FunctionDef(self, node: FunctionDef):
        func_name = node.name
        if not func_name.startswith(self.skip_prefix):
            self.functions.add(func_name)
        self.generic_visit(node)


if __name__ == '__main__':
    library_spec = sys.argv[1]
    # library_spec = "flask-restful == 0.3.7"
    # library_spec = "pycrypto"
    out = download(library_spec)
    print(out)
