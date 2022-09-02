import ast
from _ast import FunctionDef, ClassDef, Call, Subscript, AST


def _function_info(name: str, function_def: FunctionDef, doc: str, class_name: str):
    # the returned object should have the same properties as MethodDocs class in the Java code

    args = ', '.join(arg.arg for arg in function_def.args.args)
    return_type = "object"
    signature = f"{return_type} {name}({args})"
    return {
        "name": name,
        "fullName": signature,  # actually the full signature,
        "description": doc or "",  # the doc
        "returnParams": return_type,  # the return type
        "inputParams": args,
        "ClassName": class_name,
        "PackageName": "",
    }


def get_function_info(function_def: FunctionDef) -> dict:
    return _function_info(function_def.name, function_def, ast.get_docstring(function_def), "")


def get_constructor_info(class_def: ClassDef) -> dict | None:
    init = next((c for c in class_def.body if isinstance(c, FunctionDef) and c.name == '__init__'), None)
    if not init or not isinstance(init, FunctionDef):
        return None

    doc = f"{ast.get_docstring(class_def)}\n\n{ast.get_docstring(init)}"
    return _function_info(class_def.name, init, doc, class_def.name)


def get_function_name(call: Call):
    func = call.func
    if isinstance(func, Call):
        func = func.func
    elif isinstance(func, Subscript):
        func = func.value
    if hasattr(func, "attr"):
        return func.attr
    return func.id
