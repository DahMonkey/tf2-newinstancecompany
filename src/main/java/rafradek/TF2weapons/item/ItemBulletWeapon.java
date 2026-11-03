package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.projectile.EntityFlame;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.DamageSourceDirect;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ItemBulletWeapon extends ItemWeapon {

	public static ThreadLocal<HashMap<Entity, float[]>> lastShot = new ThreadLocal<HashMap<Entity, float[]>>() {
		@Override
		protected HashMap<Entity, float[]> initialValue() {
			return new HashMap<>();
		}
	};
	public static ThreadLocal<Integer> lastFlags = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};
	public static ArrayList<HitResult> lastShotClient = new ArrayList<>();
	public static boolean processShotServer;

	public void handleShoot(LivingEntity living, ItemStack stack, Level world, HashMap<Entity, float[]> map,
			int critical, int flags) {

		Iterator<Entity> iterator = map.keySet().iterator();
		boolean damageDone = false;
		while (iterator.hasNext()) {
			DamageSourceDirect var22 = TF2Util.causeDirectDamage(stack, living);
			((TF2DamageSource) var22).addAttackFlag(flags);

			if (!(this instanceof ItemMeleeWeapon))
				var22.setProjectile();

			Entity entity = iterator.next();
			((TF2DamageSource) var22).setAttackPower(map.get(entity)[0]);

			critical = this.setCritical(stack, living, entity, critical, var22);
			var22.setCritical(critical);

			float damage = map.get(entity)[0]
					* TF2Util.calculateDamage(entity, world, living, stack, critical, map.get(entity)[1]);

			if (!((ItemWeapon) stack.getItem()).onHit(stack, living, entity, damage, critical, false))
				continue;
			Vec3 pushvec = entity.position().subtract(living.position()).normalize();

			if (entity instanceof EndCrystal && living.getRandom().nextFloat() > damage / 15f) {
				continue;
			}

			if (map.get(entity) != null && damage != 0
					&& TF2Util.dealDamage(entity, world, living, stack, critical, damage, var22)) {
				damageDone = true;
				// System.out.println("Damage: "+map.get(entity)[1]);
				// distance = ((ItemBulletWeapon) stack.getItem()).getMaxRange(stack) /
				// distance;
				/*
				 * double distX = (living.posX - entity.posX) * distance; double distY =
				 * (living.posY - entity.posY) * distance; double distZ = (living.posZ -
				 * entity.posZ) * distance;
				 */
				if (!stack.isEmpty()) {
					double knockbackAmount = ((ItemBulletWeapon) stack.getItem()).getKnockbackForDamage(stack, living,
							damage, var22);

					if (entity instanceof LivingEntity)
						knockbackAmount *= 1 - ((LivingEntity) entity).getAttributes()
								.getInstance(Attributes.KNOCKBACK_RESISTANCE).getValue();
					if (knockbackAmount > 0) {
						boolean flag = map.get(entity)[1] < 3.75
								&& living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).fanCool <= 0
								&& TF2Attribute.getModifier("KnockbackFAN", stack, 0, living) != 0;
						pushvec = new Vec3(pushvec.x * knockbackAmount * (flag ? 2.8 : 1),
								(pushvec.y + (flag ? 1 : 0)) * knockbackAmount,
								pushvec.z * knockbackAmount * (flag ? 2.8 : 1));
						entity.push(pushvec.x, pushvec.y, pushvec.z);
						entity.hasImpulse = entity.hasImpulse || -(pushvec.y * knockbackAmount) > 0.02D;
						if (entity instanceof ServerPlayer)
							TF2weapons.network.sendTo(new TF2Message.VelocityAddMessage(pushvec, entity.hasImpulse),
									(ServerPlayer) entity);
					}
				}
			}
		}
		if (damageDone && living.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
			WeaponsCapability.get(living).hitNoMiss += 1;
			if (WeaponsCapability.get(living).hitNoMiss >= TF2Attribute.getModifier("Hit Crit", stack, 0, living))
				WeaponsCapability.get(living).hitNoMiss = 0;
		}
		map.clear();

	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		if (world.isClientSide && living == ClientProxy.getLocalPlayer())
			lastShotClient.clear();
		super.use(stack, living, world, hand, message);
		if (world.isClientSide && living == ClientProxy.getLocalPlayer()) {
			ClientProxy.getLocalPlayer().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).recoil += getData(stack)
					.getFloat(PropertyType.RECOIL);
			message.target = lastShotClient;
		} else if (!world.isClientSide) {
			if (TF2Attribute.getModifier("Onyx Projectile", stack, 0, living) != 0) {
				new ItemProjectileWeapon().shoot(stack, living, world, critical, hand);
			}
			if (living instanceof Player) {
				// System.out.println("Shoot: "+message.readData);
				if (message.readData == null) {
					WeaponsCapability.get(living).hitNoMiss = 0;
					return false;
				}
				int flags = 0;
				int totalCrit = TF2Util.calculateCritPre(stack, living);
				HashMap<Entity, float[]> shotInfo = new HashMap<>();
				for (HitResult obj : message.target) {
					Entity target = obj.getEntity();
					if (target == null)
						continue;

					if (!shotInfo.containsKey(target) || shotInfo.get(target) == null)
						shotInfo.put(target, new float[3]);
					int critical = totalCrit;
					// System.out.println(var4.hitInfo);
					if (((float[]) obj.hitInfo)[0] != 0 && critical != 2) {
						critical = this.getHeadshotCrit(living, stack);
						flags |= TF2DamageSource.HEADSHOT;
					}
					if (critical > totalCrit)
						totalCrit = critical;
					// ItemRangedWeapon.critical=critical;
					float[] values = shotInfo.get(target);
					// System.out.println(obj[2]+" "+critical);
					values[0]++;
					values[1] = ((float[]) obj.hitInfo)[1];
				}
				// living.getCapability(TF2weapons.WEAPONS_CAP,
				// null).predictionList.add(message);
				handleShoot(living, stack, world, shotInfo, totalCrit, flags);
			} else {
				handleShoot(living, stack, world, lastShot.get(), critical, lastFlags.get());
				lastShot.get().clear();
			}
		}
		if (living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).fanCool <= 0
				&& TF2Attribute.getModifier("KnockbackFAN", stack, 0, living) != 0) {
			living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).fanCool = 30;
		}
		return true;
		// if(world.isRemote) return false;
		/*
		 * if(((!world.isRemote && (processShotServer||!(living instanceof
		 * EntityPlayer)))||(world.isRemote&&living instanceof EntityPlayer)) &&
		 * super.use(stack, living, world, hand)){
		 * //System.out.println(world.isRemote+" "+stack.getTagCompound().
		 * getShort("reload")+" "+TF2ActionHandler.playerAction.get(world.
		 * isRemote).get(living));
		 *
		 * if(!world.isRemote && living != null&& !processShotServer) {
		 * handleShoot(living, stack, world, lastShot,critical); } else
		 * if(world.isRemote&&living==Minecraft.getMinecraft().player){
		 *
		 * //TF2weapons.network.sendToServer(new
		 * BulletMessage(Minecraft.getMinecraft().player.inventory.
		 * currentItem,lastShotClient, hand)); //lastShotClient.clear(); }
		 *
		 * return ; }
		 */
	}

	public boolean showTracer(ItemStack stack) {
		return true;
	}

	public boolean showSpecialTracer(ItemStack stack) {
		return TF2Attribute.getModifier("Trace Round", stack, 0, null) != 0;
	}

	@Override
	public void shoot(ItemStack stack, LivingEntity living, Level world, int critical, InteractionHand hand) {

		if (getData(stack).hasProperty(PropertyType.PROJECTILE)
				&& TF2Attribute.getModifier("Onyx Projectile", stack, 0, living) == 0) {
			if (!world.isClientSide) {
				new ItemProjectileWeapon().shoot(stack, living, world, critical, hand);
			}
			return;
		}
		boolean removeBlocks = TF2Attribute.getModifier("Destroy Block", stack, 0, living) > 0;
		if (!world.isClientSide && living instanceof Player && !removeBlocks)
			return;
		double startX = 0;
		double startY = 0;
		double startZ = 0;

		double endX = 0;
		double endY = 0;
		double endZ = 0;

		float spreadPitch = living.getXRot() / 180 * (float) Math.PI;
		float spreadYaw = living.yHeadRot / 180 * (float) Math.PI;

		Vec3 random = TF2Util.radiusRandom2D(this.getWeaponSpread(stack, living), world.random, -spreadYaw, -spreadPitch,
				getMaxRange(stack));
		startX = living.getX();
		startY = living.getY() + living.getEyeHeight();
		startZ = living.getZ();

		endX = 0;
		endY = 0;
		endZ = 0;

		endX = endX + random.x;
		endY = endY + random.y;
		endZ = endZ + random.z;

		endX += startX;
		endY += startY;
		endZ += startZ;

		if (world.isClientSide) {

			if (this.showTracer(stack)) {
				float mult = hand == InteractionHand.MAIN_HAND ? 1 : -1;
				ClientProxy.spawnBulletParticle(world, living,
						startX - Mth.cos(living.getYRot() / 180.0F * (float) Math.PI) * 0.16F * mult,
						startY - 0.1,
						startZ - Mth.sin(living.getYRot() / 180.0F * (float) Math.PI) * 0.16F * mult, endX,
						endY, endZ, this.showSpecialTracer(stack) ? 0 : 13,
						this.showSpecialTracer(stack) ? 2 : critical, 0, this.showSpecialTracer(stack) ? 50f : 1f);
			}
			if (living != Minecraft.getInstance().player)
				return;
		}
		// System.out.println(startX+" "+startY+" "+startZ+" "+endX+" "+endY+"
		// "+endZ);
		List<HitResult> list;
		list = TF2Util.pierce(world, living, startX, startY, startZ, endX, endY, endZ, this.canHeadshot(living, stack),
				this.getBulletSize(stack, living), this.canPenetrate(stack, living),
				TF2Attribute.getModifier("Destroy Projectiles", stack, 0, living) == 0 ? TF2Util.TARGETABLE
						: Predicates.or(
								target -> (target instanceof EntityProjectileBase && !(target instanceof EntityFlame)),
								TF2Util.TARGETABLE));
		for (HitResult var4 : list)
			if (var4.getEntity() != null) {
				float distance = 0;
				if (living != null) {
					distance = (float) TF2Util.getDistanceBox(living, var4.getEntity().getX(), var4.getEntity().getY(),
							var4.getEntity().getZ(), var4.getEntity().getBbWidth(), var4.getEntity().getBbHeight());
					// distance -= living.width / 2 + var4.entityHit.width / 2;

					// if (distance < 0)
					// distance = 0;
				}
				if (!world.isClientSide && !(living instanceof Player)) {
					if (!lastShot.get().containsKey(var4.getEntity()) || lastShot.get().get(var4.getEntity()) == null)
						lastShot.get().put(var4.getEntity(), new float[3]);
					// System.out.println(var4.hitInfo);
					if (var4.hitInfo != null && var4.hitInfo instanceof Boolean && (Boolean) var4.hitInfo
							&& critical != 2) {
						critical = this.getHeadshotCrit(living, stack);
						lastFlags.set(lastFlags.get() | TF2DamageSource.HEADSHOT);
					}
					ItemWeapon.critical = critical;
					float[] values = lastShot.get().get(var4.getEntity());
					values[0]++;
					values[1] = distance;
					// values[2]=distance;
				} else if (world.isClientSide) {
					// System.out.println(var4.hitInfo);
					var4.hitInfo = new float[] {
							var4.hitInfo != null && var4.hitInfo instanceof Boolean && (Boolean) var4.hitInfo ? 1 : 0,
							distance };
					lastShotClient.add(var4);
				}
			} else if (var4.getBlockPos() != null)
				if (world.isClientSide) {

					ClientProxy.spawnBulletHoleParticle(world, var4);
					for (int i = 0; i < 2; i++)
						world.addParticle(ParticleTypes.BLOCK_CRACK,
								var4.getLocation().x + var4.getDirection().getStepX() * 0.05,
								var4.getLocation().y + var4.getDirection().getStepY() * 0.05,
								var4.getLocation().z + var4.getDirection().getStepZ() * 0.05,
								var4.getDirection().getStepX() * 0.07 * i, var4.getDirection().getStepY() * 0.07 * i,
								var4.getDirection().getStepZ() * 0.07 * i,
								new int[] { Block.getStateId(world.getBlockState(var4.getBlockPos())) });
					if (getData(stack).hasProperty(PropertyType.HIT_SOUND)) {
						SoundEvent event = getData(stack).hasProperty(PropertyType.HIT_WORLD_SOUND)
								? getSound(stack, PropertyType.HIT_WORLD_SOUND)
								: getSound(stack, PropertyType.HIT_SOUND);
						world.playSound(var4.getLocation().x, var4.getLocation().y, var4.getLocation().z, event, SoundSource.PLAYERS,
								getData(stack).getName().equals("fryingpan") ? 2f : 0.7f, 1f, false);
					}
				} else if (!world.isClientSide) {
					if (TF2Attribute.getModifier("Explode Bullet", stack, 0, living) != 0) {
						TF2Util.explosion(world, living, stack, living, null,
								var4.getLocation().x + var4.getDirection().getStepX() * 0.05,
								var4.getLocation().y + var4.getDirection().getStepY() * 0.05,
								var4.getLocation().z + var4.getDirection().getStepZ() * 0.05,
								TF2Attribute.getModifier("Explode Bullet", stack, 0, living), 1, critical,
								(float) living.position().distanceTo(var4.getLocation()));
					} else if (removeBlocks) {
						float damage = TF2Util.calculateDamage(TF2weapons.dummyEnt, world, living, stack, critical,
								(float) living.position().distanceTo(var4.getLocation()));
						damage *= TF2Attribute.getModifier("Destroy Block", stack, 0, living);
						TF2Util.damageBlock(var4.getBlockPos(), living, world, stack, critical, damage,
								new Vec3(endX, endY, endZ), null);
					}
				}
	}

	/*
	 * public boolean checkHeadshot(World world, Entity living, ItemStack stack,
	 * Vec3d hitVec) { double ymax=living.getEntityBoundingBox().maxY; AxisAlignedBB
	 * head=AxisAlignedBB.getBoundingBox(living.posX-0.21, ymax-0.21,
	 * living.posZ-0.21,living.posX+0.21, ymax+0.21, living.posZ+0.21);
	 * System.out.println("Trafienie: "+Math.abs(ymax-hitVec.y));
	 *
	 * return Math.abs(ymax-hitVec.y)<0.205; }
	 */

	public float[] calculateRatioX(float yaw, float pitch) {
		float[] result = new float[3];
		float angledYaw = Math.abs(Mth.wrapDegrees(yaw));
		float distanceYaw = Math.max(angledYaw, 90) - Math.min(angledYaw, 90);
		result[0] = (distanceYaw / 90) + (1 - distanceYaw / 90) * (-pitch / 90);
		result[2] = (1 - distanceYaw / 90);// +(1-distanceYaw/90)*(-pitch/90);
		result[1] = 0;
		return result;
	}

	public float[] calculateRatioY(float yaw, float pitch) {
		float[] result = new float[3];
		float angledYaw = Math.abs(Mth.wrapDegrees(yaw));
		float distanceYaw = Math.max(angledYaw, 90) - Math.min(angledYaw, 90);
		result[0] = 0;
		result[2] = (distanceYaw / 90) * (-pitch / 90);
		result[1] = 1 - Math.abs(pitch) / 90;
		return result;
	}

	public float getMaxRange(ItemStack stack) {
		return 256;
	}

	public float getBulletSize(ItemStack stack, LivingEntity living) {
		return 0.04f / this.getWeaponPelletCount(stack, living);
	}

	public int setCritical(ItemStack stack, LivingEntity shooter, Entity target, int old, DamageSource source) {
		return TF2Util.calculateCritPost(target, shooter, old, stack, source);
	}
}
