"""Third mechanical pass (in place). Each rule is idempotent."""
import os, re, sys

ROOT = sys.argv[1]
R = [
    # onGround is protected in 1.19.2: reads go through isOnGround()
    (r"(?<!this)\.onGround\b(?!\s*=[^=])", ".isOnGround()"),
    (r"(?<![\w.])onGround\b(?!\s*=[^=])(?!\()", "isOnGround()"),
    # player inventory
    (r"getInventory\(\)\.getStackInSlot\(", "getInventory().getItem("),
    (r"getInventory\(\)\.setInventorySlotContents\(", "getInventory().setItem("),
    (r"getInventory\(\)\.getCurrentItem\(\)", "getInventory().getSelected()"),
    (r"getInventory\(\)\.getFirstEmptyStack\(\)", "getInventory().getFreeSlot()"),
    (r"getInventory\(\)\.addItemStackToInventory\(", "getInventory().add("),
    (r"getInventory\(\)\.markDirty\(\)", "getInventory().setChanged()"),
    (r"getInventory\(\)\.decrStackSize\(", "getInventory().removeItem("),
    (r"getInventory\(\)\.hasItemStack\(", "getInventory().contains("),
    (r"\.addItemStackToInventory\(", ".add("),
    # damage sources
    (r"\.isFireDamage\(\)", ".isFire()"), (r"\.isMagicDamage\(\)", ".isMagic()"),
    (r"\.isExplosion\(\)", ".isExplosion()"), (r"\.isProjectile\(\)", ".isProjectile()"),
    (r"\.isDifficultyScaled\(\)", ".scalesWithDifficulty()"), (r"\.isUnblockable\(\)", ".isBypassArmor()"),
    (r"\.setDamageBypassesArmor\(\)", ".bypassArmor()"), (r"\.setDamageIsAbsolute\(\)", ".bypassMagic()"),
    (r"\.setDamageAllowedInCreativeMode\(\)", ".bypassInvul()"), (r"\.setFireDamage\(\)", ".setIsFire()"),
    (r"\.setMagicDamage\(\)", ".setMagic()"), (r"\.setExplosion\(\)", ".setExplosion()"),
    (r"\.setProjectile\(\)", ".setProjectile()"), (r"\.getDamageType\(\)", ".getMsgId()"),
    (r"\.damageType\b", ".msgId"), (r"\.getDamageLocation\(\)", ".getSourcePosition()"),
    (r"\.isActiveItemStackBlocking\(\)", ".isBlocking()"), (r"\.isElytraFlying\(\)", ".isFallFlying()"),
    (r"\bmaxHurtResistantTime\b", "invulnerableDuration"),
    # vectors
    (r"\.subtractReverse\(", ".vectorTo("), (r"\.getHorizontalAngle\(\)", ".toYRot()"),
    (r"\bDirection\.Axis\.", "Direction.Axis."),
    # world
    (r"\.getDifficultyId\(\)", ".getId()"), (r"\.provider\.getDimension\(\)", ".dimension()"),
    (r"\.hasNoGravity\(\)", ".isNoGravity()"), (r"\.setNoGravity\(", ".setNoGravity("),
    (r"\.getSoundCategory\(\)", ".getSoundSource()"),
    # scoreboard
    (r"\.getColorIndex\(\)", ".getId()"), (r"\.getAllowFriendlyFire\(\)", ".isAllowFriendlyFire()"),
    (r"\.getTeamName\(\)", ".getName()"), (r"\.getMembershipCollection\(\)", ".getPlayers()"),
    (r"\.getScoreboard\(\)\.getTeam\(", ".getScoreboard().getPlayerTeam("),
    (r"\.getPlayersTeam\(", ".getPlayersTeam("),
    # ai
    (r"\.setLookPositionWithEntity\(", ".setLookAt("), (r"\.canSee\(", ".hasLineOfSight("),
    (r"\.tryMoveToEntityLiving\(", ".moveTo("), (r"\.tryMoveToXYZ\(", ".moveTo("),
    (r"\.getPathToEntityLiving\(", ".createPath("), (r"\.getPathToXYZ\(", ".createPath("),
    (r"\.getPathToPos\(", ".createPath("), (r"\.setPath\(", ".moveTo("),
    (r"\bshouldExecute\(\)", "canUse()"), (r"\bshouldContinueExecuting\(\)", "canContinueToUse()"),
    (r"\bstartExecuting\(\)", "start()"), (r"\bresetTask\(\)", "stop()"), (r"\bupdateTask\(\)", "tick()"),
    (r"\bisInterruptible\(\)", "isInterruptable()"),
    (r"\.setMoveTo\(", ".setWantedPosition("), (r"\.setMoveForward\(", ".setZza("), (r"\.setMoveStrafing\(", ".setXxa("),
    (r"\.setAIMoveSpeed\(", ".setSpeed("), (r"\.getAIMoveSpeed\(\)", ".getSpeed()"),
    (r"\.getJumpHelper\(\)", ".getJumpControl()"), (r"\.setJumping\(\)", ".jump()"),
    (r"\.isOnLadder\(\)", ".onClimbable()"), (r"\.getMaxSpawnedInChunk\(\)", ".getMaxSpawnClusterSize()"),
    (r"\.enablePersistence\(\)", ".setPersistenceRequired()"), (r"\.isNoDespawnRequired\(\)", ".isPersistenceRequired()"),
    # items / stacks
    (r"\.getTagType\(\)", ".getElementType()"), (r"\.getItemEnchantability\(\)", ".getEnchantmentValue()"),
    (r"\.isFull3D\(\)", ".isFull3D()"), (r"\.getItemUseAction\(", ".getUseAnimation("),
    (r"\.getMaxItemUseDuration\(\)", ".getUseDuration()"),
    (r"\.getMaxDamage\(\)", ".getMaxDamage()"), (r"\.isItemDamaged\(\)", ".isDamaged()"),
    (r"\.getRepairCost\(\)", ".getBaseRepairCost()"), (r"\.setRepairCost\(", ".setRepairCost("),
    (r"\.getEnchantmentTagList\(\)", ".getEnchantmentTags()"), (r"\.addEnchantment\(", ".enchant("),
    (r"\.onItemUse\(", ".useOn("),
    (r"\.getItemStackLimit\(\)", ".getMaxStackSize()"),
    (r"ItemStack\.EMPTY\.copy\(\)", "ItemStack.EMPTY"),
    (r"new ItemStack\(([^()]+)\.getCompound\(([^()]*)\)\)", r"ItemStack.of(\1.getCompound(\2))"),
    (r"new ItemStack\(([\w.]+)\)\s*;\s*//nbt", r"ItemStack.of(\1);"),
    (r"\.writeToNBT\(new CompoundTag\(\)\)", ".save(new CompoundTag())"),
    # blocks
    (r"\.getMaterial\(\)\.isLiquid\(\)", ".getMaterial().isLiquid()"),
    (r"\.getBlockHardness\(", ".getDestroySpeed("),
    # misc
    (r"\.sendPacket\(", ".send("), (r"\.connection\.sendPacket\(", ".connection.send("),
    (r"\bEnumHandSide\b", "HumanoidArm"),
    (r"\.getActivePotionEffects\(\)", ".getActiveEffects()"),
    (r"\.getIsAmbient\(\)", ".isAmbient()"), (r"\.doesShowParticles\(\)", ".isVisible()"),
    (r"\.getCurativeItems\(\)", ".getCurativeItems()"),
    (r"\.isBadEffect\(\)", ".getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL"),
    (r"\.isBeneficial\(\)", ".isBeneficial()"),
    (r"\.getLiquidColor\(\)", ".getColor()"),
    (r"\.getCreativeTab\(\)", ".getItemCategory()"),
    (r"\.getSelectedItem\(\)", ".getSelected()"),
    (r"\bgetEyeHeight\(\)", "getEyeHeight()"),
    (r"\.getLevel\(\)\.random\b", ".getLevel().random"),
    (r"\bTF2Util\.rayTraceBlocks\b", "TF2Util.rayTraceBlocks"),
]

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

def fix(c):
    for p, r in R:
        c = re.sub(p, r, c)
    return c

for root, _, files in os.walk(ROOT):
    if "compat" in root:
        continue
    for f in files:
        if not f.endswith(".java"):
            continue
        p = os.path.join(root, f)
        src = open(p, encoding="utf-8").read()
        out = "".join(fix(t) if code else t for code, t in split_code(src))
        if out != src:
            open(p, "w", encoding="utf-8").write(out)
print("ok")
