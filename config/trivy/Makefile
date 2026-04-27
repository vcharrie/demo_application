security-scan:
	trivy image \
		--ignorefile .trivyignore \
		--severity HIGH,CRITICAL \
		--exit-code 1 \
		--vuln-type os,library \
		--format json \
		--output trivy-report.json \
		coreservice:latest

convert-csv:
	python trivy_json_to_csv.py trivy-report.json trivy-report.csv

security-full: security-scan convert-csv

# To call script :
# make security-full