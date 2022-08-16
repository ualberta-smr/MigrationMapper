import sys
from pkg_resources import Requirement

if __name__ == '__main__':
    line = sys.argv[1]
    try:
        parsed: Requirement = Requirement.parse(line)
        print(parsed.project_name)
        print(parsed.specs)
    except:
        print("")
        print("")
