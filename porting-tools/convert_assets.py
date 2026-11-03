"""Convert TF2 Stuff 1.12.2 assets to the 1.19.2 resource/data pack layout."""
import json, os, re, shutil, sys

SRC = sys.argv[1]   # .../assets (from the 1.12.2 jar)
OUT = sys.argv[2]   # .../src/main/resources
MOD = "rafradek_tf2_weapons"
A = os.path.join(OUT, "assets", MOD)
D = os.path.join(OUT, "data", MOD)

# Registry names that had uppercase letters in 1.12 (not allowed now).
RENAMES = {"itemTF2": "item_tf2", "robotPart": "robot_part"}

def jload(p):
    with open(p, "rb") as f:
        b = f.read()
    try:
        txt = b.decode("utf-8-sig")
    except UnicodeDecodeError:
        txt = b.decode("cp1252", errors="replace")
    # Gson in 1.12 accepted single-quoted strings; strict JSON does not.
    txt = re.sub(r":\s*'([^'\n]*)'", lambda m: ": " + json.dumps(m.group(1)), txt)
    return json.loads(txt)

def jdump(o, p):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8") as f:
        json.dump(o, f, indent=2, ensure_ascii=False)

def fix_name(s):
    if isinstance(s, str) and s.startswith(MOD + ":"):
        n = s.split(":", 1)[1]
        return MOD + ":" + RENAMES.get(n, n)
    return s

src_mod = os.path.join(SRC, MOD)
shutil.rmtree(os.path.join(OUT, "assets"), ignore_errors=True)
shutil.rmtree(os.path.join(OUT, "data"), ignore_errors=True)

# 1. Straight copies (textures, sounds, models, shaders, weapon data, misc data)
for sub in ["textures", "sounds", "models", "shaders", "weapons", "contracts", "contractsmenu", "robots"]:
    s = os.path.join(src_mod, sub)
    if os.path.isdir(s):
        shutil.copytree(s, os.path.join(A, sub))
shutil.copy(os.path.join(src_mod, "sounds.json"), os.path.join(A, "sounds.json"))
mc = os.path.join(SRC, "minecraft")
if os.path.isdir(mc):
    shutil.copytree(mc, os.path.join(OUT, "assets", "minecraft"))
shutil.rmtree(os.path.join(A, "textures", "items", "New folder"), ignore_errors=True)
for root, _, files in os.walk(A):
    for f in files:
        if not re.fullmatch(r"[a-z0-9_.\-]+", f):
            os.remove(os.path.join(root, f))
            print("removed invalid resource name", os.path.join(root, f))

# 2. Lang .lang -> .json (keys kept; code keeps using the same keys)
for f in os.listdir(os.path.join(src_mod, "lang")):
    out = {}
    with open(os.path.join(src_mod, "lang", f), encoding="utf-8-sig") as fh:
        for line in fh:
            line = line.rstrip("\r\n")
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            out[k.strip()] = v.replace("\\n", "\n")
    jdump(out, os.path.join(A, "lang", f.replace(".lang", ".json").lower()))

# 3. Blockstates: "normal" variant -> "", model paths get block/ prefix
def fix_model_ref(m):
    if ":" in m:
        ns, p = m.split(":", 1)
    else:
        ns, p = "minecraft", m
    if not p.startswith(("block/", "item/")):
        p = "block/" + p
    return ns + ":" + p

def fix_apply(a):
    if isinstance(a, list):
        return [fix_apply(x) for x in a]
    a = dict(a)
    a["model"] = fix_model_ref(a["model"])
    return a

for f in os.listdir(os.path.join(src_mod, "blockstates")):
    bs = jload(os.path.join(src_mod, "blockstates", f))
    if "variants" in bs:
        bs["variants"] = {("" if k == "normal" else k): fix_apply(v) for k, v in bs["variants"].items()}
    if "multipart" in bs:
        for part in bs["multipart"]:
            part["apply"] = fix_apply(part["apply"])
    jdump(bs, os.path.join(A, "blockstates", f))

# 4. Model files: parent references without a folder -> block/
for root, _, files in os.walk(os.path.join(A, "models")):
    for f in files:
        p = os.path.join(root, f)
        if not f.endswith(".json"):
            os.remove(p)
            continue
        m = jload(p)
        if "parent" in m:
            par = m["parent"]
            ns, path = (par.split(":", 1) if ":" in par else ("minecraft", par))
            if not path.startswith(("block/", "item/", "builtin/")):
                path = "block/" + path
            m["parent"] = ns + ":" + path
        jdump(m, p)

# 5. Loot tables -> data pack; metadata becomes the Damage tag (see TF2Util.getMeta)
def fix_loot(o):
    if isinstance(o, dict):
        if o.get("function") == "set_data":
            d = o["data"]
            if isinstance(d, dict) and "type" not in d:
                d = {"type": "minecraft:uniform", **d}
            out = {"function": MOD + ":set_meta", "meta": d}
            if "conditions" in o:
                out["conditions"] = fix_loot(o["conditions"])
            return out
        if o.get("condition") == "random_chance_with_looting":
            o = dict(o)
            o["condition"] = "minecraft:random_chance_with_looting"
        if o.get("condition") == "killed_by_player":
            o = dict(o); o["condition"] = "minecraft:killed_by_player"
        if o.get("condition") == "killed_by_player_team":
            o = dict(o); o["condition"] = MOD + ":killed_by_player_team"
        if o.get("function") in ("set_weapon_class", "set_weapon_upgraded", "random_meta"):
            o = dict(o); o["function"] = MOD + ":" + o["function"]
        if o.get("function") in ("looting_enchant", "set_damage"):
            o = dict(o); o["function"] = "minecraft:" + o["function"]
        if o.get("function") == "set_count":
            o = dict(o); o["function"] = "minecraft:set_count"
        if o.get("function") == "set_nbt":
            o = dict(o); o["function"] = "minecraft:set_nbt"
        if o.get("type") in ("item", "loot_table", "empty"):
            o = dict(o); o["type"] = "minecraft:" + o["type"]
        out = {}
        for k, v in o.items():
            if k == "name" and isinstance(v, str):
                out[k] = fix_name(v)
            elif isinstance(v, dict) and set(v) == {"min", "max"}:
                out[k] = {"type": "minecraft:uniform", **v}
            else:
                out[k] = fix_loot(v)
        return out
    if isinstance(o, list):
        return [fix_loot(x) for x in o]
    return o

for root, _, files in os.walk(os.path.join(src_mod, "loot_tables")):
    for f in files:
        p = os.path.join(root, f)
        rel = os.path.relpath(p, os.path.join(src_mod, "loot_tables"))
        lt = fix_loot(jload(p))
        for pool in lt.get("pools", []):
            pool.pop("name", None)
            # 1.12 used a mixin to turn item_tf2 meta 9/10 drops into random weapons/hats.
            for entry in pool.get("entries", []):
                if entry.get("name") == MOD + ":item_tf2":
                    entry.setdefault("functions", []).append({"function": MOD + ":random_weapon_item"})
        jdump(lt, os.path.join(D, "loot_tables", rel))

# 6. Structures -> data pack (DataFixerUpper upgrades the NBT when loaded)
s = os.path.join(src_mod, "structures")
if os.path.isdir(s):
    shutil.copytree(s, os.path.join(D, "structures"))
print("done")

# 7. Item model override predicates must be lowercase resource locations in 1.19
for root, _, files in os.walk(os.path.join(A, "models", "item")):
    for fn in files:
        if fn.endswith(".json"):
            fp = os.path.join(root, fn)
            t = open(fp, encoding="utf-8").read()
            t2 = t.replace('"bodyModel"', '"bodymodel"').replace('"headModel"', '"headmodel"')
            if t2 != t:
                open(fp, "w", encoding="utf-8").write(t2)
print("predicates lowercased")
