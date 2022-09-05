import csv
from pathlib import Path


def group(processed_rows: list[list]):
    groups: dict[str, list] = {}

    for row in processed_rows:
        key = '___'.join(row)
        if key not in groups:
            groups[key] = row + [0]
        groups[key][-1] += 1

    return list(groups.values())


def main():
    expanded_rows = []
    with open(Path("../data/api_mapping_pymigbench_raw.csv"), encoding="utf8") as raw_file:
        raw_rows = csv.reader(raw_file)
        next(raw_rows)  # skip header
        for rr in raw_rows:
            sl = rr[0]
            tl = rr[1]
            s_api = rr[2].split('|')
            t_api = rr[3].split('|')
            if len(s_api) != len(t_api):
                raise Exception(f"Invalid row: {rr}")

            for i in range(len(s_api)):
                row = [sl, tl, s_api[i].replace(' ', ''), t_api[i].replace(' ', '')]
                expanded_rows.append(row)

    grouped_rows = [["source lib", "target lib", "source API", "target API", "frequency"]] + group(expanded_rows)
    processed_path = Path("../data/api_mapping_pymigbench_processed.csv")
    with open(processed_path, mode="w", encoding="utf8", newline="") as processed_file:
        csv.writer(processed_file).writerows(grouped_rows)


if __name__ == '__main__':
    main()
