# This script fetches the latest release tag, increments it, and outputs the new tag as NEW_TAG
LATEST_TAG=$(gh release list --limit 1 --exclude-drafts --exclude-pre-releases | grep -Eo 'v[0-9]+\.[0-9]+' | head -n1)
if [[ -z "$LATEST_TAG" ]]; then
  NEW_TAG="v1.3"
else
  MAJOR=$(echo $LATEST_TAG | cut -d. -f1 | tr -d 'v')
  MINOR=$(echo $LATEST_TAG | cut -d. -f2)
  NEW_MINOR=$((MINOR+1))
  NEW_TAG="v${MAJOR}.${NEW_MINOR}"
fi
echo "NEW_TAG=$NEW_TAG" >> $GITHUB_ENV
