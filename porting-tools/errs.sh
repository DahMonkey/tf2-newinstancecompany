#!/bin/bash
# errs.sh <relative file under TF2weapons/> : print compile errors for that file (line: message + symbol)
f="$1"
awk -v f="$f" '
/error:/ { show = index($0, "TF2weapons/" f ":") > 0; if (show) { sub(/.*TF2weapons\//, ""); line=$0 } }
show && /symbol:|location:/ { line = line " | " $0 }
show && /^$/ {}
show && /error:/ { if (prev) print prev; prev=line; next }
show { if ($0 ~ /symbol:/) prev = prev " {" $0 "}" }
END { if (prev) print prev }
' /tmp/claude-0/ci/clean.log | sed 's/ \+/ /g' | sort -t: -k2 -n -u
