import json
import csv

with open("sbom.cdx", "r", encoding="utf-8") as f:
    data = json.load(f)

components = {c["bom-ref"]: c for c in data.get("components", [])}
vulns = []

for vuln in data.get("vulnerabilities", []):
    cve = vuln.get("id")
    severity = vuln.get("ratings", [{}])[0].get("severity")
    description = vuln.get("description")

    for affect in vuln.get("affects", []):
        ref = affect.get("ref")
        comp = components.get(ref, {})

        vulns.append({
            "cve": cve,
            "package": comp.get("name"),
            "installed_version": comp.get("version"),
            "severity": severity,
            "description": description,
            "purl": comp.get("purl"),
            "ref": ref
        })

with open("vulns.csv", "w", newline="", encoding="utf-8") as csvfile:
    writer = csv.DictWriter(csvfile, fieldnames=vulns[0].keys())
    writer.writeheader()
    writer.writerows(vulns)

print("CSV generated: vulns.csv")
