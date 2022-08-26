import sys
from pkg_resources import Requirement


def read_requirements(line: str):
    try:
        parsed: Requirement = Requirement.parse(line)
        if parsed.specs:
            return parsed.project_name, parsed.specs[0][0], parsed.specs[0][1]
        return parsed.project_name, "", ""
    except:
        return "", "", ""


if __name__ == '__main__':
    ln = sys.argv[1]
    results = read_requirements(ln)
    for item in results:
        print(item)
