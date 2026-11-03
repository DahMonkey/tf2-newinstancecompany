"""Add imports for port-shim classes (and any extra FQNs) to every file that uses them."""
import os, re, sys
ROOT = sys.argv[1]
SHIMS = {
    "IMessage": "rafradek.TF2weapons.message.IMessage",
    "IMessageHandler": "rafradek.TF2weapons.message.IMessageHandler",
    "MessageContext": "rafradek.TF2weapons.message.MessageContext",
    "TF2NetworkWrapper": "rafradek.TF2weapons.message.TF2NetworkWrapper",
    "TargetPoint": "rafradek.TF2weapons.message.TF2NetworkWrapper.TargetPoint",
    "ItemMeshDefinition": "rafradek.TF2weapons.client.ItemMeshDefinition",
    "TF2Util": "rafradek.TF2weapons.util.TF2Util",
    "ByteBuf": "io.netty.buffer.ByteBuf",
    "FriendlyByteBuf": "net.minecraft.network.FriendlyByteBuf",
}
SHIMS.update(dict(a.split("=") for a in sys.argv[2:]))
def strip(src):
    return re.sub(r'"(?:\\.|[^"\\\n])*"|//[^\n]*|/\*.*?\*/', "", src, flags=re.S)
for root, _, files in os.walk(ROOT):
    for f in files:
        if not f.endswith(".java"): continue
        p = os.path.join(root, f)
        src = open(p, encoding="utf-8").read()
        pkg = re.search(r"^package ([\w.]+);", src, re.M).group(1)
        code = strip(src)
        body = re.sub(r"^import .*$", "", code, flags=re.M)
        add = []
        for name, fqn in SHIMS.items():
            if fqn.rsplit(".", 1)[0] == pkg or f[:-5] == name: continue
            if re.search(r"(?<![\w.])" + name + r"\b", body) and not re.search(r"^import [\w.]*\b" + name + ";", src, re.M) \
               and not re.search(r"^import " + re.escape(fqn.rsplit(".", 1)[0]) + r"\.\*;", src, re.M):
                add.append("import %s;" % fqn)
        if add:
            src = re.sub(r"^(package [\w.]+;\n)", lambda m: m.group(1) + "\n" + "\n".join(add) + "\n", src, count=1, flags=re.M)
            open(p, "w", encoding="utf-8").write(src)
