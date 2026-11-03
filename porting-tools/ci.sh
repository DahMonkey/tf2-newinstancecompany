#!/bin/bash
# Commit+push current state, wait for CI, save cleaned log to /tmp/claude-0/ci/clean.log, print summary.
cd /home/claude/port
msg="${1:-WIP port}"
git add -A
git commit -qm "$msg

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01HZknQ8MCsiyX5F6w6SviE2" || true
git push -q 2>&1 | grep -v "acknowledgments\|negotiation"
sleep 20
id=$(gh run list -R DahMonkey/tf2-newinstancecompany --limit 1 --json databaseId -q '.[0].databaseId')
gh run watch $id -R DahMonkey/tf2-newinstancecompany >/dev/null 2>&1
mkdir -p /tmp/claude-0/ci
gh run view $id -R DahMonkey/tf2-newinstancecompany --log 2>&1 | sed 's/^build\tBuild\t[^ ]* //' > /tmp/claude-0/ci/clean.log
gh run view $id -R DahMonkey/tf2-newinstancecompany --json conclusion -q .conclusion
grep -E "^[0-9]+ errors?$" /tmp/claude-0/ci/clean.log | tail -1
grep "error:" /tmp/claude-0/ci/clean.log | sed 's/.*src\/main\/java\/rafradek\/TF2weapons\///' | cut -d: -f1 | sort | uniq -c | sort -rn | head -${2:-15}
