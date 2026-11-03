#!/bin/bash
# Wait for the newest smoke test run and save its log to /tmp/claude-0/ci/smoke.log
sleep 15
id=$(gh run list -R DahMonkey/tf2-newinstancecompany --workflow "Smoke test" --limit 1 --json databaseId -q '.[0].databaseId')
gh run watch $id -R DahMonkey/tf2-newinstancecompany >/dev/null 2>&1
mkdir -p /tmp/claude-0/ci
gh run view $id -R DahMonkey/tf2-newinstancecompany --log 2>&1 | sed 's/^smoke\t[^\t]*\t[^ ]* //' > /tmp/claude-0/ci/smoke.log
grep "SMOKE RESULT" /tmp/claude-0/ci/smoke.log
