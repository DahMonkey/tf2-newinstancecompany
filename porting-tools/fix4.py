"""Item API pass: LegacyItem base, tooltips as Components, renamed item hooks. In place, idempotent."""
import os, re, sys

ROOT = sys.argv[1]

def find_close(s, i):
    depth = 1
    while i < len(s):
        c = s[i]
        if c == '"':
            i += 1
            while s[i] != '"':
                if s[i] == "\\":
                    i += 1
                i += 1
        elif c == "'":
            i += 1
            while s[i] != "'":
                if s[i] == "\\":
                    i += 1
                i += 1
        elif c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1

def find_block_end(s, i):
    """i at '{'; return index of matching '}'."""
    depth = 0
    while i < len(s):
        c = s[i]
        if c == '"':
            i += 1
            while s[i] != '"':
                if s[i] == "\\":
                    i += 1
                i += 1
        elif c == "'":
            i += 1
            while s[i] != "'":
                if s[i] == "\\":
                    i += 1
                i += 1
        elif s.startswith("//", i):
            i = s.index("\n", i)
        elif s.startswith("/*", i):
            i = s.index("*/", i) + 1
        elif c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1

def split_args(a):
    out, depth, cur, q = [], 0, "", None
    for c in a:
        if q:
            cur += c
            if c == q:
                q = None
            continue
        if c in "\"'":
            q = c
        if c in "([{":
            depth += 1
        if c in ")]}":
            depth -= 1
        if c == "," and depth == 0:
            out.append(cur); cur = ""
        else:
            cur += c
    out.append(cur)
    return out

def wrap_adds(body, name):
    out, i = "", 0
    key = name + ".add("
    while True:
        j = body.find(key, i)
        if j < 0:
            out += body[i:]; break
        k = find_close(body, j + len(key))
        args = split_args(body[j + len(key):k])
        last = args[-1].strip()
        if not last.startswith("Component."):
            args[-1] = (" " if len(args) > 1 else "") + "Component.literal(" + last + ")"
        out += body[i:j] + key + ",".join(args) + ")"
        i = k + 1
    return out

def fix_tooltips(s):
    pat = re.compile(r"appendHoverText\(([^)]*?)List<String> (\w+)")
    out, i = "", 0
    for m in pat.finditer(s):
        pass
    while True:
        m = pat.search(s, i)
        if not m:
            out += s[i:]; break
        name = m.group(2)
        brace = s.index("{", m.end())
        end = find_block_end(s, brace)
        header = s[i:m.start()] + "appendHoverText(" + m.group(1) + "List<Component> " + name + s[m.end():brace]
        body = wrap_adds(s[brace:end + 1], name)
        out += header + body
        i = end + 1
    return out

def fix_get_name(s):
    """String getName(ItemStack) -> Component getName(ItemStack), wrapping returns."""
    pat = re.compile(r"public String getName\(ItemStack (\w+)\)\s*\{")
    while True:
        m = pat.search(s)
        if not m:
            return s
        brace = m.end() - 1
        end = find_block_end(s, brace)
        body = s[brace:end + 1]
        def ret(mm):
            expr = mm.group(1)
            return "return Component.literal(%s);" % expr
        body = re.sub(r"return ([^;]+);", ret, body)
        body = body.replace("Component.literal(super.getName(", "Component.literal(super.getName(").replace(
            "super.getName(%s))" % m.group(1), "super.getName(%s).getString())" % m.group(1))
        s = s[:m.start()] + "public Component getName(ItemStack %s) {" % m.group(1) + body[1:] + s[end + 1:]

for root, _, files in os.walk(ROOT):
    if "compat" in root:
        continue
    for f in files:
        if not f.endswith(".java") or f == "LegacyItem.java":
            continue
        p = os.path.join(root, f)
        s = open(p, encoding="utf-8").read()
        o = s
        if "/item/" in p.replace("\\", "/") or "Item" in f:
            s = re.sub(r"(class \w+(?:<[^>]*>)? extends )Item(\s*(?:implements|\{))", r"\1LegacyItem\2", s)
        s = fix_tooltips(s)
        s = fix_get_name(s)
        s = re.sub(r"public void onUpdate\(ItemStack ", "public void inventoryTick(ItemStack ", s)
        s = re.sub(r"onEntitySwing\(LivingEntity (\w+), ItemStack (\w+)\)", r"onEntitySwing(ItemStack \2, LivingEntity \1)", s)
        s = re.sub(r"public boolean hasEffect\(ItemStack ", "public boolean isFoil(ItemStack ", s)
        s = re.sub(r"public int getItemStackLimit\(ItemStack ", "public int getMaxStackSize(ItemStack ", s)
        s = re.sub(r"public int getItemEnchantability\(\)", "public int getEnchantmentValue()", s)
        s = re.sub(r"\.isInCreativeTab\(", ".allowedIn(", s) if "extends LegacyItem" not in s and "extends ItemFromData" not in s else s
        if s != o:
            if "Component" in s and "import net.minecraft.network.chat.Component;" not in s:
                s = s.replace("\nimport ", "\nimport net.minecraft.network.chat.Component;\nimport ", 1)
            open(p, "w", encoding="utf-8").write(s)
print("ok")
