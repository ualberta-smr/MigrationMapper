import sys
from pathlib import Path

import readRequirement

exe_dir = Path(Path(sys.executable).parent)  # the .venv/Scripts dir
out_dir = exe_dir.parent.parent.joinpath("librariesClasses/py")
pip_cmd = exe_dir.joinpath("pip.exe")
wheel_cmd = exe_dir.joinpath("wheel")
lib_def_dir = out_dir.joinpath("lib_defs")
lib_code_dir = out_dir.joinpath("lib_code")
lib_def_dir.mkdir(exist_ok=True, parents=True)
lib_code_dir.mkdir(exist_ok=True, parents=True)


def lib_file_name(lib_spec: str):
    spec = readRequirement.read_requirements(lib_spec)
    return f"{spec[0]}{spec[1].replace('>', ')').replace('<', '(')}{spec[2]}.json"


def get_lib_index_file_path(lib_spec: str):
    return lib_def_dir.joinpath(lib_file_name(lib_spec))


def lib_index_exists(lib_spec: str):
    return get_lib_index_file_path(lib_spec).exists()
