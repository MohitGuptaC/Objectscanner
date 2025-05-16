#!/bin/bash
# Find the highest vX.Y tag, increment minor, or exit if none exist

LATEST_TAG=$(git tag --list 'v[0-9]*.[0-9]*' --sort=-v:refname | head -n1)

if [[ -z "$LATEST_TAG" ]]; then
  echo "No version tags found (vX.Y). Cannot create a new release tag."
  exit 1
else
  MAJOR=$(echo $LATEST_TAG | cut -d. -f1 | tr -d 'v')
  MINOR=$(echo $LATEST_TAG | cut -d. -f2)
  NEW_MINOR=$((MINOR+1))
  NEW_TAG="v${MAJOR}.${NEW_MINOR}"
  echo "NEW_TAG=$NEW_TAG" >> $GITHUB_ENV
fi