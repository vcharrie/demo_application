import json
import csv

INPUT = "trivy-report.json"
OUTPUT = "vulnerabilities.csv"

def extract_vulns(result):
    rows = []
    for target in result.get("Results", []):
        target_name = target.get("Target", "unknown")
        for vuln in target.get("Vulnerabilities", []):
            rows.append({
                "target": target_name,
                "type": vuln.get("PkgType", ""),
                "package": vuln.get("PkgName", ""),
                "installed_version": vuln.get("InstalledVersion", ""),
                "fixed_version": vuln.get("FixedVersion", ""),
                "cve": vuln.get("VulnerabilityID", ""),
                "severity": vuln.get("Severity", ""),
                "title": vuln.get("Title", ""),
            })
    return rows

def main():
    with open(INPUT, "r", encoding="utf-8") as f:
        data = json.load(f)

    rows = extract_vulns(data)

    with open(OUTPUT, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=[
            "target",
            "type",
            "package",
            "installed_version",
            "fixed_version",
            "cve",
            "severity",
            "title"
        ])
        writer.writeheader()
        writer.writerows(rows)

    print(f"CSV generated: {OUTPUT}")

if __name__ == "__main__":
    main()
