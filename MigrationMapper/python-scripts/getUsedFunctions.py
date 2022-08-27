import ast
import json
import sys
from _ast import Call, Attribute

import path_helper


class FunctionCallVisitor(ast.NodeVisitor):

    def __init__(self, start_line, lines_count, all_lib_functions):
        super(ast.NodeVisitor, self).__init__()
        self.start_line = int(start_line)
        self.lines_count = int(lines_count)
        self.end_line = self.start_line + self.lines_count - 1
        self.all_lib_functions = all_lib_functions
        self.called_lib_functions = set()

    def visit_Call(self, node: Call):
        if self.start_line <= node.lineno <= self.end_line:
            name = self.func_name(node.func)
            if name in self.all_lib_functions:
                self.called_lib_functions.add(name)
        self.generic_visit(node)

    def func_name(self, func):
        if isinstance(func, Call):
            func = func.func
        if isinstance(func, Attribute):
            return func.attr
        return func.id


if __name__ == '__main__':
    lib_spec, source_path, start_line, lines_count = sys.argv[1:5]
    lib_path = path_helper.get_lib_index_file_path(lib_spec)
    with open(lib_path) as file:
        lib_info = json.loads(file.read())
        all_lib_functions = lib_info["functions"]

    try:
        with open(source_path) as source:
            tree = ast.parse(source.read())
    except:
        print("False")
        exit(0)

    visitor = FunctionCallVisitor(start_line, lines_count, all_lib_functions)
    visitor.visit(tree)
    for funcs in visitor.called_lib_functions:
        print(funcs)
