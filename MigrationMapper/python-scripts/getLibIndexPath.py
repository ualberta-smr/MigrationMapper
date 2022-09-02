import sys

import path_helper

if __name__ == '__main__':
    path = path_helper.get_lib_index_file_path(sys.argv[1])
    print(path)
