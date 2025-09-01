#!/bin/bash
set -e  # exit immediately on error

# Ask for Docker image name
read -p "Enter Docker image name (e.g. myrepo/chrongo:1.93): " IMAGE_NAME

echo ">>> Running unit tests..."
mvn -q test -Dtest="**/tests/**/*Test" | tail -n 1

echo ">>> Running integration tests..."
mvn -q test -Dtest="**/integration/**/*Test" | tail -n 1

echo ">>> Building Docker image: $IMAGE_NAME"
docker build -t "$IMAGE_NAME" .

echo "✅ Done! Image built: $IMAGE_NAME"
