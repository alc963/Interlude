#!/usr/bin/env bash
export SPOTIFY_REDIRECT_URI=http://127.0.0.1:8082/auth/spotify/callback
export FRONTEND_URL=http://127.0.0.1:8081

docker compose down
docker compose up --build