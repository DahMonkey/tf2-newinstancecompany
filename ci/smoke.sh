#!/bin/bash
# Usage: ci/smoke.sh <gradle task> <success regex> <seconds to keep running after success> <timeout seconds>
# Runs the game, waits for it to load, then reports crashes and mod errors from the log.
TASK=$1
SUCCESS=$2
SETTLE=$3
TIMEOUT=$4
LOG=smoke-$TASK.log
./gradlew $TASK --no-daemon > "$LOG" 2>&1 &
PID=$!
start=$(date +%s)
result=timeout
while kill -0 $PID 2>/dev/null; do
  sleep 5
  if grep -q -- "---- Minecraft Crash Report ----\|Failed to load mod\|LoadingFailedException\|ModLoadingException\|Failed to create mod instance\|has failed to load correctly" "$LOG"; then
    result=crash; break
  fi
  if grep -Eq "$SUCCESS" "$LOG"; then
    sleep "$SETTLE"; result=loaded; break
  fi
  if [ $(( $(date +%s) - start )) -gt "$TIMEOUT" ]; then break; fi
done
if ! kill -0 $PID 2>/dev/null && [ "$result" = timeout ]; then result=exited; fi
if grep -q -- "---- Minecraft Crash Report ----\|Failed to create mod instance\|has failed to load correctly\|Exception caught during firing event" "$LOG"; then result=crash; fi
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; sleep 3; pkill -f GradleWorker 2>/dev/null; pkill -f 'net.minecraftforge.bootstrap\|cpw.mods' 2>/dev/null
echo "SMOKE RESULT ($TASK): $result"
echo "==== errors mentioning the mod ===="
grep -n -B2 -A12 "Exception\|Error:" "$LOG" | grep -v "^--$" | head -400
echo "==== crash reports ===="
for f in run/crash-reports/*.txt; do [ -f "$f" ] && head -120 "$f"; done
[ "$result" = loaded ]
