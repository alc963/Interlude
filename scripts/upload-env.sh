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

ENV_FILE="$(pwd)/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "$ENV_FILE not found. Create it based on .env.example" >&2
  exit 1
fi
if [[ ! -s "$ENV_FILE" ]]; then
  echo "$ENV_FILE is empty. Fill it in based on .env.example" >&2
  exit 1
fi

ssh "$VPS_USER@$VPS_HOST" 'mkdir -p ~/Interlude'
rsync -avz "$ENV_FILE" "$VPS_USER@$VPS_HOST:~/Interlude/.env"

echo "Uploaded .env to $VPS_USER@$VPS_HOST:~/Interlude/.env :)"
