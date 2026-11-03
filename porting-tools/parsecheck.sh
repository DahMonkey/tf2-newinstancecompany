#!/bin/bash
# Report syntax-level javac errors only (symbol errors are expected without Minecraft on the classpath).
cd /home/claude/port/src/main/java
find . -name "*.java" > /tmp/claude-0/files.txt
javac -proc:none -d /tmp/claude-0/jout -Xmaxerrs 100000 @/tmp/claude-0/files.txt 2>&1 | grep "error:" | grep -E "expected|illegal|unclosed|not a statement|orphaned|else without|reached end" | head -20
