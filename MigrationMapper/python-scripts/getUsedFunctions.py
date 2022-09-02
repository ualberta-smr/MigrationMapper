import ast
import json
import os
import sys
from _ast import Call, Attribute, Subscript

import ast_helper
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
            name = ast_helper.get_function_name(node)
            if name in self.all_lib_functions:
                func_info = self.all_lib_functions[name]
                self.called_lib_functions.add(func_info["signature"])
        self.generic_visit(node)


if __name__ == '__main__':

    sys.argv += [
        "raven==6.10.0",
        "../Clone/sipa/sipa/initialization.py",
        "1",
        "1000"
    ]

    lib_spec, source_path, start_line, lines_count = sys.argv[1:5]
    lib_path = path_helper.get_lib_index_file_path(lib_spec)
    with open(lib_path) as file:
        lib_info = json.loads(file.read())
        all_lib_functions = lib_info["functions"]

    try:
        with open(source_path) as source:
            tree = ast.parse(source.read())
        visitor = FunctionCallVisitor(start_line, lines_count, all_lib_functions)
        visitor.visit(tree)
        for funcs in sorted(visitor.called_lib_functions):
            print(funcs)
    except:
        pass
        # don't print anything
