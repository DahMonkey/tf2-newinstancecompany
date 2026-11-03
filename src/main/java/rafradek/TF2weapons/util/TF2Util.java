package rafradek.TF2weapons.util;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.Container;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.message.IMessage;
import rafradek.TF2weapons.message.TF2NetworkWrapper.TargetPoint;

import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon.DestroyBlockEntry;
import rafradek.TF2weapons.TF2EventsCommon.InboundDamage;
import rafradek.TF2weapons.TF2EventsCommon.TF2WorldStorage;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.TF2EventsClient;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.EntityStatue;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.boss.EntityTF2Boss;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.inventory.InventoryWearables;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.function.Predicate;
import net.minecraftforge.entity.PartEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.RandomSource;

public class TF2Util {

	public static final UUID FOLLOW_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE978AD348");

	public static final Block[] NATURAL_BLOCKS = new Block[] { Blocks.STONE };
	public static final Predicate<Entity> TARGETABLE = target -> (target.isPickable()
			&& (!(target instanceof LivingEntity)
					|| (target instanceof LivingEntity && ((LivingEntity) target).deathTime <= 0))
			&& !(target instanceof EntityStatue));

	public static int[] colorCode = new int[32];
	public static final float[] ASIN_VALUES = new float[512];

	/** Block ray trace that returns null on a miss, like 1.12's World.rayTraceBlocks. */
	public static BlockHitResult rayTraceBlocks(Level world, Vec3 start, Vec3 end, Entity entity, boolean stopOnLiquid) {
		BlockHitResult result = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
				stopOnLiquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entity));
		return result.getType() == HitResult.Type.MISS ? null : result;
	}

	public static BlockHitResult rayTraceBlocks(Level world, Vec3 start, Vec3 end) {
		return rayTraceBlocks(world, start, end, null, false);
	}

	public static List<HitResult> pierce(Level world, Entity living, double startX, double startY, double startZ,
			double endX, double endY, double endZ, boolean headshot, float size, boolean pierce,
			Predicate<Entity> selector) {
		ArrayList<HitResult> targets = new ArrayList<>();
		Vec3 var17 = new Vec3(startX, startY, startZ);
		Vec3 var3 = new Vec3(endX, endY, endZ);
		Vec3 mid = new Vec3((startX + endX) / 2D, (startY + endY) / 2D, (startZ + endZ) / 2D);
		BlockHitResult var4 = rayTraceBlocks(world, var17, var3, living, false);

		if (var4 != null)
			var3 = var4.getLocation();
		else
			var3 = new Vec3(endX, endY, endZ);
		Entity var5 = null;
		Iterable<Entity> var6;
		if (var3.subtract(var17).lengthSqr() > 16384D)
			var6 = Iterables.concat(
					living.level.getEntities(living,
							new AABB(startX, startY, startZ, mid.x, mid.y, mid.z).inflate(2D, 2D, 2D)),
					living.level.getEntities(living,
							new AABB(mid.x, mid.y, mid.z, endX, endY, endZ).inflate(2D, 2D, 2D)));
		else
			var6 = living.level.getEntities(living,
					new AABB(startX, startY, startZ, endX, endY, endZ).inflate(2D, 2D, 2D));
		double var7 = 0.0D;
		Vec3 collideVec = null;
		for (Entity target : var6)
			if (selector.test(target)) {
				AABB oldBB = target.getBoundingBox();
				if (world.isClientSide && TF2EventsClient.moveEntities) {
					float ticktime = TF2EventsClient.tickTime;
					target.setBoundingBox(
							target.getBoundingBox().move((target.xo - target.getX()) * (1 - ticktime),
									(target.yo - target.getY()) * (1 - ticktime),
									(target.zo - target.getZ()) * (1 - ticktime)));
				}
				AABB var12 = target.getBoundingBox().inflate(size, size, size);
				Vec3 var13 = var12.clip(var17, var3).orElse(null);

				if (var13 == null && var12.contains(var3)) {
					var13 = var3;
				}

				if (var13 != null) {
					double var14 = var17.distanceToSqr(var13);

					if (!pierce && (var14 < var7 || var7 == 0.0D)) {
						var5 = target;
						var7 = var14;
						collideVec = var13;
					} else if (pierce)
						targets.add(TF2Util.getTraceResult(target, var13, size, headshot, var17, var3));
				}

				target.setBoundingBox(oldBB);
			}
		if (!pierce && var5 != null && !(var5 instanceof LivingEntity && ((LivingEntity) var5).getHealth() <= 0))
			targets.add(TF2Util.getTraceResult(var5, collideVec, size, headshot, var17, var3));
		if ((pierce || targets.isEmpty()) && var4 != null && var4.getType() == HitResult.Type.BLOCK)
			targets.add(var4);
		else if (targets.isEmpty())
			targets.add(BlockHitResult.miss(var3, Direction.UP, new BlockPos(var3)));
		return targets;
	}

	public static List<HitResult> pierce(Level world, Entity living, double startX, double startY, double startZ,
			double endX, double endY, double endZ, boolean headshot, float size, boolean pierce) {
		return pierce(world, living, startX, startY, startZ, endX, endY, endZ, headshot, size, pierce, TARGETABLE);
	}

	public static List<HitResult> pierce(Level world, Entity living, double length, boolean headshot, float size,
			boolean pierce) {
		Vec3 look = living.getLookAngle().scale(length);
		return pierce(world, living, living.getX(), living.getY() + living.getEyeHeight(), living.getZ(),
				living.getX() + look.x, living.getY() + living.getEyeHeight() + look.y, living.getZ() + look.z, headshot,
				size, pierce, TARGETABLE);
	}

	public static TF2HitResult getTraceResult(Entity target, Vec3 hitVec, float size, boolean headshot, Vec3 start,
			Vec3 end) {
		TF2HitResult result = new TF2HitResult(target, hitVec);

		if (headshot && target instanceof LivingEntity
				&& !(target instanceof IEntityTF2 && !((IEntityTF2) target).hasHead())) {
			result.hitInfo = TF2Util.getHead((LivingEntity) target).clip(start, end.add(end.subtract(start).normalize()))
					.isPresent();
		}
		return result;
	}

	public static AABB getHead(LivingEntity target) {
		double ymax = target.getY() + target.getEyeHeight();
		boolean custom = target instanceof IEntityTF2;
		AABB head;
		if (!custom || (head = ((IEntityTF2) target).getHeadBox()) == null) {
			head = new AABB(target.getX() - 0.25, ymax - 0.16, target.getZ() - 0.25, target.getX() + 0.25,
					ymax + 0.25, target.getZ() + 0.25);

			if (target.getBbWidth() >= 0.63) {
				float offsetX = -Mth.sin(target.yBodyRot * 0.017453292F)
						* Math.max(0f, target.getBbWidth() * 0.6f - 0.32f);
				float offsetZ = Mth.cos(target.yBodyRot * 0.017453292F)
						* Math.max(0f, target.getBbWidth() * 0.6f - 0.32f);

				head = head.move(offsetX, 0, offsetZ);
			}
		}
		return head;
	}

	public static Vec3 radiusRandom2D(float radius, RandomSource random) {
		/*
		 * double x=random.nextDouble()*radius*2-radius; radius -= Math.abs(x); double
		 * y=random.nextDouble()*radius*2-radius;
		 */

		/*
		 * double t = 4*Math.PI*random.nextDouble()*radius-radius; double u =
		 * (random.nextDouble()*radius*2-radius)+(random.nextDouble()*radius*2-
		 * raddddddddius); double r = u>1?2-u:u;
		 */
		float a = random.nextFloat(), b = random.nextFloat();
		return new Vec3(
				Math.max(a, b) * radius * Mth.cos((float) (2f * Math.PI * Math.min(a, b) / Math.max(a, b))),
				Math.max(a, b) * radius * Mth.sin((float) (2f * Math.PI * Math.min(a, b) / Math.max(a, b))), 0);
	}

	public static Vec3 radiusRandom2D(float radius, RandomSource random, float yaw, float pitch, double dist) {
		/*
		 * double x=random.nextDouble()*radius*2-radius; radius -= Math.abs(x); double
		 * y=random.nextDouble()*radius*2-radius;
		 */

		/*
		 * double t = 4*Math.PI*random.nextDouble()*radius-radius; double u =
		 * (random.nextDouble()*radius*2-radius)+(random.nextDouble()*radius*2-
		 * raddddddddius); double r = u>1?2-u:u;
		 */
		float a, b;
		double x;
		double y;

		do {
			a = random.nextFloat() * 2f - 1f;
			b = random.nextFloat() * 2f - 1f;
		} while (a * a + b * b > 1);

		x = a * radius;
		y = b * radius;

		/*
		 * a = random.nextFloat(); b = random.nextFloat(); x = Math.max(a, b) * radius *
		 * MathHelper.cos((float) (2f * Math.PI * Math.min(a, b) / Math.max(a, b))); y =
		 * Math.max(a, b) * radius * MathHelper.sin((float) (2f * Math.PI * Math.min(a,
		 * b) / Math.max(a, b)));
		 */

		double z = 1;

		float yawcos = Mth.cos(yaw);
		float f1 = Mth.sin(yaw);
		float f2 = Mth.cos(pitch);
		float f3 = Mth.sin(pitch);

		double y1 = y * f2 + z * f3;
		double z1 = z * f2 - y * f3;
		double x1 = x * yawcos + z1 * f1;
		double z2 = z1 * yawcos - x * f1;
		return new Vec3(x1 * dist, y1 * dist, z2 * dist);
	}

	public static boolean isUsingShield(Entity shielded, DamageSource source) {
		if (shielded instanceof LivingEntity && ((LivingEntity) shielded).isBlocking()) {
			Vec3 location = source.getSourcePosition();
			if (location == null)
				location = source.getDirectEntity().position();
			if (location != null) {
				Vec3 vec3d1 = shielded.getViewVector(1.0F);
				Vec3 vec3d2 = location.vectorTo(shielded.position()).normalize();
				vec3d2 = new Vec3(vec3d2.x, 0.0D, vec3d2.z);

				if (vec3d2.dot(vec3d1) < 0.0D)
					return true;
			}
		}
		return false;
	}

	public static int calculateCritPost(Entity target, LivingEntity shooter, int initial, ItemStack stack,
			DamageSource source) {

		if (initial == 0 && (target instanceof LivingEntity
				&& (((LivingEntity) target).getEffect(TF2weapons.markDeath) != null
				|| ((LivingEntity) target).getEffect(TF2weapons.jarate) != null)))
			initial = 1;

		if (initial == 0 && !stack.isEmpty() && !target.isOnGround() && !target.isInWater()
				&& TF2Attribute.getModifier("Minicrit Airborne", stack, 0, shooter) != 0) {
			if (!(WeaponsCapability.get(target) != null && !WeaponsCapability.get(target).isExpJump()))
				initial = 1;
		}

		if (initial == 0 && !stack.isEmpty() && shooter != null) {
			float mindist = TF2Attribute.getModifier("Minicrit Distance", stack, 0, shooter);
			mindist *= mindist;
			if (mindist != 0f && target.distanceToSqr(shooter) >= mindist)
				initial = 1;
		}
		if (initial == 0 && (!stack.isEmpty() && target.isOnFire()
				&& TF2Attribute.getModifier("Crit Burn", stack, 0, shooter) == 2))
			initial = 1;
		else if (initial < 2 && (!stack.isEmpty() && target.isOnFire()
				&& TF2Attribute.getModifier("Crit Burn", stack, 0, shooter) == 1))
			initial = 2;

		if (initial < 2
				&& (!stack.isEmpty() && (target instanceof LivingEntity
						&& ((LivingEntity) target).getEffect(TF2weapons.stun) != null))
				&& TF2Attribute.getModifier("Crit Stun", stack, 0, shooter) != 0)
			initial = 2;

		if (initial < 2 && (!stack.isEmpty() && shooter != null && shooter instanceof Player
				&& (shooter.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isExpJump() || shooter.isFallFlying())
				&& TF2Attribute.getModifier("Crit Rocket", stack, 0, shooter) != 0))
			initial = 2;

		if (initial == 1 && (!stack.isEmpty() && shooter != null && shooter instanceof Player
				&& TF2Attribute.getModifier("Crit Mini", stack, 0, shooter) != 0))
			initial = 2;

		if (initial > 0 && (target instanceof LivingEntity
				&& ((LivingEntity) target).getEffect(TF2weapons.backup) != null))
			initial = 0;

		if (initial > 0 && source.isProjectile() && !source.isExplosion() && !source.isFire()
				&& !source.isMagic()
				&& (target instanceof LivingEntity
						&& ((LivingEntity) target).getEffect(TF2weapons.shieldBullet) != null
						&& ((LivingEntity) target).getEffect(TF2weapons.shieldBullet)
						.getAmplifier() > 0))
			initial = 0;

		if (initial > 0 && source.isExplosion() && (target instanceof LivingEntity
				&& ((LivingEntity) target).getEffect(TF2weapons.shieldExplosive) != null
				&& ((LivingEntity) target).getEffect(TF2weapons.shieldExplosive).getAmplifier() > 0))
			initial = 0;

		if (initial > 0 && source.isFire()
				&& (target instanceof LivingEntity
						&& ((LivingEntity) target).getEffect(TF2weapons.shieldFire) != null
						&& ((LivingEntity) target).getEffect(TF2weapons.shieldFire).getAmplifier() > 0))
			initial = 0;

		if (initial > 1 && TF2Attribute.getModifier("Crits Become Mini Crits", stack, 0, shooter) != 0)
			initial = 1;

		if (target instanceof EntityBuilding && initial == 1)
			initial = 0;

		if (target instanceof EntityTF2Boss && initial == 1)
			initial = 0;

		if (target instanceof EntityMerasmus
				&& ((EntityMerasmus) target).getEffect(TF2weapons.stun) != null)
			initial = 2;
		return initial;
	}

	//tf2 damage calulation formula
	public static float calculateDamage(Entity target, Level world, LivingEntity living, ItemStack stack,
			int critical, float distance) {
		ItemWeapon weapon = (ItemWeapon) stack.getItem();
		float calculateddamage = weapon.getWeaponDamage(stack, living, target);

		if (calculateddamage == 0)
			return 0f;
		if (target == living)
			return calculateddamage;
		if (critical == 2 && !(target instanceof IEntityTF2 && ((IEntityTF2) target).isBuilding()))
			calculateddamage *= 3;
		else if (critical == 1)
			calculateddamage *= 1.35f;
		if (target == TF2weapons.dummyEnt)
			distance *= 0.5f;
		float falloff = weapon.getWeaponDamageFalloff(stack);
		float falloffmax = weapon.getWeaponDamageFalloffMaxRange(stack, living);
		if (!(target instanceof IEntityTF2 && !((IEntityTF2) target).hasDamageFalloff()) && falloff > 0
				&& (critical < 2 || target == living))
			if (distance <= falloff)
				// calculateddamage *=weapon.maxDamage - ((distance /
				// (float)weapon.damageFalloff) *
				// (weapon.maxDamage-weapon.damage));
				calculateddamage *= TF2Util.lerp(weapon.getWeaponMaxDamage(stack, living), 1f, (distance / falloff));
			else if (critical == 0)
				// calculateddamage
				// *=Math.max(weapon.getWeaponMinDamage(stack,living)/weapon.getWeaponDamage(stack,living),
				// ((weapon.getWeaponDamage(stack,living)) -
				// (((distance-weapon.getWeaponDamageFalloff(stack)) /
				// ((float)weapon.getWeaponDamageFalloff(stack)*2)) *
				// (weapon.getWeaponDamage(stack,living)-weapon.getWeaponMinDamage(stack,living))))/weapon.getWeaponDamage(stack,living));
				calculateddamage *= TF2Util.lerp(1f, weapon.getWeaponMinDamage(stack, living),
						Math.min(1, (distance - falloff) / (falloffmax - falloff)));
		/*
		 * (Math.min(distance / weapon.getWeaponDamageFalloff(stack),
		 * TF2Attribute.getModifier("Accuracy", stack, 2, living)) - 1) /
		 * (TF2Attribute.getModifier("Accuracy", stack, 2, living) - 1));
		 */
		// calculateddamage *= 1 -
		// (1-weapon.getWeaponMinDamage(stack,living))*(Math.min(distance/weapon.getWeaponDamageFalloff(stack),2*TF2Attribute.getModifier("Accuracy",
		// stack, 1,living))-1*TF2Attribute.getModifier("Accuracy", stack,
		// 1,living));
		// System.out.println((distance-weapon.getWeaponDamageFalloff(stack))-(weapon.getWeaponDamageFalloff(stack)*2));
		if (target instanceof EnderMan && !(stack.getItem() instanceof ItemMeleeWeapon
				|| TF2Attribute.getModifier("Unblockable", stack, 0, living) != 0))
			calculateddamage *= 0.4f;

		/*
		 * if (living instanceof IRangedWeaponAttackMob)
		 * calculateddamage*=((IRangedWeaponAttackMob)living).
		 * getAttributeModifier("Damage");
		 */
		return calculateddamage;
	}

	public static float lerp(float v0, float v1, float t) {
		return (1 - t) * v0 + t * v1;
	}

	public static float position(float v0, float v1, float lerp) {
		return (lerp - v1) * (1f / (v0 - v1));
	}

	/** Same as {@link #isOnSameTeam}; the rename pass turned calls into this name. */
	public static boolean isAlliedTo(Entity entity1, Entity entity2) {
		return isOnSameTeam(entity1, entity2);
	}

	public static boolean isOnSameTeam(Entity entity1, Entity entity2) {
		return entity1 != null && entity2 != null
				&& ((TF2Util.getTeam(entity1) == TF2Util.getTeam(entity2) && TF2Util.getTeam(entity1) != null)
						|| (entity1 instanceof OwnableEntity && ((OwnableEntity) entity1).getOwner() == entity2)
						|| (entity2 instanceof OwnableEntity && ((OwnableEntity) entity2).getOwner() == entity1)
						|| entity1 == entity2);

	}

	public static Team getTeam(Entity living) {
		if (living == null)
			return null;
		else if (!(living instanceof Projectile))
			return living.getTeam();
		else
			return getTeam(((Projectile) living).getOwner());
	}

	public static int getTeamColor(Entity living) {
		/*
		 * if (living instanceof EntityTF2Character) return ((EntityTF2Character)
		 * living).getEntTeam(); else if (living instanceof EntityBuilding) return
		 * ((EntityBuilding) living).getEntTeam(); else if (living instanceof
		 * EntityPlayer) return ((EntityPlayer) living).getTeam() ==
		 * living.world.getScoreboard().getTeam("BLU") ? 1 : 0; else if (living
		 * instanceof IThrowableEntity) return getTeamForDisplay(((IThrowableEntity)
		 * living).getThrower());
		 */
		return colorCode[living != null ? getTeamColorNumber(living) : 0];
	}

	public static int getTeamForDisplay(Entity living) {
		if (living instanceof EntityTF2Character)
			return ((EntityTF2Character) living).getEntTeam();
		else if (living instanceof EntityBuilding)
			return ((EntityBuilding) living).getEntTeam();
		else if (living instanceof Player)
			return ((Player) living).getTeam() == living.level.getScoreboard().getPlayerTeam("BLU") ? 1 : 0;
		else if (living instanceof Projectile)
			return getTeamForDisplay(((Projectile) living).getOwner());
		return 0;
	}

	public static int getTeamColorNumber(Entity living) {
		if (living.isAlive() && getTeam(living) != null && getTeam(living).getColor().getId() >= 0)
			return getTeam(living).getColor().getId();
		else
			return 7;
	}

	public static boolean canHit(LivingEntity shooter, Entity ent) {
		// System.out.println("allowed: "+isOnSameTeam(shooter,ent)+"
		// "+!(shooter.getTeam()!=null&&shooter.getTeam().getAllowFriendlyFire())+"
		// "+(ent!=shooter)+" "+!(shooter instanceof
		// EntityBuilding&&((EntityBuilding)shooter).getOwner()==ent));
		return ent.isAlive() && !(ent instanceof LivingEntity && isOnSameTeam(shooter, ent)
				&& !(shooter.getTeam() != null && shooter.getTeam().isAllowFriendlyFire())
				&& (ent != shooter) /*
				 * && !(shooter instanceof IEntityOwnable && ((IEntityOwnable)
				 * shooter).getOwner() == ent)
				 */);
	}

	public static <T extends Entity> T getClosestEntityInCone(Vec3 start, Vec3 end, List<T> list, double aperture) {
		double mindist = Double.MAX_VALUE;
		T minentity = null;
		for (T entity : list) {
			double entdist = entity.distanceToSqr(start.x, start.y, start.z);
			if (entdist < mindist && (aperture >= 180 || TF2Util.isLyingInCone(entity.position(), start, end,
					(float) Math.toRadians(aperture)))) {
				mindist = entdist;
				minentity = entity;
			}
		}
		return minentity;
	}

	public static boolean lookingAt(LivingEntity entity, double max, double targetX, double targetY,
			double targetZ) {
		return TF2Util.isLyingInCone(new Vec3(targetX, targetY, targetZ), entity.getEyePosition(1),
				entity.getEyePosition(1).add(entity.getViewVector(1)), (float) Math.toRadians(max));
	}

	public static boolean lookingAt(LivingEntity entity, double max, Entity target) {
		return TF2Util.isLyingInCone(target.getEyePosition(1f), entity.getEyePosition(1),
				entity.getEyePosition(1).add(entity.getViewVector(1)), (float) Math.toRadians(max));
	}

	public static boolean lookingAtFast(LivingEntity entity, double max, double targetX, double targetY,
			double targetZ) {
		double d0 = targetX - entity.getX();
		double d1 = targetY - (entity.getY() + entity.getEyeHeight());
		double d2 = targetZ - entity.getZ();
		double d3 = Mth.sqrt((float)d0 * d0 + d2 * d2);
		float f = (float) (Mth.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
		float f1 = (float) (-(Mth.atan2(d1, d3) * 180.0D / Math.PI));
		float compareyaw = Math.abs(180 - Math.abs(Math.abs(f - Mth.wrapDegrees(entity.yHeadRot)) - 180));
		float comparepitch = Math.abs(180 - Math.abs(Math.abs(f1 - entity.getXRot()) - 180));
		// System.out.println("Angl: "+compareyaw+" "+comparepitch);
		return compareyaw < max && comparepitch < max / 2D;
	}

	/**
	 * @param x        coordinates of point to be tested
	 * @param start        coordinates of apex point of cone
	 * @param end       coordinates of center of basement circle
	 * @param aperture in radians
	 */
	static public boolean isLyingInCone(Vec3 x, Vec3 start, Vec3 end, float aperture) {

		// This is for our convenience
		float halfAperture = aperture / 2.f;

		// Vector pointing to X point from apex
		Vec3 apexToXVect = start.subtract(x);

		// Vector pointing from apex to circle-center point.
		Vec3 axisVect = start.subtract(end);

		boolean isInInfiniteCone = apexToXVect.dot(axisVect) / apexToXVect.length()
				/ axisVect.length() > Mth.cos(halfAperture);

				return isInInfiniteCone;
	}

	public static boolean dealDamage(Entity entity, Level world, LivingEntity living, ItemStack stack, int critical,
			float damage, DamageSource source) {
		if (world.isClientSide || damage == 0)
			return false;

		if (TF2ConfigVars.batchDamage == 0) {
			entity.invulnerableTime = 0;
			if (entity instanceof PartEntity
					&& !(((PartEntity<?>) entity).getParent() instanceof EnderDragon))
				((Entity) (((PartEntity<?>) entity).getParent())).invulnerableTime = 0;
		}

		if (entity instanceof Player && !(living instanceof Player) && !source.scalesWithDifficulty()
				&& TF2ConfigVars.scaleAttributes) {
			if (world.getDifficulty() == Difficulty.NORMAL) {
				damage *= 0.7f;
			} else if (world.getDifficulty() == Difficulty.HARD) {
				damage *= 0.9f;
			} else
				damage *= 0.45f;
		}

		damage *= TF2ConfigVars.damageMultiplier;

		if (entity == living && source instanceof TF2DamageSource && living instanceof Player
				&& living.getTeam() != null) {
			((TF2DamageSource) source).setAttackSelf();
		}

		if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon
				&& ItemFromData.getData(stack).hasProperty(PropertyType.HIT_SOUND))
			((ItemWeapon) stack.getItem()).playHitSound(stack, living, entity);
		boolean knockback = canHit(living, entity);
		if (knockback && isUsingShield(entity, source) && source.getSourcePosition() == null
				&& TF2Attribute.getModifier("Unblockable", stack, 0, living) != 1) {
			LivingEntity blocker = (LivingEntity) entity;
			blocker.getUseItem().hurtAndBreak((int) (damage * (source.isExplosion() ? 4f : 2f)), blocker,
					e -> e.broadcastBreakEvent(e.getUsedItemHand()));
			damage *= source.isExplosion() ? 0.3f : 0.45f;
		}

		boolean invPeriod = entity.invulnerableTime > (entity instanceof LivingEntity
				? ((LivingEntity) entity).invulnerableDuration / 2f
						: 10f);

		if (TF2ConfigVars.batchDamage == 1
				&& world.getCapability(TF2weapons.WORLD_CAP).orElse(null).damage.containsKey(entity)) {
			damage += world.getCapability(TF2weapons.WORLD_CAP).orElse(null).damage.get(entity).damage;
			world.getCapability(TF2weapons.WORLD_CAP).orElse(null).damage.remove(entity);
		}

		if (knockback && !(invPeriod && TF2ConfigVars.batchDamage == 1)) {
			return dealDamageActual(entity, world, living, stack, critical, damage, source);
		} else if (knockback && invPeriod && TF2ConfigVars.batchDamage == 1) {
			InboundDamage inbound = world.getCapability(TF2weapons.WORLD_CAP).orElse(null).damage.get(entity);
			if (inbound != null) {
				damage += inbound.damage;
			}
			world.getCapability(TF2weapons.WORLD_CAP).orElse(null).damage.put(entity,
					new InboundDamage(source, damage, critical, living, stack));
			return true;
		}
		return false;
	}

	public static boolean dealDamageActual(Entity entity, Level world, LivingEntity living, ItemStack stack,
			int critical, float damage, DamageSource source) {
		double lvelocityX = entity.getDeltaMovement().x;
		double lvelocityY = entity.getDeltaMovement().y;
		double lvelocityZ = entity.getDeltaMovement().z;
		boolean lairborne = entity.hasImpulse;
		if (entity instanceof PartEntity) {
			Entity parent = (Entity) ((PartEntity<?>) entity).getParent();
			lairborne = parent.hasImpulse;
			lvelocityX = parent.getDeltaMovement().x;
			lvelocityY = parent.getDeltaMovement().y;
			lvelocityZ = parent.getDeltaMovement().z;
		}

		float prehealth = entity instanceof LivingEntity
				? ((LivingEntity) entity).getHealth() + ((LivingEntity) entity).getAbsorptionAmount()
						: 0f;

		if (entity.hurt(source, damage)) {
			if (source instanceof TF2DamageSource && !((TF2DamageSource) source).getWeaponOrig().isEmpty())
				stack = ((TF2DamageSource) source).getWeaponOrig();
			if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon)
				((ItemWeapon) stack.getItem()).onDealDamage(stack, living, entity, source,
						entity instanceof LivingEntity ? prehealth - (((LivingEntity) entity).getHealth()
								+ ((LivingEntity) entity).getAbsorptionAmount()) : 0f);

			if (entity instanceof LivingEntity) {
				if (living instanceof Player) {
					TF2PlayerCapability.get((Player) living).addLastDamage(damage,
							entity instanceof Player);
				}
				LivingEntity livingTarget = (LivingEntity) entity;

				if (living instanceof EntityTF2Character && ((EntityTF2Character) living).isSharing()) {
					// Lets the mercenary's owner get player-only drops and XP from this kill.
					if (((EntityTF2Character) living).getOwner() instanceof Player)
						livingTarget.setLastHurtByPlayer((Player) ((EntityTF2Character) living).getOwner());
				}
				livingTarget.invulnerableTime = 20;
				if (TF2Attribute.getModifier("Silent Kill", stack, 0, living) == 0) {
					if (critical == 2)
						TF2Util.playSound(entity, TF2Sounds.MISC_CRIT, 1.5F,
								1.2F / (world.random.nextFloat() * 0.2F + 0.9F));
					else if (critical == 1)
						TF2Util.playSound(entity, TF2Sounds.MISC_MINI_CRIT, 1.5F,
								1.2F / (world.random.nextFloat() * 0.2F + 0.9F));
					if (!(entity instanceof EntityBuilding))
						TF2Util.playSound(entity, TF2Sounds.MISC_PAIN, 1F,
								1.2F / (world.random.nextFloat() * 0.2F + 0.9F));
				}
				livingTarget.hasImpulse = false;
				livingTarget.setDeltaMovement((lvelocityX), livingTarget.getDeltaMovement().y, livingTarget.getDeltaMovement().z);
				livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().x, (lvelocityY), livingTarget.getDeltaMovement().z);
				livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().x, livingTarget.getDeltaMovement().y, (lvelocityZ));
				livingTarget.hurtMarked = false;
			}
			if (entity instanceof PartEntity) {
				Entity parent = (Entity) ((PartEntity<?>) entity).getParent();
				parent.hasImpulse = lairborne;
				parent.setDeltaMovement((lvelocityX), parent.getDeltaMovement().y, parent.getDeltaMovement().z);
				parent.setDeltaMovement(parent.getDeltaMovement().x, (lvelocityY), parent.getDeltaMovement().z);
				parent.setDeltaMovement(parent.getDeltaMovement().x, parent.getDeltaMovement().y, (lvelocityZ));
				parent.hurtMarked = false;
			}
			return true;
		}

		if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon) {
			((ItemWeapon) stack.getItem()).onHitFinal(stack, living, entity, source);
		}
		return false;
	}

	public static float damageBlock(BlockPos pos, LivingEntity living, Level world, ItemStack stack, int critical,
			float damage, Vec3 forwardVec, Explosion explosion) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.isAir(state, world, pos) || TF2ConfigVars.destTerrain == 0 || state.getDestroySpeed(world, pos) < 0
				|| (!(living instanceof Player) && !world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
				|| (living instanceof Player && !world.isBlockModifiable((Player) living, pos)))
			return 0;

		DestroyBlockEntry finalEntry = null;
		int entryId = 0;
		int emptyId = -1;
		TF2WorldStorage cap = world.getCapability(TF2weapons.WORLD_CAP).orElse(null);

		for (int i = 0; i < cap.destroyProgress.size(); i++) {
			DestroyBlockEntry entry = cap.destroyProgress.get(i);
			if (emptyId == -1 && entry == null)
				emptyId = i;
			if (entry != null && entry.level == world && entry.pos.equals(pos)) {
				finalEntry = entry;
				entryId = i;
				break;
			}
		}
		if (finalEntry == null) {
			finalEntry = new DestroyBlockEntry(pos, world);
			if (emptyId != -1) {
				cap.destroyProgress.set(emptyId, finalEntry);
				entryId = emptyId;
			} else {
				cap.destroyProgress.add(finalEntry);
				entryId = cap.destroyProgress.size() - 1;
			}

		}

		/*
		 * if (block instanceof BlockChest) { ((TileEntityChest)
		 * world.getTileEntity(pos)).setLootTable(LootTableList.CHESTS_NETHER_BRIDGE,
		 * living.getRNG().nextLong()); }
		 */
		float hardness = TF2Util.getHardness(state, world, pos);

		if (!stack.isEmpty() && stack.getItem() instanceof ItemSniperRifle && hardness > 100)
			damage *= 3;
		finalEntry.curDamage += damage;

		if (living != null)
			world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE, 0xFFFF + entryId), pos,
					(int) ((finalEntry.curDamage / hardness) * 10));

		if (finalEntry.curDamage >= hardness) {
			if (living != null && living instanceof Player)
				block.harvestBlock(world, (Player) living, pos, state, null, stack);
			else {
				block.dropBlockAsItem(world, pos, state, (int) TF2Attribute.getModifier("Looting", stack, 0, living));
				if (explosion != null) block.onBlockExploded(world, pos, explosion);
			}
			cap.destroyProgress.remove(finalEntry);

			boolean flag = (living instanceof Player) ? (world.isEmptyBlock(pos)) && block.removedByPlayer(state, world, pos, (Player) living, true) : true;

			if (flag) {
				if (living != null) {
					world.levelEvent(2001, pos, Block.getStateId(state));
					world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE, 0xFFFF + entryId), pos, -1);
				}
				block.onBlockDestroyedByPlayer(world, pos, state);

				if (forwardVec != null) {
					HitResult trace = world.rayTraceBlocks(
							living.position().add(0, living.getEyeHeight(), 0), forwardVec, false, true,
							false);
					if (trace != null)
						damageBlock(trace.getBlockPos(), living, world, stack, critical,
								finalEntry.curDamage - hardness, forwardVec, explosion);
				}
			}
			return finalEntry.curDamage - hardness;
		}
		return 0;
	}

	public static float getHardness(BlockState state, Level world, BlockPos pos) {
		return state.getDestroySpeed(world, pos)
				* (!state.getMaterial().isToolNotRequired() && !(state.getBlock() instanceof BlockStone) ? 12f : 5.5f);
	}

	public static int getExperiencePoints(Player player) {
		InventoryWearables wearables = player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null);

		int totalMoney = 0;
		for (int i = 5; i < 8; i++) {
			ItemStack stack = wearables.getStackInSlot(i);
			if (stack.getItem() instanceof ItemMoney) {
				totalMoney += ((ItemMoney) stack.getItem()).getValue(wearables.getStackInSlot(i));
			}
		}
		return totalMoney;
		/*
		 * int playerLevel = player.experienceLevel; player.experienceLevel = 0; int
		 * totalExp = 0; for (int i = 0; i < playerLevel; i++) { player.experienceLevel
		 * = i; totalExp += player.xpBarCap(); } player.experienceLevel = playerLevel;
		 * totalExp += Math.round(player.experience * player.xpBarCap()); return
		 * totalExp;
		 */
	}

	public static void setExperiencePoints(Player player, int amount) {
		InventoryWearables wearables = player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null);

		int big = amount / 81;
		if (big > 64) {
			ItemHandlerHelper.giveItemToPlayer(player, TF2Util.metaStack(TF2weapons.itemMoney, big - 64, 2));
		}
		int medium = (amount % 81) / 9;
		int small = amount % 9;

		wearables.setInventorySlotContents(5, TF2Util.metaStack(TF2weapons.itemMoney, small, 0));
		wearables.setInventorySlotContents(6, TF2Util.metaStack(TF2weapons.itemMoney, medium, 1));
		wearables.setInventorySlotContents(7, TF2Util.metaStack(TF2weapons.itemMoney, big, 2));
	}

	public static double getDistanceSqBox(Entity target, double x, double y, double z, double widthO, double heightO) {
		double xdiff = target.getX() - x;
		double ydiff = target.getY() + target.getBbHeight() / 2 - y - heightO / 2;
		double zdiff = target.getZ() - z;
		double widthCom = (target.getBbWidth() + widthO) / 2;
		double heightCom = (target.getBbHeight() + heightO) / 2;
		if (Math.abs(xdiff) - widthCom < 0)
			xdiff = 0;
		else
			xdiff = xdiff > 0 ? xdiff - widthCom : xdiff + widthCom;

			if (Math.abs(zdiff) - widthCom < 0)
				zdiff = 0;
			else
				zdiff = zdiff > 0 ? zdiff - widthCom : zdiff + widthCom;

				if (Math.abs(ydiff) - heightCom < 0)
					ydiff = 0;
				else
					ydiff = ydiff > 0 ? ydiff - target.getBbHeight() / 2 : ydiff + target.getBbHeight() / 2;
					return xdiff * xdiff + zdiff * zdiff + ydiff * ydiff;
	}

	public static double getDistanceBox(Entity target, double x, double y, double z, double widthO, double heightO) {
		return Math.sqrt(getDistanceSqBox(target, x, y, z, widthO, heightO));
	}

	public static boolean canInteract(LivingEntity entity) {
		return !(entity instanceof Player && ((Player) entity).isSpectator())
				&& entity.getEffect(TF2weapons.stun) == null
				&& (!entity.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
						|| (WeaponsCapability.get(entity).invisTicks == 0 && !WeaponsCapability.get(entity).isFeign()))
				&& entity.getEffect(TF2weapons.bonk) == null;
	}

	public static void explosion(Level world, LivingEntity shooter, ItemStack weapon, Entity exploder,
			Entity direct, double x, double y, double z, float size, float damageMult, int critical, float distance) {
		float blockDmg = TF2Attribute.getModifier("Destroy Block", weapon, 0, shooter)
				* calculateDamage(TF2weapons.dummyEnt, world, shooter, weapon, critical, distance);

		if (blockDmg > 0 && TF2Attribute.getModifier("Onyx Projectile", weapon, 0, shooter) != 0)
			blockDmg *= TF2Attribute.getModifier("Onyx Projectile", weapon, 0, shooter);

		SoundEvent sound;

		if (exploder instanceof EntityProjectileBase) {
			sound = ((EntityProjectileBase) exploder).getExplosionSound();
		} else {
			sound = ItemFromData.getSound(weapon, PropertyType.EXPLOSION_SOUND);
		}

		TF2Explosion explosion = new TF2Explosion(world, exploder, x, y, z, size, direct, blockDmg, 1, sound);

		// System.out.println("ticks: "+this.ticksExisted);
		explosion.isFlaming = false;
		explosion.isSmoking = true;
		explosion.doExplosionA();
		explosion.doExplosionB(true);
		int killedInRow = 0;

		for (Entry<Entity, Float> entry : explosion.affectedEntities.entrySet()) {
			Entity ent = entry.getKey();

			int criticalloc = critical;
			if (exploder instanceof EntityProjectileBase) {
				if (((EntityProjectileBase) exploder).hitEntities.contains(ent)) {
					criticalloc = Math.max(criticalloc, 1);
					TF2Util.playSound(ent, TF2Sounds.DOUBLE_DONK, 3f, 1.0f);
					entry.setValue(1f);
				}
				distance = ((EntityProjectileBase) exploder).getDistanceToTarget(ent, ent.getX(), ent.getY(), ent.getZ());
			} else
				distance = (float) getDistanceBox(shooter, ent.getX(), ent.getY(), ent.getZ(), ent.getBbWidth() + 0.1,
						ent.getBbHeight() + 0.1);

			boolean fromSentry = exploder instanceof EntityProjectileBase
					&& ((EntityProjectileBase) exploder).sentry != null && ent instanceof LivingEntity;
			DamageSource source;
			if (TF2Attribute.getModifier("Unblockable", weapon, 0, shooter) == 1)
				source = TF2Util.causeDirectDamage(weapon, shooter);
			else {
				source = TF2Util.causeBulletDamage(weapon, shooter,
						fromSentry ? ((EntityProjectileBase) exploder).sentry : exploder);
			}

			criticalloc = calculateCritPost(ent, shooter, criticalloc, weapon, source);
			((TF2DamageSource) source).setCritical(criticalloc);
			float dmg = calculateDamage(ent, world, shooter, weapon, criticalloc, distance) * damageMult;

			if (ent instanceof EndCrystal && shooter != null && shooter.getRandom().nextFloat() > dmg / 15f) {
				continue;
			}

			Vec3 vec = explosion.getKnockbackMap().get(ent);
			if (vec != null) {

				boolean expJump = ent == shooter;
				double scale = dmg;
				if (expJump) {
					TF2Class used = TF2Util.getWeaponUsedByClass(weapon);
					if (used != null)
						scale *= ItemToken.EXPLOSION_VALUES[used.get3DDataValue()];
				} else {
					if (ent instanceof LivingEntity)
						scale *= 1 - ((LivingEntity) ent)
						.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
				}

				scale = Math.min(7.5, scale);
				if (expJump)
					scale = TF2Attribute.getModifier("Self Push Force", weapon, (float) scale, shooter);
				vec = vec.scale(scale * TF2ConfigVars.explosionKnockback);
				if (ent.getDeltaMovement().y != 0)
					ent.fallDistance = (float) Math.max(0f, ent.fallDistance * ((ent.getDeltaMovement().y + vec.y) / ent.getDeltaMovement().y));
				if (vec.y > 0) {
					ent.onGround = false;
					if (expJump) {
						// ent.fallDistance -= vec.y * 8 - 1;
					} else
						ent.fallDistance -= vec.y * 3 - 1;
				}
				ent.push(vec.x, vec.y, vec.z);
				// System.out.println("Explosion vec "+vec.lengthVector() * 20 + " "+
				// Math.sqrt(vec.x * vec.x + vec.z * vec.z) * 20 + " " + dmg *
				// entry.getValue());
				explosion.getKnockbackMap().put(ent, vec);
				/*
				 * if (ent instanceof EntityPlayer) ent.addVelocity(vec.x*3, 0, vec.y*3);
				 */

			}
			if (ent == shooter) {
				dmg = TF2Attribute.getModifier("Self Damage", weapon, dmg, shooter);
				if (shooter.getItemBySlot(EquipmentSlot.FEET).getItem() == TF2weapons.itemGunboats)
					dmg *= 0.4f;
			}

			if (fromSentry) {
				if (((EntityProjectileBase) exploder).sentry.fromPDA)
					((TF2DamageSource) source).addAttackFlag(TF2DamageSource.SENTRY_PDA);
			}
			dealDamage(ent, world, shooter, weapon, criticalloc, entry.getValue() * dmg, source.setExplosion());

			if (ent.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
				ent.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setExpJump(true);
			}
			/*
			 * if (critical == 2 && !ent.isEntityAlive() && ent instanceof EntityLivingBase)
			 * { killedInRow++; if (killedInRow > 2 && exploder instanceof EntityRocket &&
			 * shooter instanceof EntityPlayerMP && TF2weapons.isEnemy(shooter,
			 * (EntityLivingBase) ent)) { ((EntityPlayerMP)
			 * shooter).awardStat(TF2Achievements.CRIT_ROCKET_KILL); } }
			 */
			if (fromSentry) {
				EntitySentry sentry = ((EntityProjectileBase) exploder).sentry;
				((LivingEntity) ent).setLastAttackedEntity(sentry);
				((LivingEntity) ent).setLastHurtByMob(sentry);

				if (!ent.isAlive())
					sentry.scoreKill((LivingEntity) ent);
			}

		}
		Iterator<Player> iterator = world.players().iterator();

		while (iterator.hasNext()) {
			Player entityplayer = iterator.next();

			if (entityplayer.distanceToSqr(x, y, z) < 4096.0D && explosion.getKnockbackMap().containsKey(entityplayer))
				TF2weapons.network.sendTo(
						new TF2Message.VelocityAddMessage(explosion.getKnockbackMap().get(entityplayer), true),
						(ServerPlayer) entityplayer);
			/*
			 * ((EntityPlayerMP) entityplayer).connection .sendPacket(new
			 * SPacketExplosion(x, y, z, size, explosion.affectedBlockPositions,
			 * explosion.getKnockbackMap().get(entityplayer)));
			 */
		}
	}

	public static int calculateCritPre(ItemStack stack, LivingEntity living) {
		int thisCritical = 0;
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
		if (living.getEffect(TF2weapons.crit) != null
				|| living.getEffect(TF2weapons.buffbanner) != null)
			thisCritical = 1;
		if (thisCritical == 0 && (cap.focusedShot(stack) || cap.focusShotRemaining > 0))
			thisCritical = 1;
		if (thisCritical == 0 && !living.isOnGround()
				&& TF2Attribute.getModifier("Minicrit Airborne Self", stack, 0, living) == 1f)
			thisCritical = 1;
		if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon) {
			ItemWeapon item = (ItemWeapon) stack.getItem();
			if ((!item.rapidFireCrits(stack) && item.hasRandomCrits(stack, living)
					&& living.getRandom().nextFloat() <= item.critChance(stack, living)) || cap.getCritTime() > 0)
				thisCritical = 2;
		}
		if (living.getEffect(TF2weapons.critBoost) != null)
			thisCritical = 2;
		if (cap.hitNoMiss > 0 && cap.hitNoMiss + 1 >= TF2Attribute.getModifier("Hit Crit", stack, 0, living)) {
			thisCritical = 2;
		}
		if (!stack.isEmpty() && (!(stack.getItem() instanceof ItemWeapon) || stack.getItem() instanceof ItemMeleeWeapon)
				&& (living.getEffect(TF2weapons.charging) != null || cap.ticksBash > 0))
			if (thisCritical < 2 && (cap.ticksBash > 0 && cap.bashCritical)
					|| (living.getEffect(TF2weapons.charging) != null
					&& living.getEffect(TF2weapons.charging).getDuration() < 14))
				thisCritical = 2;
			else if (thisCritical == 0
					&& (cap.ticksBash > 0 || living.getEffect(TF2weapons.charging).getDuration() < 35))
				thisCritical = 1;
		return thisCritical;
	}

	public static DamageSourceProjectile causeBulletDamage(ItemStack weapon, Entity shooter, Entity projectile) {
		return (DamageSourceProjectile) (new DamageSourceProjectile(weapon, projectile, shooter)).setProjectile();
	}

	public static DamageSourceDirect causeDirectDamage(ItemStack weapon, Entity shooter) {
		return (new DamageSourceDirect(weapon, shooter));
	}

	public static Vec3 radiusRandom3D(float radius, RandomSource random) {
		double x, y, z;
		double radius2 = radius * radius;
		do {
			x = random.nextDouble() * radius * 2 - radius;
			y = random.nextDouble() * radius * 2 - radius;
			z = random.nextDouble() * radius * 2 - radius;
		} while (x * x + y * y + z * z > radius2);
		return new Vec3(x, y, z);

	}

	public static Vec3 rangeRandom3D(float radius, RandomSource random) {
		float yaw = random.nextFloat() * 2f * (float) Math.PI;
		float y = random.nextFloat() * 2f - 1f;
		float x = Mth.cos(yaw) * Mth.cos(asin(y));
		float z = Mth.sin(yaw) * Mth.cos(asin(y));

		return new Vec3(x * radius, y * radius, z * radius);
	}

	public static float asin(float value) {
		if (value >= 0)
			return ASIN_VALUES[(int) (value * 511f)];
		else
			return -ASIN_VALUES[(int) (-value * 511f)];
	}

	public static void stun(LivingEntity living, int duration, boolean noMovement) {
		if (!(living instanceof Player && ((Player) living).getAbilities().instabuild)) {
			living.addEffect(new MobEffectInstance(TF2weapons.stun, duration, noMovement ? 1 : 0));
			living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
		}
	}

	public static void playSound(Entity entity, SoundEvent event, float volume, float pitch) {
		entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), event, entity.getSoundSource(), volume,
				pitch);
	}

	public static boolean isEnemy(LivingEntity living, LivingEntity living2) {
		return (living2 instanceof Enemy || living2 instanceof Player || (living2 instanceof Mob
				&& !(living2 instanceof EntityBuilding) && ((Mob) living2).getTarget() == living))
				&& !isOnSameTeam(living, living2);
	}

	public static void igniteAndAchievement(Entity target, LivingEntity living, int sec, float upgrade) {

		/*
		 * if (living instanceof EntityPlayerMP) { if
		 * (target.hasCapability(TF2weapons.WEAPONS_CAP, null) &&
		 * target.getDataManager().get(TF2EventsCommon.ENTITY_EXP_JUMP)) {
		 * ((EntityPlayer) living).awardStat(TF2Achievements.PILOT_LIGHT); } if
		 * (ItemFromData.isSameType(living.getHeldItemMainhand(), "flaregun") &&
		 * !target.isBurning()) { ((EntityPlayer)
		 * living).awardStat(TF2Achievements.FLAREGUN_IGNITED); if (((EntityPlayerMP)
		 * living).getStatFile().readStat(TF2Achievements.FLAREGUN_IGNITED) >= 100)
		 * ((EntityPlayer) living).awardStat(TF2Achievements.ATTENTION_GETTER); } }
		 */

		try {
			int fire = target.getRemainingFireTicks();
			if (target instanceof LivingEntity) {
				upgrade *= TF2Attribute.getModifier("Afterburn Reduction",
						ItemBackpack.getBackpack((LivingEntity) target), 1, living);
				fire *= 40f / ProtectionEnchantment.getFireTimeForEntity((LivingEntity) target, 40);
			}

			if (fire <= 0)
				target.setSecondsOnFire(1 + Mth.ceil(sec * upgrade));

			else if (fire < 20 * Mth.ceil(8 * upgrade) + 20) {

				target.setSecondsOnFire(Math.min(Mth.floor(fire / 20f) + Math.round(sec * upgrade),
						Mth.ceil(8 * upgrade)));
				target.setRemainingFireTicks(target.getRemainingFireTicks() + fire % 20);

			}
			// ReflectionAccess.entityFire.setInt(target, Math.min(fire +
			// MathHelper.ceil(sec * upgrade) * 20, 20 * MathHelper.ceil(8 * upgrade) + fire
			// % 20));
			// fire = target.getRemainingFireTicks();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void sendParticle(EnumTF2Particles type, Entity tracking, double x, double y, double z,
			double offsetX, double offsetY, double offsetZ, int count, int... params) {
		sendTracking(
				new TF2Message.ParticleSpawnMessage(type.ordinal(), x, y, z, offsetX, offsetY, offsetZ, count, params),
				tracking);
	}

	public static void sendParticle(EnumTF2Particles type, Level world, double x, double y, double z, double offsetX,
			double offsetY, double offsetZ, int count, int... params) {
		TF2weapons.network.sendToAllAround(
				new TF2Message.ParticleSpawnMessage(type.ordinal(), x, y, z, offsetX, offsetY, offsetZ, count, params),
				new TargetPoint(world.dimension(), x, y, z, type.getRange()));
	}

	public static void sendTracking(IMessage message, Entity entity) {
		if (entity instanceof ServerPlayer)
			TF2weapons.network.sendTo(message, (ServerPlayer) entity);
		for (Player player : ((ServerLevel) entity.level).getEntityTracker().getTrackingPlayers(entity)) {
			TF2weapons.network.sendTo(message, (ServerPlayer) player);
		}
	}

	public static void sendTrackingExcluding(IMessage message, Entity entity) {
		for (Player player : ((ServerLevel) entity.level).getEntityTracker().getTrackingPlayers(entity)) {
			TF2weapons.network.sendTo(message, (ServerPlayer) player);
		}
	}

	/*
	 * public static TargetPoint pointFromEntity(Entity entity) { return new
	 * TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 256); }
	 */

	public static float getDamageBeforeAbsorb(float damage, float totalArmor, float toughnessAttribute) {
		float f = 2.0F + toughnessAttribute / 4.0F;
		float f1 = Mth.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
		return damage * (1.0F - f1 / 25.0F);
	}

	public static boolean isOre(String ore, ItemStack stack) {
		for (ItemStack stackin : OreDictionary.getOres(ore)) {
			if (stackin.sameItem(stack))
				return true;
		}
		return false;
	}

	public static ItemStack getFirstItem(Container inventory, Predicate<ItemStack> pred) {

		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && pred.test(stack))
				return stack;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getFirstItem(IItemHandler inventory, Predicate<ItemStack> pred) {

		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty() && pred.test(stack))
				return stack;
		}
		return ItemStack.EMPTY;
	}

	public static int getFirstItemSlot(Container inventory, Predicate<ItemStack> pred) {

		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && pred.test(stack))
				return i;
		}
		return -1;
	}

	public static int getFirstItemSlot(IItemHandler inventory, Predicate<ItemStack> pred) {

		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty() && pred.test(stack))
				return i;
		}
		return -1;
	}

	public static ItemStack getBestItem(Container inventory, Comparator<ItemStack> comp, Predicate<ItemStack> pred) {
		ItemStack greater = ItemStack.EMPTY;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if ((pred == null || pred.test(stack)) && comp.compare(stack, greater) > 0)
				greater = stack;
		}
		return greater;
	}

	public static ItemStack getBestItem(IItemHandler inventory, Comparator<ItemStack> comp, Predicate<ItemStack> pred) {
		ItemStack greater = ItemStack.EMPTY;
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if ((pred == null || pred.test(stack)) && comp.compare(stack, greater) > 0)
				greater = stack;
		}
		return greater;
	}

	public static ItemStack mergeStackByDamage(IItemHandler inventory, ItemStack stack) {
		if (stack.isEmpty() || stack.getCount() > 1 || !(stack.getItem() instanceof ItemFireAmmo))
			return stack;
		ItemStack existingAmmo;
		int amount = 0;
		while (amount < ((ItemAmmo) stack.getItem()).getAmount(stack)
				&& !(existingAmmo = TF2Util
				.getFirstItem(inventory,
						stackL -> stackL.getCount() == 1 && stackL.getItem() == stack.getItem()
						&& ((ItemFireAmmo) stackL.getItem())
						.getAmount(stackL) != ((ItemFireAmmo) stackL.getItem()).uses))
				.isEmpty()) {
			amount += ((ItemFireAmmo) existingAmmo.getItem()).restoreAmmo(existingAmmo,
					((ItemAmmo) stack.getItem()).getAmount(stack));
			if (((ItemFireAmmo) existingAmmo.getItem())
					.getAmount(existingAmmo) == ((ItemFireAmmo) existingAmmo.getItem()).uses) {
				ItemStack copy = existingAmmo.copy();
				existingAmmo.setCount(0);
				ItemHandlerHelper.insertItemStacked(inventory, copy, false);
			}
		}
		((ItemAmmo) stack.getItem()).consumeAmmo(null, stack, amount);
		return stack;
	}

	public static boolean hasEnoughItem(Container inventory, Predicate<ItemStack> pred, int amount) {
		int count = 0;
		if (amount <= 0)
			return true;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && pred.test(stack)) {
				count += stack.getCount();
				if (count >= amount)
					return true;
			}

		}
		return count >= amount;
	}

	public static boolean hasEnoughItem(IItemHandler inventory, Predicate<ItemStack> pred, int amount) {
		int count = 0;
		if (amount <= 0)
			return true;
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty() && pred.test(stack)) {
				count += stack.getCount();
				if (count >= amount)
					return true;
			}

		}
		return count >= amount;
	}

	public static int removeItemsMatching(IItemHandler inventory, int amount, Predicate<ItemStack> match) {
		int left = amount;
		for (int i = 0; i < inventory.getSlots(); i++) {
			if (match.apply(inventory.getStackInSlot(i))) {
				left = left - inventory.extractItem(i, left, false).getCount();
				if (left <= 0)
					return 0;
			}
		}
		return left;
	}

	public static void attractMobs(LivingEntity living, Level world) {
		if (!world.isClientSide && TF2ConfigVars.shootAttract && world.getDifficulty().getId() > 1) {

			int range = world.getDifficulty() == Difficulty.HARD ? 60 : 38;
			for (PathfinderMob mob : world.getEntitiesOfClass(PathfinderMob.class,
					living.getBoundingBox().inflate(range, range, range),
					input -> input.getTarget() == null && input.canChangeDimensions() && TF2Util.isHostile(input))) {
				mob.getLookControl().setLookAt(living, 60, 30);
				if (!TF2Util.isAlliedTo(living, mob)) {
					if (mob.getSensing().hasLineOfSight(living) || mob.distanceToSqr(living) < 150) {
						mob.setTarget(living);
						if (mob.getTarget() != null
								&& mob.getAttribute(Attributes.FOLLOW_RANGE)
								.getModifier(FOLLOW_MODIFIER) == null) {
							mob.getAttribute(Attributes.FOLLOW_RANGE)
							.addTransientModifier(new AttributeModifier(FOLLOW_MODIFIER, "Follow Check",
									65 - mob.getAttribute(Attributes.FOLLOW_RANGE)
									.getValue(), AttributeModifier.Operation.fromValue(0)));
							mob.getPersistentData().putInt("TF2AM", (int) world.getGameTime());
						}
						// mob.getNavigator().tryMoveToEntityLiving(living, 1.1f);

					}

					// CoroUtilPath.tryMoveToEntityLivingLongDist((EntityCreature)mob,
					// living, 1.1D);
					;
				}

			}
		}
	}

	public static void addModifierSafe(LivingEntity living, Attribute attribute, AttributeModifier modifier,
			boolean saved) {
		living.getAttribute(attribute).removeModifier(modifier.getId());
		living.getAttribute(attribute).addTransientModifier(modifier);
		living.getAttribute(attribute).getValue();
		if (!saved)
			modifier.setSaved(false);
	}

	public static boolean isHostile(LivingEntity living) {
		return living instanceof Enemy && !TF2ConfigVars.hostileBlacklist.contains(EntityList.getKey(living));
	}

	public static Vec3 getRotationVector(float pitch, float yaw) {
		float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f2 = -Mth.cos(-pitch * 0.017453292F);
		float f3 = Mth.sin(-pitch * 0.017453292F);
		return new Vec3(f1 * f2, f3, f * f2);
	}

	public static double getYaw(double x, double z) {
		return Mth.atan2(x, z) * 180.0D / Math.PI;
	}

	public static boolean isBaseSame(CompoundTag base, CompoundTag second) {
		if (base != null)
			for (String key : base.getAllKeys()) {
				if (!base.get(key).equals(second.get(key)))
					return false;
			}
		return true;
	}

	public static Vec3 getMovementVector(LivingEntity living) {
		Vec3 moveDir = new Vec3(living.zza, living.xxa, 0).normalize();
		float cos = Mth.cos(living.getYRot() * 0.017453292F - (float) Math.PI);
		float sin = Mth.sin(living.getYRot() * 0.017453292F - (float) Math.PI);
		return new Vec3(-moveDir.y * cos + moveDir.x * sin, -moveDir.x * cos - moveDir.y * sin, 0);
	}

	public static boolean teleportSafe(Mob toTeleport, Entity dest) {
		int i = Mth.floor(dest.getX()) - 2;
		int j = Mth.floor(dest.getZ()) - 2;
		int k = Mth.floor(dest.getBoundingBox().minY);

		for (int l = 0; l <= 4; ++l) {
			for (int i1 = 0; i1 <= 4; ++i1) {
				if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && isTeleportFriendlyBlock(toTeleport, i, j, k, l, i1)) {
					toTeleport.moveTo(i + l + 0.5F, k, j + i1 + 0.5F, toTeleport.getYRot(),
							toTeleport.getXRot());
					toTeleport.getNavigation().stop();
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isTeleportFriendlyBlock(LivingEntity owner, int x, int p_192381_2_, int y,
			int p_192381_4_, int p_192381_5_) {
		BlockPos blockpos = new BlockPos(x + p_192381_4_, y - 1, p_192381_2_ + p_192381_5_);
		BlockState iblockstate = owner.level.getBlockState(blockpos);
		return iblockstate.getBlockFaceShape(owner.level, blockpos, Direction.DOWN) == BlockFaceShape.SOLID
				&& iblockstate.canEntitySpawn(owner) && owner.level.isEmptyBlock(blockpos.above())
				&& owner.level.isEmptyBlock(blockpos.above(2));
	}

	public static float getGravity(LivingEntity living) {
		if (living.isInWater() || living.isNoGravity() || living.isFallFlying() || living instanceof WitherBoss
				|| living instanceof EntityMonoculus || living instanceof Ghast)
			return 0;
		else
			return 0.08f;
	}

	public static void addAndSendEffect(LivingEntity living, MobEffectInstance effect) {
		living.addEffect(effect);
		if (!living.level.isClientSide)
			for (Player player : ((ServerLevel) living.level).getEntityTracker().getTrackingPlayers(living)) {
				((ServerPlayer) player).connection.send(new ClientboundUpdateMobEffectPacket(living.getId(), effect));
			}
	}

	public static Entity findAmmoSource(LivingEntity living, double range, boolean immediate) {
		return Iterables
				.getFirst(
						living.level
						.getEntitiesOfClass(EntityDispenser.class,
								living.getBoundingBox().inflate(range, range / 4D, range),
								input -> TF2Util.isAlliedTo(input, living)
								&& (!immediate || (!input.isDisabled() && input.getMetal() > 0))),
						null);
	}

	public static boolean isNaturalBlock(Level world, BlockPos pos, BlockState state) {
		Block block = state.getBlock();

		for (Block block2 : NATURAL_BLOCKS) {
			if (block == block2)
				return true;
		}

		Biome biome = world.getBiome(pos);
		return block == biome.topBlock.getBlock() || block == biome.fillerBlock.getBlock();
	}

	public static Vec2 getAngleFromFacing(Direction facing) {
		switch (facing) {
		case UP:
			return new Vec2(0, -90);
		case DOWN:
			return new Vec2(0, 90);
		default:
			return new Vec2(facing.toYRot(), 0);
		}
	}

	public static void stomp(LivingEntity living) {
		for (LivingEntity target : living.level.getEntitiesOfClass(LivingEntity.class,
				living.getBoundingBox().grow(0.25, -living.getDeltaMovement().y, 0.25),
				input -> input != living && !TF2Util.isAlliedTo(input, living))) {

			float damage = Math.max(0, living.fallDistance - 3) * 1.8f;
			living.fallDistance = 0;
			if (damage > 0) {
				target.hurt(new EntityDamageSource("fallpl", living), damage);
				TF2Util.playSound(living, TF2Sounds.WEAPON_MANTREADS, 1.5F, 1F);
			}
		}
	}

	public static void extractData(String input, File output, File source) {
		try {
			byte[] bytes = Resources
					.toByteArray(TF2Util.class.getResource("/assets/" + TF2weapons.MOD_ID + "/" + input));
			FileOutputStream ostream = new FileOutputStream(output);
			ostream.write(bytes);
			ostream.flush();
			ostream.close();
			/*
			 * if (source.isFile()) { ZipFile zip = new ZipFile(source); ZipEntry entry =
			 * zip.getEntry(input); if (entry != null) { long crc = entry.getCrc();
			 * InputStream zin = zip.getInputStream(entry); byte[] bytes = new byte[(int)
			 * entry.getSize()]; zin.read(bytes); FileOutputStream str = new
			 * FileOutputStream(output); str.write(bytes); str.flush(); str.close();
			 * zin.close();
			 *
			 * FileInputStream istr = new FileInputStream(output); byte[] bytesc = new
			 * byte[istr.available()]; istr.read(bytesc); CRC32 crc2 = new CRC32();
			 * crc2.update(bytesc); istr.close();
			 *
			 * if (crc2.getValue() != crc) { TF2weapons.corrupted = true; } } zip.close(); }
			 * else { File inputFile = new File(source, input); FileInputStream istr = new
			 * FileInputStream(inputFile);
			 *
			 * byte[] bytes = new byte[(int) inputFile.length()]; istr.read(bytes);
			 * FileOutputStream str = new FileOutputStream(output); str.write(bytes);
			 * str.flush(); str.close(); istr.close();
			 *
			 * FileInputStream istr2 = new FileInputStream(output); byte[] bytesc = new
			 * byte[istr2.available()]; istr2.read(bytesc); CRC32 crc2 = new CRC32();
			 * crc2.update(bytesc); TF2weapons.LOGGER.info("Value: "+crc2.getValue());
			 * istr2.close(); }
			 */
		} catch (IOException e) {
			TF2weapons.corrupted = true;
			e.printStackTrace();
		}

		if (TF2weapons.corrupted) {
			output.delete();
			TF2weapons.instance.weaponDir.delete();
			TF2weapons.proxy.displayCorruptedFileError();
		}
	}

	public static boolean isWeaponOfClass(ItemStack stack, int slot, TF2Class clazz) {
		if (ItemFromData.getData(stack).hasProperty(PropertyType.SLOT))
			return ItemFromData.isItemOfClassSlot(ItemFromData.getData(stack), slot, clazz);
		else {
			String parent = ItemFromData.getData(stack).getString(PropertyType.BASED_ON);
			if (!parent.isEmpty())
				return ItemFromData.isItemOfClassSlot(MapList.nameToData.get(parent), slot, clazz);
			else
				return false;

		}
	}

	public static TF2Class getWeaponUsedByClass(ItemStack stack) {
		if (ItemFromData.getData(stack) == ItemFromData.BLANK_DATA)
			return TF2Class.NONE;
		String parent = ItemFromData.getData(stack).getString(PropertyType.BASED_ON);
		WeaponData data;
		if (!parent.isEmpty() && MapList.nameToData.get(parent).hasProperty(PropertyType.SLOT))
			data = MapList.nameToData.get(parent);
		else
			data = ItemFromData.getData(stack);
		if (data.hasProperty(PropertyType.SLOT))
			return TF2Class.getClass(Iterables.getFirst(data.get(PropertyType.SLOT).keySet(), null));
		else
			return TF2Class.NONE;
	}

	public static LivingEntity getOwnerIfOwnable(LivingEntity living) {
		if (living instanceof OwnableEntity && ((OwnableEntity) living).getOwner() != null)
			return (LivingEntity) ((OwnableEntity) living).getOwner();
		else
			return living;
	}

	public static Vec3 getHeightVec(Level world, BlockPos pos) {
		/*
		 * EnumFacing facing = EnumFacing.getFacingFromVector((float)living.motionX, 0f,
		 * (float)living.motionZ); if (!living.world.isBlockFullCube(pos) &&
		 * living.world.isBlockFullCube(pos.down()) &&
		 * living.world.isBlockFullCube(pos.offset(facing)) &&
		 * !living.world.isBlockFullCube(pos.offset(facing).up())) { vec2 =
		 * vec2.addVector(0, 1, 0).normalize().scale(motiona);
		 */
		Vec3 vec = new Vec3(0, 0, 0);
		BlockState center = world.getBlockState(pos);
		BlockState down = world.getBlockState(pos.relative(Direction.DOWN));
		BlockState north = world.getBlockState(pos.relative(Direction.NORTH));
		BlockState south = world.getBlockState(pos.relative(Direction.SOUTH));
		BlockState west = world.getBlockState(pos.relative(Direction.WEST));
		BlockState east = world.getBlockState(pos.relative(Direction.EAST));
		if (center.getCollisionBoundingBox(world, pos) != null)
			vec = vec.add(0, 1 - center.getCollisionBoundingBox(world, pos).maxY, 0);
		else if (down.getCollisionBoundingBox(world, pos) != null)
			vec = vec.add(0, 2 - down.getCollisionBoundingBox(world, pos).maxY, 0);
		if (north.getCollisionBoundingBox(world, pos) != null)
			vec = vec.add(0, 0, north.getCollisionBoundingBox(world, pos).maxY);
		if (south.getCollisionBoundingBox(world, pos) != null)
			vec = vec.add(0, 0, -south.getCollisionBoundingBox(world, pos).maxY);
		if (west.getCollisionBoundingBox(world, pos) != null)
			vec = vec.add(west.getCollisionBoundingBox(world, pos).maxY, 0, 0);
		if (east.getCollisionBoundingBox(world, pos) != null)
			vec = vec.add(-east.getCollisionBoundingBox(world, pos).maxY, 0, 0);
		// vec = vec.normalize();
		return vec;
	}

	public static void setVelocity(Entity entity, double motionX, double motionY, double motionZ) {
		entity.setDeltaMovement((motionX), entity.getDeltaMovement().y, entity.getDeltaMovement().z);
		entity.setDeltaMovement(entity.getDeltaMovement().x, (motionY), entity.getDeltaMovement().z);
		entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, (motionZ));
		entity.hurtMarked = true;
		if (entity instanceof ServerPlayer) {
			((ServerPlayer) entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
		}
	}

	public static ItemStack pickAmmo(ItemStack stack, Player player, boolean addNormalInventory) {
		if (stack.getItem() instanceof ItemFireAmmo && stack.getCount() == 1) {
			stack = TF2Util.mergeStackByDamage(
					player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), stack);
			if (!player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3).isEmpty())
				stack = TF2Util.mergeStackByDamage(player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null)
						.getStackInSlot(3).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), stack);

			if (stack.isEmpty()) {
				return stack;
			}
		}
		if (stack.getItem() instanceof ItemAmmo && player.getCapability(TF2weapons.INVENTORY_CAP).isPresent()
				|| addNormalInventory) {

			if (!player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3).isEmpty()) {
				IItemHandler inv = player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3)
						.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
				stack = ItemHandlerHelper.insertItemStacked(inv, stack, false);
				/*
				 * if(stack.isEmpty()){ event.getItem().setItem(orig); }
				 */
				ItemStack weapon = player.getMainHandItem();
				if (!weapon.isEmpty() && weapon.getItem() instanceof ItemWeapon)
					TF2weapons.network.sendTo(
							new TF2Message.UseMessage(((ItemWeapon) weapon.getItem()).getClip(weapon), false,
									((ItemUsable) weapon.getItem()).getAmmoAmount(player, weapon), InteractionHand.MAIN_HAND),
							(ServerPlayer) player);
			}
			if (!stack.isEmpty() && addNormalInventory) {
				IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
				stack = ItemHandlerHelper.insertItemStacked(inv, stack, false);
				ItemStack weapon = player.getMainHandItem();
				if (!weapon.isEmpty() && weapon.getItem() instanceof ItemWeapon)
					TF2weapons.network.sendTo(
							new TF2Message.UseMessage(((ItemWeapon) weapon.getItem()).getClip(weapon), false,
									((ItemUsable) weapon.getItem()).getAmmoAmount(player, weapon), InteractionHand.MAIN_HAND),
							(ServerPlayer) player);
			}
			if (stack.isEmpty()) {
				return stack;
			}
		}
		return stack;
	}

	public static int getValueOnAxis(Vec3i vec, Axis axis) {
		switch (axis) {
		case X:
			return vec.getX();
		case Y:
			return vec.getY();
		case Z:
			return vec.getZ();
		default:
			return 0;
		}
	}

	public static BlockPos setValueOnAxis(Vec3i vec, Axis axis, int value) {
		switch (axis) {
		case X:
			return new BlockPos(value, vec.getY(), vec.getZ());
		case Y:
			return new BlockPos(vec.getX(), value, vec.getZ());
		case Z:
			return new BlockPos(vec.getX(), vec.getY(), value);
		default:
			return new BlockPos(vec);
		}
	}

	public static double getHeightAboveGround(Entity entity, Level world, boolean checkWater) {
		if (entity.isOnGround())
			return 0.;
		else
			return getHeightAboveGround(new Vec3(entity.getX(), entity.getBoundingBox().minY, entity.getZ()), world,
					checkWater);
	}

	public static double getHeightAboveGround(Vec3 pos, Level world, boolean checkWater) {
		HitResult ray = world.rayTraceBlocks(pos, pos.subtract(0., 256., 0.), checkWater);

		if (ray != null && ray.getLocation() != null)
			return pos.y - ray.getLocation().y;
		else
			return 0.;
	}

	public static float getReducedHealing(LivingEntity attacker, LivingEntity target, float damage) {
		return getDamageReduction(new DamageSource("").setProjectile(), attacker, 15f) / 15f * damage;
	}

	public static float getDamageReduction(DamageSource source, LivingEntity living, float damage) {
		return CombatRules
				.getDamageAfterMagicAbsorb(
						CombatRules.getDamageAfterAbsorb(damage, living.getArmorValue(),
								(float) living.getAttribute(Attributes.ARMOR_TOUGHNESS)
								.getValue()),
						EnchantmentHelper.getEnchantmentModifierDamage(living.getArmorSlots(), source));
	}

	public static int getTotalCurrency(IItemHandler handler) {
		int money = 0;
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (stack.getItem() instanceof ItemMoney) {
				money += ((ItemMoney) stack.getItem()).getValue(stack);
			}
		}
		return money;
	}

	public static void setTotalCurrency(IItemHandler handler, int fromSlot, int amount) {
		int big = amount / 81;
		int medium = (amount % 81) / 9;
		int small = amount % 9;

		handler.extractItem(fromSlot, 64, false);
		handler.insertItem(fromSlot, TF2Util.metaStack(TF2weapons.itemMoney, small, 0), false);
		handler.extractItem(fromSlot + 1, 64, false);
		handler.insertItem(fromSlot + 1, TF2Util.metaStack(TF2weapons.itemMoney, medium, 1), false);
		handler.extractItem(fromSlot + 2, 64, false);
		handler.insertItem(fromSlot + 2, TF2Util.metaStack(TF2weapons.itemMoney, big, 2), false);
	}

	public static IItemHandler getLoadoutItemHandler(LivingEntity living) {
		if (living instanceof Player) {
			IItemHandler handler = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
			return handler;
		} else if (living instanceof EntityTF2Character) {
			IItemHandler handler = ((EntityTF2Character) living).loadout;
			return handler;
		}
		return null;
	}

	public static void playSoundToPlayer(Player player, SoundEvent event, SoundSource category, double x,
			double y, double z, float volume, float pitch) {
		if (player instanceof ServerPlayer)
			((ServerPlayer) player).connection
			.send(new ClientboundSoundPacket(event, category, x, y, z, volume, pitch));
		else if (player.getLevel().isClientSide && player == Minecraft.getInstance().player)
			player.getLevel().playSound(x, y, z, event, category, volume, pitch, false);
	}

	public static boolean restoreAmmoToWeapons(Player player, float ammo) {
		boolean restored = false;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.getItem() instanceof ItemFromData) {
				int ammotype = ((ItemFromData) stack.getItem()).getAmmoType(stack);
				int ammocount = ItemFromData.getAmmoAmountType(player, ammotype);
				if (ammocount < ItemFromData.getData(stack).getInt(PropertyType.MAX_AMMO)) {
					int maxammo = Mth.ceil(ItemFromData.getData(stack).getInt(PropertyType.MAX_AMMO) * ammo);
					restored = true;
					TF2Util.pickAmmo(
							ItemAmmoPackage.getAmmoForType(ammotype, Math.min(
									ItemFromData.getData(stack).getInt(PropertyType.MAX_AMMO) - ammocount, maxammo)),
							player, true);
				}
			}
		}
		return restored;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> NBTTagListToList(Tag nbttag, Class<T> clazz) {
		List<T> list = new ArrayList<>();
		if (!(nbttag instanceof ListTag))
			return list;

		ListTag nbtlist = (ListTag) nbttag;
		if (clazz == Tag.class)
			addNBTBaseToList(nbtlist, (List<Tag>) list);
		if (nbtlist.getElementType() == 8)
			addStringToList(nbtlist, (List<String>) list);
		return list;
	}

	private static <T> void addNBTBaseToList(ListTag nbtlist, List<Tag> list) {
		for (int i = 0; i < nbtlist.size(); i++) {
			list.add(nbtlist.get(i));
		}
	}

	private static <T> void addStringToList(ListTag nbtlist, List<String> list) {
		for (int i = 0; i < nbtlist.size(); i++) {
			list.add(nbtlist.getString(i));
		}
	}

	static {
		for (int i = 0; i < 512; i++) {
			ASIN_VALUES[i] = (float) Math.asin(i / 511D);
		}
		for (int i = 0; i < 32; ++i) {
			int j = (i >> 3 & 1) * 85;
			int k = (i >> 2 & 1) * 170 + j;
			int l = (i >> 1 & 1) * 170 + j;
			int i1 = (i >> 0 & 1) * 170 + j;

			if (i == 6) {
				k += 85;
			}

			if (i >= 16) {
				k /= 4;
				l /= 4;
				i1 /= 4;
			}

			colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
		}
	}

	public static boolean matches(ItemStack stack1, ItemStack stack2) {
		return stack1.sameItem(stack2) && ItemStack.tagMatches(stack1, stack2) && stack1.getCount() <= stack2.getCount();
	}


	/* ------------------------------------------------------------------ */
	/* 1.12 -> 1.19 port helpers */
	/* ------------------------------------------------------------------ */

	/**
	 * Creates a stack of an item that used metadata subtypes in 1.12. The metadata is stored as the
	 * stack's damage value (see PORTING_NOTES.md).
	 */
	public static ItemStack metaStack(net.minecraft.world.level.ItemLike item, int count, int meta) {
		ItemStack stack = new ItemStack(item, count);
		if (meta != 0)
			stack.setDamageValue(meta);
		return stack;
	}

	/** The 1.12 metadata of a stack (stored as its damage value). */
	public static int getMeta(ItemStack stack) {
		return stack.getDamageValue();
	}

	/** Server-safe translation (1.12's deprecated server-side I18n). */
	public static String translate(String key, Object... args) {
		String format = net.minecraft.locale.Language.getInstance().getOrDefault(key);
		if (args.length == 0)
			return format;
		try {
			return String.format(format, args);
		} catch (java.util.IllegalFormatException e) {
			return format;
		}
	}

	public static boolean canTranslate(String key) {
		return net.minecraft.locale.Language.getInstance().has(key);
	}

	/** Converts 1.12 AI mutex bits (1 move, 2 look, 4 jump) to 1.19 goal flags. */
	public static EnumSet<net.minecraft.world.entity.ai.goal.Goal.Flag> mutexFlags(int bits) {
		EnumSet<net.minecraft.world.entity.ai.goal.Goal.Flag> flags = EnumSet
				.noneOf(net.minecraft.world.entity.ai.goal.Goal.Flag.class);
		if ((bits & 1) != 0)
			flags.add(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE);
		if ((bits & 2) != 0)
			flags.add(net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK);
		if ((bits & 4) != 0)
			flags.add(net.minecraft.world.entity.ai.goal.Goal.Flag.JUMP);
		if ((bits & 8) != 0)
			flags.add(net.minecraft.world.entity.ai.goal.Goal.Flag.TARGET);
		return flags;
	}

	/** Item in a hand slot (0 main, 1 off hand) without the equipment-change side effects. */
	public static ItemStack getHandStack(Entity entity, int hand) {
		if (!(entity instanceof LivingEntity))
			return ItemStack.EMPTY;
		return ((LivingEntity) entity).getItemBySlot(hand == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
	}

	/** Owner of a thrown entity or projectile, or null. */
	public static Entity getThrower(Entity entity) {
		if (entity instanceof EntityProjectileBase)
			return ((EntityProjectileBase) entity).getThrower();
		if (entity instanceof Projectile)
			return ((Projectile) entity).getOwner();
		return null;
	}

	/** Sets synced data and marks it for sending even if the new value equals the old one. */
	public static void forceSet(net.minecraft.network.syncher.SynchedEntityData data,
			net.minecraft.network.syncher.EntityDataAccessor<CompoundTag> key, CompoundTag value) {
		// Setting a different value first makes the data manager send the update.
		CompoundTag marker = new CompoundTag();
		marker.putBoolean("__resync", true);
		data.set(key, marker);
		data.set(key, value);
	}

	/** 1.12 World.getPlayerEntityByName. */
	public static Player getPlayerByName(Level level, String name) {
		for (Player player : level.players())
			if (name.equals(player.getGameProfile().getName()))
				return player;
		return null;
	}

	/** 1.12's onInitialSpawn(difficulty, data), for mobs spawned by the mod itself. */
	public static net.minecraft.world.entity.SpawnGroupData initialSpawn(net.minecraft.world.entity.Mob mob,
			net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.SpawnGroupData data) {
		if (!(mob.level instanceof net.minecraft.server.level.ServerLevel))
			return data;
		return mob.finalizeSpawn((net.minecraft.server.level.ServerLevel) mob.level, difficulty,
				net.minecraft.world.entity.MobSpawnType.EVENT, data, null);
	}

	/** 1.12's EntityItem.setThrower(name); null-safe for spawnAtLocation results. */
	public static void setThrower(@javax.annotation.Nullable net.minecraft.world.entity.item.ItemEntity item,
			@javax.annotation.Nullable java.util.UUID thrower) {
		if (item != null && thrower != null)
			item.setThrower(thrower);
	}
}
