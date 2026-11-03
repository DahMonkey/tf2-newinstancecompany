"""Second mechanical pass, applied in place to the port tree. Safe to re-run."""
import os, re, sys

ROOT = sys.argv[1]

R = [
    (r"\brand\b", "random"),
    (r"\.getTrueSource\(\)", ".getEntity()"), (r"\.getImmediateSource\(\)", ".getDirectEntity()"),
    (r"\bgetEntityLiving\(\)", "getEntity()"), (r"\bgetEntityPlayer\(\)", "getEntity()"),
    (r"\.entityDropItem\(", ".spawnAtLocation("), (r"\bentityDropItem\(", "spawnAtLocation("),
    (r"\.up\(\)", ".above()"), (r"\.down\(\)", ".below()"), (r"\.up\((?=[^)])", ".above("), (r"\.down\((?=[^)])", ".below("),
    (r"\bthis\.setMutexBits\(", "this.setFlags(TF2Util.mutexFlags("), (r"\bsetMutexBits\(", "setFlags(TF2Util.mutexFlags("),
    (r"\btaskOwner\b", "mob"),
    (r"\.getWorld\(\)", ".getLevel()"), (r"\bgetWorld\(\)", "getLevel()"),
    (r"\bexperienceValue\b", "xpReward"), (r"\bisAirBorne\b", "hasImpulse"), (r"\bstepHeight\b", "maxUpStep"),
    (r"\.setNoAI\(", ".setNoAi("), (r"\bsetNoAI\(", "setNoAi("), (r"\.getEntitySenses\(\)", ".getSensing()"),
    (r"\bgetEntitySenses\(\)", "getSensing()"), (r"\.clearPath\(\)", ".stop()"), (r"\.noPath\(\)", ".isDone()"),
    (r"\.getAttributeMap\(\)", ".getAttributes()"), (r"\bgetAttributeMap\(\)", "getAttributes()"),
    (r"\.setEntityBoundingBox\(", ".setBoundingBox("), (r"\bsetEntityBoundingBox\(", "setBoundingBox("),
    (r"\.getHasStack\(\)", ".hasItem()"), (r"\.onSlotChanged\(\)", ".setChanged()"), (r"\.putStack\(", ".set("),
    (r"\baddSlotToContainer\(", "addSlot("), (r"\bmergeItemStack\(", "moveItemStackTo("),
    (r"\binventorySlots\b", "slots"), (r"\.getSizeInventory\(\)", ".getContainerSize()"),
    (r"\bgetSizeInventory\(\)", "getContainerSize()"),
    (r"\bguiLeft\b", "leftPos"), (r"\bguiTop\b", "topPos"), (r"\bxSize\b", "imageWidth"), (r"\bySize\b", "imageHeight"),
    (r"\bthis\.fontRenderer\b", "this.font"), (r"\bthis\.mc\b", "this.minecraft"),
    (r"\bparticleScale\b", "quadSize"), (r"\bparticleMaxAge\b", "lifetime"), (r"\bparticleAlpha\b", "alpha"),
    (r"\bparticleAge\b", "age"), (r"\bparticleRed\b", "rCol"), (r"\bparticleGreen\b", "gCol"), (r"\bparticleBlue\b", "bCol"),
    (r"\bparticleGravity\b", "gravity"), (r"\.setRBGColorF\(", ".setColor("), (r"\bsetRBGColorF\(", "setColor("),
    (r"\bEntityDataSerializers\.VARINT\b", "EntityDataSerializers.INT"),
    (r"\bEntityDataSerializers\.OPTIONAL_UNIQUE_ID\b", "EntityDataSerializers.OPTIONAL_UUID"),
    (r"\bEntityDataSerializers\.TEXT_COMPONENT\b", "EntityDataSerializers.COMPONENT"),
    (r"\bEntityDataSerializers\.FACING\b", "EntityDataSerializers.DIRECTION"),
    (r"\.addScheduledTask\(", ".execute("),
    (r"\blimbSwingAmount\b", "animationSpeed"), (r"\bprevLimbSwingAmount\b", "animationSpeedOld"),
    (r"\.getRidingEntity\(\)", ".getVehicle()"),
    (r"\bonGround\b", "onGround"),
    (r"\bfallDistance\b", "fallDistance"),
    (r"\bisCollidedHorizontally\b", "horizontalCollision"), (r"\bcollidedHorizontally\b", "horizontalCollision"),
    (r"\bcollidedVertically\b", "verticalCollision"), (r"\bisCollided\b", "horizontalCollision"),
    (r"\bisInWeb\b", "isInPowderSnow"),
    (r"\bdeathTime\b", "deathTime"), (r"\brecentlyHit\b", "lastHurtByPlayerTime"),
    (r"\battackingPlayer\b", "lastHurtByPlayer"), (r"\bidleTime\b", "noActionTime"),
    (r"\bmoveForward\b", "zza"), (r"\bmoveStrafing\b", "xxa"), (r"\brandomYawVelocity\b", "yRotA"),
    (r"\bjumpMovementFactor\b", "flyingSpeed"), (r"\bisJumping\b", "jumping"),
    (r"\bswingProgress\b", "attackAnim"), (r"\bprevSwingProgress\b", "oAttackAnim"),
    (r"\bswingingHand\b", "swingingArm"), (r"\bisSwingInProgress\b", "swinging"),
    (r"\bactiveItemStackUseCount\b", "useItemRemaining"),
    (r"\.getServerWorld\(\)", ".getLevel()"), (r"\.getEntityWorld\(\)", ".getLevel()"),
    (r"\.setCustomNameTag\(([^;]*)\);", r".setCustomName(\1);"), (r"\.getCustomNameTag\(\)", ".getCustomName().getString()"),
    (r"\.hasCustomName\(\)", ".hasCustomName()"),
    (r"\bfinal Random\b", "final RandomSource"), (r"\(Random\b", "(RandomSource"), (r", *Random\b", ", RandomSource"),
    (r"\.splitStack\(", ".split("), (r"\.getItemStack\(\)", ".getItem()"),
    (r"\bTF2Util\.getHead\b", "TF2Util.getHead"),
    (r"\.getActivePotionEffect\(", ".getEffect("),
    (r"\.getTeam\(\)\.getName\(\)", ".getTeam().getName()"),
    (r"\.getEntityAttribute\(", ".getAttribute("),
    (r"\.removeModifier\(", ".removeModifier("),
    (r"\bEnumCreatureType\.MONSTER\b", "MobCategory.MONSTER"),
    (r"\.getOwnerId\(\)", ".getOwnerUUID()"),
    (r"\.getBlockType\(\)", ".getBlockState().getBlock()"),
    (r"\bwriteToNBT\(CompoundTag", "saveAdditional(CompoundTag"),
    (r"\.isCreative\(\)", ".isCreative()"),
    (r"\.getServer\(\)\.getWorld\(", ".getServer().getLevel("),
    (r"\.playEvent\(", ".levelEvent("),
    (r"\.isSpectator\(\)", ".isSpectator()"),
    (r"\.getIdFromEntity\(", ".getId("),
    (r"\bthis\.posX\b(?!\s*[-+*/]?=[^=])", "this.getX()"), (r"\bthis\.posY\b(?!\s*[-+*/]?=[^=])", "this.getY()"),
    (r"\bthis\.posZ\b(?!\s*[-+*/]?=[^=])", "this.getZ()"),
    (r"(?<![\w.])posX\b(?!\s*[-+*/]?=[^=])", "getX()"), (r"(?<![\w.])posY\b(?!\s*[-+*/]?=[^=])", "getY()"),
    (r"(?<![\w.])posZ\b(?!\s*[-+*/]?=[^=])", "getZ()"),
    (r"(?<![\w.])motionX\b(?!\s*[-+*/]?=[^=])", "getDeltaMovement().x"),
    (r"(?<![\w.])motionY\b(?!\s*[-+*/]?=[^=])", "getDeltaMovement().y"),
    (r"(?<![\w.])motionZ\b(?!\s*[-+*/]?=[^=])", "getDeltaMovement().z"),
    (r"(?<![\w.])rotationYaw\b(?!\s*[-+*/]?=[^=])", "getYRot()"), (r"(?<![\w.])rotationPitch\b(?!\s*[-+*/]?=[^=])", "getXRot()"),
    (r"(?<![\w.])rotationYawHead\b(?!\s*[-+*/]?=[^=])", "yHeadRot"), (r"(?<![\w.])renderYawOffset\b", "yBodyRot"),
    (r"(?<![\w.])prevRotationYaw\b", "yRotO"), (r"(?<![\w.])prevRotationPitch\b", "xRotO"),
    (r"(?<![\w.])prevRotationYawHead\b", "yHeadRotO"), (r"(?<![\w.])prevRenderYawOffset\b", "yBodyRotO"),
    (r"(?<![\w.])prevPosX\b", "xo"), (r"(?<![\w.])prevPosY\b", "yo"), (r"(?<![\w.])prevPosZ\b", "zo"),
    (r"(?<![\w.])lastTickPosX\b", "xOld"), (r"(?<![\w.])lastTickPosY\b", "yOld"), (r"(?<![\w.])lastTickPosZ\b", "zOld"),
    (r"(?<![\w.])ticksExisted\b", "tickCount"), (r"(?<![\w.])world\.isRemote\b", "level.isClientSide"),
    (r"(?<![\w.])isRemote\b", "isClientSide"),
    (r"\.rotationYawHead\s*=", ".yHeadRot ="), (r"\.renderYawOffset\s*=", ".yBodyRot ="),
    (r"\.setPositionAndUpdate\(", ".teleportTo("),
]

# x.motionY = v;  ->  x.setDeltaMovement(x.getDeltaMovement().x, v, x.getDeltaMovement().z);
AXES = {"X": 0, "Y": 1, "Z": 2}
def motion_assign(m):
    target, axis, op, val = m.group(1) or "", m.group(2), m.group(3), m.group(4)
    t = target  # "" or "entity."
    comps = ["%sgetDeltaMovement().x" % t, "%sgetDeltaMovement().y" % t, "%sgetDeltaMovement().z" % t]
    i = AXES[axis]
    cur = comps[i]
    if op == "=":
        comps[i] = "(%s)" % val
    else:
        comps[i] = "%s %s (%s)" % (cur, op[0], val)
    return "%ssetDeltaMovement(%s);" % (t, ", ".join(comps))
MOTION_RE = re.compile(r"((?:[\w.()]+\.)?)motion([XYZ])\s*([-+*/]?=)\s*([^;]+);")

POS_RE = re.compile(r"((?:[\w.()]+\.)?)pos([XYZ])\s*([-+*/]?=)\s*([^;]+);")
def pos_assign(m):
    t, axis, op, val = m.group(1) or "", m.group(2), m.group(3), m.group(4)
    comps = ["%sgetX()" % t, "%sgetY()" % t, "%sgetZ()" % t]
    i = AXES[axis]
    comps[i] = "(%s)" % val if op == "=" else "%s %s (%s)" % (comps[i], op[0], val)
    return "%ssetPos(%s);" % (t, ", ".join(comps))

ROT_RE = re.compile(r"((?:[\w.()]+\.)?)rotation(Yaw|Pitch)\s*([-+*/]?=)\s*([^;]+);")
def rot_assign(m):
    t, which, op, val = m.group(1) or "", m.group(2), m.group(3), m.group(4)
    g, s = ("getYRot", "setYRot") if which == "Yaw" else ("getXRot", "setXRot")
    v = "(%s)" % val if op == "=" else "%s%s() %s (%s)" % (t, g, op[0], val)
    return "%s%s(%s);" % (t, s, v)

def find_close(s, i):
    """i is index just after '('; return index of matching ')'."""
    depth = 1
    while i < len(s):
        c = s[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i
        elif c == '"':
            i += 1
            while s[i] != '"':
                if s[i] == "\\":
                    i += 1
                i += 1
        i += 1
    return -1

def split_args(a):
    out, depth, cur = [], 0, ""
    for c in a:
        if c in "([{<" and not (c == "<" and depth == 0 and False):
            depth += c in "([{"
        if c in ")]}":
            depth -= 1
        if c == "," and depth == 0:
            out.append(cur); cur = ""
        else:
            cur += c
    out.append(cur)
    return [x.strip() for x in out]

def fix_itemstack(s):
    """new ItemStack(a, b, c) where c is not a CompoundTag -> TF2Util.metaStack(a, b, c)."""
    out, i = "", 0
    while True:
        j = s.find("new ItemStack(", i)
        if j < 0:
            out += s[i:]; break
        k = find_close(s, j + len("new ItemStack("))
        if k < 0:
            out += s[i:]; break
        args = split_args(s[j + len("new ItemStack("):k])
        inner = fix_itemstack(s[j + len("new ItemStack("):k])
        if len(args) == 3 and "Tag" not in args[2] and "nbt" not in args[2].lower():
            out += s[i:j] + "TF2Util.metaStack(" + inner + ")"
        else:
            out += s[i:j] + "new ItemStack(" + inner + ")"
        i = k + 1
    return out

def split_code(src):
    pos = 0
    pat = re.compile(r'"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])+\'|//[^\n]*|/\*.*?\*/', re.S)
    for m in pat.finditer(src):
        if m.start() > pos:
            yield True, src[pos:m.start()]
        yield False, m.group(0)
        pos = m.end()
    if pos < len(src):
        yield True, src[pos:]

def guarded(regex, fn):
    """Only rewrite assignments that start a statement (not declarations like `double posX = ...`)."""
    def wrap(c):
        def rep(m):
            before = c[:m.start()].rstrip()
            if before and before[-1] not in ";{}" and not before.endswith(")") and not before.endswith("else"):
                return m.group(0)
            return fn(m)
        return regex.sub(rep, c)
    return wrap

DECL_RE = re.compile(r"\b(?:double|float|int|long)\s+(posX|posY|posZ|motionX|motionY|motionZ|rotationYaw|rotationPitch|rotationYawHead|prevPosX|prevPosY|prevPosZ|rand)\b")
DECLARED = set()

def fix_code(c):
    c = guarded(MOTION_RE, motion_assign)(c)
    c = guarded(POS_RE, pos_assign)(c)
    c = guarded(ROT_RE, rot_assign)(c)
    for p, r in R:
        if p.startswith("(?<![\\w.])") and any(n in p for n in DECLARED):
            continue
        c = re.sub(p, r, c)
    return c

skip = set(sys.argv[2:])
for root, _, files in os.walk(ROOT):
    for f in files:
        if not f.endswith(".java") or f in skip:
            continue
        p = os.path.join(root, f)
        src = open(p, encoding="utf-8").read()
        DECLARED.clear()
        DECLARED.update(DECL_RE.findall(src))
        body = "".join(fix_code(t) if code else t for code, t in split_code(src))
        body = fix_itemstack(body)
        if body != src:
            open(p, "w", encoding="utf-8").write(body)
print("ok")
