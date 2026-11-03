"""Capability providers: 1.12 T getCapability / hasCapability -> 1.19 LazyOptional getCapability."""
import os, re, sys
sys.path.insert(0, os.path.dirname(__file__))
from fix4 import find_block_end

ROOT = sys.argv[1]
GET = re.compile(r"(@Override\s*)?public <T> T getCapability\(\s*Capability<T> (\w+),\s*(@Nullable\s+)?(?:net\.minecraft\.core\.)?Direction (\w+)\)\s*\{")
HAS = re.compile(r"(@Override\s*)?public boolean hasCapability\(\s*Capability<\?> (\w+),\s*(@Nullable\s+)?(?:net\.minecraft\.core\.)?Direction (\w+)\)\s*\{")

for root, _, files in os.walk(ROOT):
    for f in files:
        if not f.endswith(".java"):
            continue
        p = os.path.join(root, f)
        s = open(p, encoding="utf-8").read()
        if "public <T> T getCapability(" not in s and "public boolean hasCapability(" not in s:
            continue
        # hasCapability -> plain helper (no override)
        while True:
            m = HAS.search(s)
            if not m:
                break
            s = s[:m.start()] + "public boolean hasCapabilityLegacy(Capability<?> %s, Direction %s) {" % (m.group(2), m.group(4)) + s[m.end():]
        while True:
            m = GET.search(s)
            if not m:
                break
            cap, side = m.group(2), m.group(4)
            brace = m.end() - 1
            end = find_block_end(s, brace)
            body = s[brace:end + 1]
            body = re.sub(r"super\.getCapability\(([^()]*)\)", r"super.getCapability(\1).orElse(null)", body)
            legacy = "public <T> T getCapabilityLegacy(Capability<T> %s, Direction %s) " % (cap, side) + body
            wrapper = ("@Override\n\tpublic <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> %s, @javax.annotation.Nullable Direction %s) {\n"
                       "\t\tT value = this.getCapabilityLegacy(%s, %s);\n"
                       "\t\treturn value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);\n"
                       "\t}\n\n\t") % (cap, side, cap, side)
            s = s[:m.start()] + wrapper + legacy + s[end + 1:]
        s = re.sub(r"super\.hasCapability\(([^()]*)\)", r"super.getCapability(\1).isPresent()", s)
        s = re.sub(r"\bthis\.hasCapability\(", "this.hasCapabilityLegacy(", s)
        open(p, "w", encoding="utf-8").write(s)
print("ok")
