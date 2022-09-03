import csv
from pathlib import Path

if __name__ == '__main__':
    with open(Path("../data/lm.csv"), encoding="utf8") as file:
        candidate_migrations = csv.reader(file)
        valid_pairs = Path("../data/validPairs.csv").read_text("utf8").splitlines(keepends=False)
        valid_pairs = set(valid_pairs)
        repositories = set()
        for cells in candidate_migrations:
            if cells[11].strip() in valid_pairs:
                repositories.add("https://github.com/" + cells[0].strip())

    print(*sorted(repositories), sep="\n")
