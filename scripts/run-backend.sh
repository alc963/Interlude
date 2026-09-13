#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd -- "$script_dir/.." && pwd)"
env_file="$project_dir/.env"

if [[ ! -f "$env_file" ]]; then
    printf 'Missing %s. Copy .env.example to .env and set the database values.\n' "$env_file" >&2
    exit 1
fi

cd "$project_dir"
docker compose up -d postgres

export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5432}"

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

cd "$project_dir/backend"
exec ./mvnw spring-boot:run