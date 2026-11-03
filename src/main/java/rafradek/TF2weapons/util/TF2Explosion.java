package rafradek.TF2weapons.util;

import java.util.ArrayList;
import net.minecraft.server.level.ServerLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class TF2Explosion extends Explosion {

	private Random explosionRNG = new Random();
	public Level world;
	/** A list of ChunkPositions of blocks affected by this explosion */
	public List<BlockPos> affectedBlockPositions = new ArrayList<>();
	public HashMap<Entity, Float> affectedEntities = new HashMap<>();
	private Entity directHit;
	/** whether or not the explosion sets fire to blocks around it */
	public boolean isFlaming;
	/** whether or not this explosion spawns smoke particles */
	public boolean isSmoking;
	private final double explosionX;
	private final double explosionY;
	private final double explosionZ;
	private final Entity exploder;
	private final float explosionSize;
	private final Map<Entity, Vec3> knockbackMap;
	private final Vec3 position;
	private float harvestDamage;
	private float pushScale;
	private SoundEvent sound;

	public TF2Explosion(Level world, Entity exploder, double x, double y, double z, float size, Entity direct,
			float harvestDamage, float pushScale, SoundEvent sound) {
		super(world, exploder, null, null, x, y, z, size, false, Explosion.BlockInteraction.DESTROY);
		this.explosionRNG = new Random();
		this.affectedBlockPositions = Lists.newArrayList();
		this.knockbackMap = Maps.newHashMap();
		this.exploder = exploder;
		this.explosionSize = size;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
		this.position = new Vec3(explosionX, explosionY, explosionZ);
		this.world = world;
		this.directHit = direct;
		this.harvestDamage = harvestDamage;
		this.pushScale = pushScale;
		this.sound = sound;
	}

	@Override
	public void explode() {
		HashSet<BlockPos> hashset = Sets.newHashSet();
		int j;
		int k;
		if (TF2ConfigVars.destTerrain == 2 || (TF2ConfigVars.destTerrain == 1 && this.harvestDamage > 0))
			for (int i = 0; i < 16; ++i)
				for (j = 0; j < 16; ++j)
					for (k = 0; k < 16; ++k)
						if (i == 0 || i == 15 || j == 0 || j == 15 || k == 0 || k == 15) {
							double d0 = i / 15.0F * 2.0F - 1.0F;
							double d1 = j / 15.0F * 2.0F - 1.0F;
							double d2 = k / 15.0F * 2.0F - 1.0F;
							double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
							d0 /= d3;
							d1 /= d3;
							d2 /= d3;
							float f = TF2ConfigVars.destTerrain == 2
									? this.explosionSize * (0.7F + this.world.random.nextFloat() * 0.6F)
									: (this.harvestDamage / 64);
							double d4 = this.explosionX;
							double d6 = this.explosionY;
							double d8 = this.explosionZ;

							if (TF2ConfigVars.destTerrain == 2)
								for (; f > 0.0F; f -= 0.22500001F) {
									BlockPos blockpos = new BlockPos(d4, d6, d8);
									BlockState iblockstate = this.world.getBlockState(blockpos);

									if (!iblockstate.isAir()) {
										float f2 = iblockstate.getExplosionResistance(world, blockpos, this);
										f -= (f2 + 0.3F) * 0.3F;
									}

									if (f > 0.0F && (this.exploder == null || this.exploder
											.shouldBlockExplode(this, this.world, blockpos, iblockstate, f)))
										hashset.add(blockpos);

									d4 += d0 * 0.30000001192092896D;
									d6 += d1 * 0.30000001192092896D;
									d8 += d2 * 0.30000001192092896D;
								}
							else
								for (; f > 0.0F; f -= 0.015F) {
									BlockPos blockpos = new BlockPos(d4, d6, d8);
									BlockState iblockstate = this.world.getBlockState(blockpos);

									if (!iblockstate.isAir())
										f = TF2Util.damageBlock(blockpos, (LivingEntity) null, world,
												ItemStack.EMPTY, 0, f, null, this);

									d4 += d0 * 0.30000001192092896D;
									d6 += d1 * 0.30000001192092896D;
									d8 += d2 * 0.30000001192092896D;
								}
						}
		this.affectedBlockPositions.addAll(hashset);
		float f3 = Math.max(6, this.explosionSize * 2.0F);
		j = Mth.floor(this.explosionX - f3 - 1.0D);
		k = Mth.floor(this.explosionX + f3 + 1.0D);
		int j1 = Mth.floor(this.explosionY - f3 - 1.0D);
		int l = Mth.floor(this.explosionY + f3 + 1.0D);
		int k1 = Mth.floor(this.explosionZ - f3 - 1.0D);
		int i1 = Mth.floor(this.explosionZ + f3 + 1.0D);
		List<Entity> list = this.world.getEntitiesOfClass(Entity.class, new AABB(j, j1, k1, k, l, i1),
				input -> input != exploder && !input.ignoreExplosion()
						&& (input.isAttackable() || TF2ConfigVars.destTerrain == 2)
						&& TF2Util.canHit(getSourceMob(), input));
		ForgeEventFactory.onExplosionDetonate(this.world, this, list, f3);
		Vec3 Vec3 = new Vec3(this.explosionX, this.explosionY, this.explosionZ);
		int livingEntities = 0;
		for (Object obj : list)
			if (obj instanceof LivingEntity)
				livingEntities++;
		for (int l1 = 0; l1 < list.size(); ++l1) {
			Entity entity = list.get(l1);
			double d4 = TF2Util.getDistanceBox(entity, this.explosionX, explosionY, explosionZ, 0, 0)
					/ this.getExplosionSize(entity) * 0.5;

			if (d4 <= 0.5D) {
				boolean isExploder = this.getSourceMob() == entity;
				boolean expJump = isExploder && this.directHit == null;
				// System.out.println("jump: "+expJump);
				double d5 = entity.getX() - this.explosionX;
				double d6;
				if (isExploder)
					d6 = entity.getY() + entity.getEyeHeight() - 0.185 - this.explosionY;
				else
					d6 = entity.getY() + (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) / 2
							- this.explosionY;
				double d7 = entity.getZ() - this.explosionZ;
				double d9 = (float) Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);

				if (d9 != 0.0D) {
					d5 /= d9;
					d6 /= d9;
					d7 /= d9;
					// float explMod=(this.explosionSize/4);
					Vec3 move = new Vec3(d5, d6, d7).normalize();
					double d10 = this.getBlockDensity(Vec3,
							entity.getBoundingBox().inflate(-0.05f, -0.05f, -0.05f));
					double d11 = (1D - d4) * d10;
					// System.out.println("multiplier: "+d11+" damage:
					// "+this.dmg);
					if (entity == this.directHit)
						d11 = 1;
					if (d11 == 0)
						continue;
					/*
					 * if (expJump) { move=move.addVector(d5*0.5, 0, d7*0.5); }
					 */
					this.affectedEntities.put(entity, (float) (d11 / (expJump ? 2 : 1)));

					// double d8 =
					// EnchantmentProtection.getBlastDamageReduction(entity,
					// d11);
					// System.out.println("d: "+ d5 * d8+" "+d6 * d8+" "+d7
					// * d8);
					// entity.addVelocity(2, 2, 2);
					d11 = d11 * 0.5 + 0.5;

					if (entity instanceof ServerPlayer) {
						// TF2weapons.network.sendTo(new
						// TF2Message.PropertyMessage("ExpJump",(byte)1,entity),
						// (EntityPlayerMP) entity);

						this.getKnockbackMap().put(entity, move.scale(d11));
						// entity.getDataWatcher().updateObject(29,
						// Byte.valueOf((byte) 1));
					} else if (!(entity instanceof LivingEntity && ((LivingEntity) entity).getAttributes()
							.getInstance(Attributes.KNOCKBACK_RESISTANCE)
							.getValue() >= 1)) {
						this.getKnockbackMap().put(entity, move);
					}
				}
			}
		}
	}

	/**
	 * Does the second part of the explosion (sound, particles, drop spawn)
	 */
	@Override
	public void finalizeExplosion(boolean spawnParticles) {
		if (sound != null) {
			this.world.playSound((Player) null, this.explosionX, this.explosionY, this.explosionZ, sound,
					SoundSource.BLOCKS, this.getSourceMob() instanceof Player ? 4.0F : 1.0F, 1.0F);
		}

		if (this.isSmoking)
			TF2Util.sendParticle(EnumTF2Particles.EXPLOSION, world, this.explosionX, this.explosionY, this.explosionZ,
					this.explosionSize, 0, 0, 1);
		else
			this.world.addParticle(ParticleTypes.EXPLOSION, this.explosionX, this.explosionY,
					this.explosionZ, 1.0D, 0.0D, 0.0D);

		if (this.isSmoking && TF2ConfigVars.destTerrain == 2)
			for (BlockPos blockpos : this.affectedBlockPositions) {
				BlockState iblockstate = this.world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if (spawnParticles) {
					double d0 = blockpos.getX() + this.world.random.nextFloat();
					double d1 = blockpos.getY() + this.world.random.nextFloat();
					double d2 = blockpos.getZ() + this.world.random.nextFloat();
					double d3 = d0 - this.explosionX;
					double d4 = d1 - this.explosionY;
					double d5 = d2 - this.explosionZ;
					double d6 = (float) Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
					d3 = d3 / d6;
					d4 = d4 / d6;
					d5 = d5 / d6;
					double d7 = 0.5D / (d6 / this.explosionSize + 0.1D);
					d7 = d7 * (this.world.random.nextFloat() * this.world.random.nextFloat() + 0.3F);
					d3 = d3 * d7;
					d4 = d4 * d7;
					d5 = d5 * d7;
					this.world.addParticle(ParticleTypes.POOF, (d0 + this.explosionX) / 2.0D,
							(d1 + this.explosionY) / 2.0D, (d2 + this.explosionZ) / 2.0D, d3, d4, d5);
					this.world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
				}

				if (!iblockstate.isAir()) {
					if (iblockstate.canDropFromExplosion(this.world, blockpos, this) && this.world instanceof ServerLevel)
						Block.dropResources(iblockstate, this.world, blockpos, this.world.getBlockEntity(blockpos));

					iblockstate.onBlockExploded(this.world, blockpos, this);
				}
			}

		if (this.isFlaming)
			for (BlockPos blockpos1 : this.affectedBlockPositions)
				if (this.world.getBlockState(blockpos1).isAir()
						&& this.world.getBlockState(blockpos1.below()).isSolidRender(this.world, blockpos1.below())
						&& this.explosionRNG.nextInt(3) == 0)
					this.world.setBlockAndUpdate(blockpos1, Blocks.FIRE.defaultBlockState());
	}

	/*
	 * public float getBlockDensity(Vec3d p_72842_1_, AxisAlignedBB p_72842_2_) {
	 * double d0 = 1.0D / ((p_72842_2_.maxX - p_72842_2_.minX) * 2.0D + 1.0D);
	 * double d1 = 1.0D / ((p_72842_2_.maxY - p_72842_2_.minY) * 2.0D + 1.0D);
	 * double d2 = 1.0D / ((p_72842_2_.maxZ - p_72842_2_.minZ) * 2.0D + 1.0D);
	 *
	 * if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) { int i = 0; int j = 0;
	 *
	 * for (float f = 0.0F; f <= 1.0F; f = (float)(f + d0)) { for (float f1 = 0.0F;
	 * f1 <= 1.0F; f1 = (float)(f1 + d1)) { for (float f2 = 0.0F; f2 <= 1.0F; f2 =
	 * (float)(f2 + d2)) { double d3 = p_72842_2_.minX + (p_72842_2_.maxX -
	 * p_72842_2_.minX) * f; double d4 = p_72842_2_.minY + (p_72842_2_.maxY -
	 * p_72842_2_.minY) * f1; double d5 = p_72842_2_.minZ + (p_72842_2_.maxZ -
	 * p_72842_2_.minZ) * f2; RayTraceResult mop=this.world.rayTraceBlocks(new
	 * Vec3d(d3, d4, d5), p_72842_1_, false, true, false); IBlockState
	 * state=this.world.getBlockState(mop.getBlockPos()); if (mop ==
	 * null||state.getBlock()==Blocks.SNOW_LAYER) { ++i; }
	 *
	 * ++j; } } }
	 *
	 * return (float)i / (float)j; } else { return 0.0F; } }
	 */
	public float getBlockDensity(Vec3 origin, AABB p_72842_2_) {
		double d0 = 1.0D / ((p_72842_2_.maxX - p_72842_2_.minX) * 2.0D + 1.0D);
		double d1 = 1.0D / ((p_72842_2_.maxY - p_72842_2_.minY) * 2.0D + 1.0D);
		double d2 = 1.0D / ((p_72842_2_.maxZ - p_72842_2_.minZ) * 2.0D + 1.0D);
		List<AABB> collisionBoxes = new ArrayList<>();
		for (net.minecraft.world.phys.shapes.VoxelShape shape : this.world.getBlockCollisions(null, p_72842_2_.inflate(1, 1, 1)))
			collisionBoxes.addAll(shape.toAabbs());
		if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
			int i = 0;
			int j = 0;

			for (float f = 0.0F; f <= 1.0F; f = (float) (f + d0))
				for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) (f1 + d1))
					for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) (f2 + d2)) {
						double d3 = p_72842_2_.minX + (p_72842_2_.maxX - p_72842_2_.minX) * f;
						double d4 = p_72842_2_.minY + (p_72842_2_.maxY - p_72842_2_.minY) * f1;
						double d5 = p_72842_2_.minZ + (p_72842_2_.maxZ - p_72842_2_.minZ) * f2;
						Vec3 start = new Vec3(d3, d4, d5);
						boolean free = true;
						for (AABB box : collisionBoxes)
							if (box.clip(start, origin).isPresent()) {
								free = false;
								break;
							}
						// RayTraceResult mop=//this.world.getCo(new
						// Vec3d(d3, d4, d5), p_72842_1_, false, true, false);
						if (free)
							++i;

						++j;
					}
			// System.out.println("Explosion power: "+((float)i/j));
			return (float) i / (float) j;
		} else
			return 0.0F;
	}

	public Map<Entity, Vec3> getKnockbackMap() {
		return this.knockbackMap;
	}

	public float getExplosionSize(Entity target) {
		return target == getSourceMob() && this.exploder instanceof EntityProjectileBase
				? ((EntityProjectileBase) this.exploder).getExplosionSize()
				: this.explosionSize;
	}

	/**
	 * Returns either the entity that placed the explosive block, the entity that
	 * caused the explosion or null.
	 */
	@Override
	public LivingEntity getSourceMob() {
		if (this.exploder instanceof EntityProjectileBase) {
			return ((EntityProjectileBase) this.exploder).shootingEntity;
		}
		return (LivingEntity) this.exploder;
	}

	public void func_180342_d() {
		this.affectedBlockPositions.clear();
	}

	public List<BlockPos> func_180343_e() {
		return this.affectedBlockPositions;
	}

	@Override
	public Vec3 getPosition() {
		return this.position;
	}

}