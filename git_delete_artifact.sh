gh api repos/vcharrie/demo_application/actions/artifacts \
  --jq '.artifacts[].id' | \
while read -r ART_ID; do
  echo "Deleting artifact $ART_ID"
  gh api --method DELETE repos/vcharrie/demo_application/actions/artifacts/$ART_ID
done
