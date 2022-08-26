import ast
import json
import pathlib
import sys

from path_helper import get_lib_index_file_path


class Analyzer(ast.NodeVisitor):
    def __init__(self):
        self.modules = set()

    def visit_Import(self, node):
        for alias in node.names:
            self.modules.add(alias.name)
        self.generic_visit(node)

    def visit_ImportFrom(self, node):
        self.modules.add(node.module)
        self.generic_visit(node)


if __name__ == '__main__':
    client_code_path, lib_spec = sys.argv[1:3]
    # client_code_path = "Clone/Process/sipa/run.py"
    # top_level_path = ".venv\\Lib\\site-packages\\Flask-2.2.2.dist-info\\top_level.txt"
    client_code_path = client_code_path.replace('\\\\', '/')
    with open(pathlib.Path(client_code_path), encoding="utf8") as code_file:
        code = code_file.read()

    try:
        tree = ast.parse(code)
    except SyntaxError:
        print(False)
        exit(0)
    analyzer = Analyzer()
    analyzer.visit(tree)

    lib_index_path = get_lib_index_file_path(lib_spec)
    with open(lib_index_path) as lib_file:
        lib_info = json.loads(lib_file.read())
        modules = set(lib_info["modules"])

    used = modules.intersection(analyzer.modules);
    print(len(used) > 0)
