#!/usr/bin/env bash
set -euo pipefail

SECRETS_FILE="$(pwd)/.secrets"
if [[ ! -f "$SECRETS_FILE" ]]; then
  echo "$SECRETS_FILE not found. Feel free to copy .secrets.example to .secrets and fill it in" >&2
  exit 1
fi

source "$SECRETS_FILE"

# Check all important secrets
: "${VPS_HOST:?VPS_HOST is not set in .secrets}"
: "${VPS_USER:?VPS_USER is not set in .secrets}"
: "${VPS_KEY:?VPS_KEY is not set in .secrets}"

rsync -avz --delete \
  --exclude='.git' \
  --exclude='backend/target' \
  --exclude='frontend/node_modules' \
  --exclude='frontend/dist' \
  "$(pwd)/" "$VPS_USER@$VPS_HOST:~/Interlude/"

echo "Successfully transfered files :)"

act --secret-file .secrets