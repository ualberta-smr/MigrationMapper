import ast
from _ast import FunctionDef, ClassDef, Call, Subscript
from pathlib import Path

skip_prefix = ("_", "test_")


def get_function_info(current_file: Path, function_def: FunctionDef, class_def: ClassDef = None) -> dict | None:
    actual_func_name = function_def.name

    if class_def and actual_func_name == "__init__":
        applied_func_name = class_def.name
        doc = f"{ast.get_docstring(class_def)}\n\n{ast.get_docstring(function_def)}"
    elif not actual_func_name.startswith(skip_prefix):
        applied_func_name = actual_func_name
        doc = ast.get_docstring(function_def)
    else:
        return None

    class_name = class_def.name if class_def else ""

    # the returned object should have the same properties as MethodDocs class in the Java code
    args = ', '.join(arg.arg for arg in function_def.args.args)
    return_type = "object"
    signature = f"{return_type} {applied_func_name}({args})"
    location = f"{current_file.as_posix()}:{function_def.lineno}"
    return {
        "name": applied_func_name,
        "fullName": signature,  # actually the full signature,
        "description": doc or "",  # the doc
        "returnParams": return_type,  # the return type
        "inputParams": args,
        "ClassName": class_name,
        "PackageName": "",
        "location": location
    }


def get_function_name(call: Call):
    func = call.func
    if isinstance(func, Call):
        func = func.func
    elif isinstance(func, Subscript):
        func = func.value
    if hasattr(func, "attr"):
        return func.attr
    return func.id
