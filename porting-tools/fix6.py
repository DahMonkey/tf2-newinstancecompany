"""HitResult accessors: 1.12 RayTraceResult fields -> 1.19 Entity/BlockHitResult + TF2HitResult helpers."""
import os, re, sys
ROOT = sys.argv[1]
DECL = re.compile(r"\b(?:HitResult|List<HitResult>|ArrayList<HitResult>)\s+(\w+)\s*[=;,):]")
for root, _, files in os.walk(ROOT):
    for f in files:
        if not f.endswith(".java") or f == "TF2HitResult.java":
            continue
        p = os.path.join(root, f)
        s = open(p, encoding="utf-8").read()
        o = s
        names = set(DECL.findall(s))
        names |= set(re.findall(r"for \(HitResult (\w+)\s*:", s))
        for n in names:
            s = re.sub(r"\b%s\.hitInfo\s*=\s*(.+?);" % n, r"TF2HitResult.setHitInfo(%s, \1);" % n, s, flags=re.S)
            s = re.sub(r"\b%s\.hitInfo\b" % n, "TF2HitResult.getHitInfo(%s)" % n, s)
            s = re.sub(r"(?<![\w.])%s\.getEntity\(\)" % n, "TF2HitResult.getEntity(%s)" % n, s)
            s = re.sub(r"(?<![\w.(])%s\.getBlockPos\(\)" % n, "((BlockHitResult) %s).getBlockPos()" % n, s)
            s = re.sub(r"(?<![\w.(])%s\.getDirection\(\)" % n, "((BlockHitResult) %s).getDirection()" % n, s)
        s = re.sub(r"\bType\.(ENTITY|BLOCK|MISS)\b", r"HitResult.Type.\1", s) if "HitResult" in s else s
        s = s.replace("HitResult.HitResult.Type", "HitResult.Type")
        if s != o:
            for imp in ["rafradek.TF2weapons.util.TF2HitResult", "net.minecraft.world.phys.BlockHitResult"]:
                cls = imp.rsplit(".", 1)[1]
                if cls + "." in s or "(" + cls + ")" in s:
                    if "import " + imp + ";" not in s and not s.startswith("package " + imp.rsplit(".", 1)[0] + ";"):
                        s = s.replace("\nimport ", "\nimport " + imp + ";\nimport ", 1)
            open(p, "w", encoding="utf-8").write(s)
print("ok")
