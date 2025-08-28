#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "🧪 Quick Test Suite"

# JVM tests only
echo "Running JVM tests..."
mvn clean test -Dspring.profiles.active=unit

echo "Running JVM integration tests..."
mvn integration-test -Dspring.profiles.active=integration

echo "✅ JVM tests passed!"

# Quick native compatibility check
echo "Running native compatibility tests..."
mvn test -Dtest="**/native_tests/**/*Test" -Dspring.profiles.active=native

echo "✅ Native compatibility tests passed!"
echo "🎉 Quick test suite completed!"