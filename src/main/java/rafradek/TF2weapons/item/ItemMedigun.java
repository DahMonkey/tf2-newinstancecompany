package rafradek.TF2weapons.item;

import rafradek.TF2weapons.client.compat.ScaledResolution;
import rafradek.TF2weapons.client.compat.LegacyGui;
import rafradek.TF2weapons.util.TF2HitResult;
import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;
import rafradek.TF2weapons.client.compat.OpenGlHelper;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.ClientRender;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.Contract.Objective;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMedigun extends ItemUsable {

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		// if(!world.isRemote||living !=
		// Minecraft.getMinecraft().player!(TF2weapons.medigunLock&&living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).healTarget>0)) return false;
		// System.out.println("View: "+var4+" "+startX+" "+startY+" "+startZ+"
		// "+startX+endX+" "+endY+" "+endZ);
		if (world.isClientSide && living == rafradek.TF2weapons.client.ClientProxy.getLocalPlayer()) {
			HitResult trace = this.trace(stack, living, world);
			if (world.getEntity(living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHealTarget()) == null
					&& trace != null && TF2HitResult.getEntity(trace) != null && TF2HitResult.getEntity(trace) instanceof LivingEntity
					&& !(TF2HitResult.getEntity(trace) instanceof EntityBuilding)) {
				List<HitResult> list = new ArrayList<>();
				TF2HitResult.setHitInfo(trace, new float[] { 0, 0 });
				list.add(trace);
				message.target = list;
			}
		} else if (!world.isClientSide && message != null && message.readData != null) {
			living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setHealTarget((int) message.readData.get(0)[0]);
		}
		return true;
	}

	public void heal(ItemStack stack, LivingEntity living, Level world, LivingEntity target) {

		if (!(living instanceof Player && ((Player) living).getAbilities().instabuild)) {
			// ItemStack stackAmmo = this.searchForAmmo(living, stack);
			if (!this.isAmmoSufficient(stack, living, true))
				return;
			int use = 16;
			if (target.getHealth() >= target.getMaxHealth())
				use /= 2;
			if (target.getAbsorptionAmount() >= target.getMaxHealth() * this.getMaxOverheal(stack, living, target)
					* 0.9f) {
				use = use * 2 / 5;
				if (stack.getTag().getFloat("ubercharge") >= 1)
					if (living instanceof Player)
						use /= 3;
					else
						use = 0;
			} else if (stack.getTag().getFloat("ubercharge") >= 1)
				use = use * 2 / 3;
			if (use != 0)
				this.consumeAmmoGlobal(living, stack, use);
			// ((ItemAmmo) stackAmmo.getItem()).consumeAmmo(living, stackAmmo,
			// this.getActualAmmoUse(stack, living, use));
		}
		int lastHitTime = target.tickCount - target.getPersistentData().getInt("lasthit");
		float heal = this.getHealAmount(stack, living, target);
		if (!(target instanceof IEntityTF2))
			heal *= TF2ConfigVars.damageMultiplier;
		if (lastHitTime > 200)
			heal *= 1 + Math.min(2, (lastHitTime - 200f) / 50f);
		float overheal = heal + target.getMaxHealth() * 0.001666f;
		float ubercharge = TF2Attribute.getModifier("Uber Rate", stack, 0.00125f, living);
		if (TF2Attribute.getModifier("Life Steal", stack, 0, living) != 0 && !TF2Util.isAlliedTo(living, target)) {
			heal = heal * (0.8f + TF2Attribute.getModifier("Life Steal", stack, 0, living) * 0.2f) * (1f
					- (float) target.getAttribute(Attributes.ARMOR).getValue() * 0.03f);
			TF2Util.dealDamage(target, world, living, stack, 0, heal,
					TF2Util.causeDirectDamage(stack, living).setCritical(0).bypassArmor());
			float lifesteal = TF2Util.getReducedHealing(living, target,
					heal * TF2Attribute.getModifier("Life Steal", stack, 0, living) * 0.15f);
			if (!TF2Util.isEnemy(living, target))
				lifesteal *= 0.25f;
			if (target instanceof Player) {
				lifesteal *= 2f;
			}
			living.heal(lifesteal);

		} else {
			if (target.getHealth() < target.getMaxHealth()) {
				overheal = (target.getHealth() + heal) - target.getMaxHealth() + 0.04f;
				target.heal(heal);
				if (living instanceof ServerPlayer
						&& (target instanceof EntityTF2Character || target instanceof Player)) {
					if ((living.getCapability(TF2weapons.PLAYER_CAP).orElse(null).healed += heal) >= 20) {
						living.getCapability(TF2weapons.PLAYER_CAP).orElse(null).healed -= 20;
						living.getCapability(TF2weapons.PLAYER_CAP).orElse(null).completeObjective(Objective.HEAL_20, stack);
					}
				}
			} else {
				if (target.getAbsorptionAmount() < target.getMaxHealth() * this.getMaxOverheal(stack, living, target)) {
					target.setAbsorptionAmount(Math.min(target.getAbsorptionAmount() + overheal,
							target.getMaxHealth() * this.getMaxOverheal(stack, living, null)));
					target.getEntityData().set(TF2EventsCommon.ENTITY_OVERHEAL, target.getAbsorptionAmount());
				}
				ubercharge = TF2Attribute.getModifier("Uber Overheal Rate", stack, ubercharge, target);
				if (target.getAbsorptionAmount() >= target.getMaxHealth()
						* (this.getMaxOverheal(stack, living, target) - 0.075)) {
					ubercharge /= 2;
				}
			}

		}
		if (!stack.getTag().getBoolean("Activated") && stack.getTag().getFloat("ubercharge") < 1) {
			float preUber = stack.getTag().getFloat("ubercharge");
			stack.getTag().putFloat("ubercharge", Math.min(1, preUber + ubercharge));
			if (Mth.floor(stack.getTag().getFloat("ubercharge")
					* this.getUbers(stack, living)) > Mth.floor(preUber * this.getUbers(stack, living)))
				TF2Util.playSound(living, ItemFromData.getSound(stack, PropertyType.CHARGED_SOUND), 1.25f, 1);
		}
	}

	public HitResult trace(ItemStack stack, LivingEntity living, Level world) {
		double startX = 0;
		double startY = 0;
		double startZ = 0;

		double endX = 0;
		double endY = 0;
		double endZ = 0;

		// double[]
		// rand=TF2weapons.radiusRandom3D(this.getWeaponSpread(stack,living),
		// world.rand);

		startX = living.getX();// - (double)(MathHelper.cos(living.rotationYaw /
								// 180.0F * (float)Math.PI) * 0.16F);
		startY = living.getY() + living.getEyeHeight();
		startZ = living.getZ();// - (double)(MathHelper.sin(living.rotationYaw /
								// 180.0F * (float)Math.PI) * 0.16F);

		// double[] rand=TF2weapons.radiusRandom2D(this.getWeaponSpread(stack),
		// world.rand);

		// float spreadPitch = (float) (living.rotationPitch / 180 + rand[1]);
		// float spreadYaw = (float) (living.rotationYaw / 180 +
		// rand[0]*(90/Math.max(90-Math.abs(spreadPitch*180),0.0001f)));
		// System.out.println("Rot: "+living.yHeadRot+"
		// "+living.rotationPitch);
		float spreadPitch = living.getXRot() / 180;
		float spreadYaw = living.yHeadRot / 180;

		endX = -Mth.sin(spreadYaw * (float) Math.PI) * Mth.cos(spreadPitch * (float) Math.PI);
		endY = (-Mth.sin(spreadPitch * (float) Math.PI));
		endZ = Mth.cos(spreadYaw * (float) Math.PI) * Mth.cos(spreadPitch * (float) Math.PI);

		float var9 = (float) Math.sqrt(endX * endX + endY * endY + endZ * endZ);
		// float[] ratioX= this.calculateRatioX(living.rotationYaw,
		// living.rotationPitch);
		// float[] ratioY= this.calculateRatioY(living.rotationYaw,
		// living.rotationPitch);
		// float
		// wrapAngledYaw=MathHelper.wrapAngleTo180_float(living.rotationYaw);
		// float
		// fixedYaw=Math.max(Math.abs(wrapAngledYaw),90)-Math.min(Math.abs(wrapAngledYaw),90);

		double range = getData(stack).getFloat(PropertyType.RANGE);
		endX = (endX / var9) * range;
		endY = (endY / var9) * range;
		endZ = (endZ / var9) * range;
		List<HitResult> list = TF2Util.pierce(world, living, startX, startY, startZ, startX + endX, startY + endY,
				startZ + endZ, false, 0.2f, false);
		return !list.isEmpty() && TF2Util.hitEntity(list.get(0)) != null ? list.get(0) : null;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void inventoryTick(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.inventoryTick(par1ItemStack, par2World, par3Entity, par4, par5);
		LivingEntity living = (LivingEntity) par3Entity;
		if (par1ItemStack.isEmpty())
			return;
		if (!par2World.isClientSide && par5 && !this.canFire(par2World, living, par1ItemStack)) {
			par3Entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setHealTarget(-1);
		}

		if (!par2World.isClientSide) {

			MobEffect effect = this.getPotion(par1ItemStack, living);
			boolean shield = this.isShieldResist(par1ItemStack, living);
			boolean activated = par1ItemStack.getTag().getBoolean("Activated");

			if (activated && !shield) {
				float uber = par1ItemStack.getTag().getFloat("ubercharge")
						- 1f / this.getUberTime(par1ItemStack, living);
				par1ItemStack.getTag().putFloat("ubercharge", Math.max(0, uber));

				if (par5 && effect != null && par3Entity.tickCount % 4 == 0)
					TF2Util.addAndSendEffect(living, new MobEffectInstance(effect, 15));
				if (par1ItemStack.getTag().getFloat("ubercharge") == 0) {

					par1ItemStack.getTag().putBoolean("Activated", false);
					TF2Util.playSound(par3Entity, ItemFromData.getSound(par1ItemStack, PropertyType.UBER_STOP_SOUND),
							1.5f, 1);
					living.removeEffect(effect);

					// TF2weapons.sendTracking(new
					// TF2Message.PropertyMessage("UberCharged",
					// (byte)0,par3Entity),par3Entity);
				}
			}
			if (par5) {
				WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
				cap.setUberView(par1ItemStack.getTag().getFloat("ubercharge"));
				if (shield && (cap.state & 4) == 4 && cap.getSecondaryCooldown() <= 0) {
					cap.setSecondaryCooldown(250);
					TF2Util.playSound(living, ItemFromData.getSound(par1ItemStack, PropertyType.SPECIAL_1_SOUND), 0.75f,
							1);
					par1ItemStack.getTag().putByte("ResType",
							(byte) (par1ItemStack.getTag().getByte("ResType") + 1));
					if (par1ItemStack.getTag().getByte("ResType") > 2) {
						par1ItemStack.getTag().putByte("ResType", (byte) 0);
					}
				}
				if (this.getPotion(par1ItemStack, living) == TF2weapons.quickFix
						&& this.isAlreadyUbered(par1ItemStack, living)) {
					this.heal(par1ItemStack, living, par2World, living);
				}
				Entity healTargetEnt = par2World.getEntity(cap.getHealTarget());
				if (healTargetEnt != null && healTargetEnt instanceof LivingEntity) {
					LivingEntity healTarget = (LivingEntity) healTargetEnt;
					// System.out.println("healing:
					// "+ItemUsable.itemProperties.server.get(par3Entity).getInteger("HealTarget"));
					double range = getData(par1ItemStack).getFloat(PropertyType.RANGE) + 1.6;
					if (!par2World.isClientSide && healTarget != null
							&& par3Entity.distanceToSqr(healTarget) > range * range) {
						par3Entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setHealTarget(-1);
						// TF2weapons.sendTracking(new
						// TF2Message.PropertyMessage("HealTarget",
						// -1,par3Entity),par3Entity);
					} else if (healTarget != null && healTarget instanceof LivingEntity) {

						this.heal(par1ItemStack, living, par2World, healTarget);
						if (effect != null && (healTarget.tickCount % 4 == 0
								|| healTarget.getEffect(effect) == null) && (activated || shield)) {
							TF2Util.addAndSendEffect(healTarget, new MobEffectInstance(effect, shield ? 5 : 15));
							if (shield) {
								TF2Util.addAndSendEffect(living, new MobEffectInstance(effect, 5));
							}
						}
					}
				}
			}
			if (par5 || par4 == 40) {
				try {
					TF2Util.getHandStack(par3Entity, par5 ? 0 : 1)
							.getTag()
							.putFloat("ubercharge", par1ItemStack.getTag().getFloat("ubercharge"));
				} catch (Exception e) {}
			}
		}
	}

	public boolean shouldActivateCharge(ItemStack stack, EntityTF2Character living, LivingEntity target) {
		if (this.isShieldResist(stack, living)) {
			// System.out.println("reload: "+!living.isReloadPressed()+"
			// "+(living.getLastDamageSource() != null || target.getLastDamageSource() !=
			// null));
			return (living.getHealth() < living.getMaxHealth() * 0.82 || target.getHealth() < target.getMaxHealth())
					&& (living.getLastDamageSource() != null || target.getLastDamageSource() != null)
					&& !living.isReloadPressed();
		} else {
			if (this.getPotion(stack, living) == TF2weapons.critBoost) {
				if (target instanceof EntityTF2Character
						&& ((EntityTF2Character) target).getTarget() instanceof Player
						&& ((EntityTF2Character) target).attack.getRangeSq() >= ((EntityTF2Character) target)
								.getTarget().distanceToSqr(target)) {
					return true;
				}
				return target.getHealth() / target.getMaxHealth() < 0.35F
						&& (target.tickCount - target.getLastHurtByMobTimestamp()) < 25;
			} else {
				return target.getHealth() / target.getMaxHealth() < 0.35F
						&& (target.tickCount - target.getLastHurtByMobTimestamp()) < 25;
			}
		}
	}

	@Override
	public void holster(WeaponsCapability cap, ItemStack stack, LivingEntity living, Level world) {
		cap.setHealTarget(-1);
		living.removeEffect(TF2weapons.uber);
		super.holster(cap, stack, living, world);
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return !(living instanceof Player) || (((Player) living).getAbilities().instabuild
				|| !this.searchForAmmo(living, stack).isEmpty());
	}

	@Override
	public boolean fireTick(ItemStack stack, LivingEntity living, Level world) {
		return false;
	}

	@Override
	public boolean altFireTick(ItemStack stack, LivingEntity living, Level world) {
		return false;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		return true;
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entityLiving) {
		return true;
	}

	public boolean isAlreadyUbered(ItemStack stack, LivingEntity living) {
		MobEffect potion = this.getPotion(stack, living);
		return living.hasEffect(potion)
				&& (living.getEffect(potion).getAmplifier() > 0 || !this.isShieldResist(stack, living));
	}

	public MobEffect getPotion(ItemStack stack, LivingEntity living) {
		if (this.isShieldResist(stack, living)) {
			switch (stack.getTag().getByte("ResType")) {
			case 0:
				return TF2weapons.shieldBullet;
			case 1:
				return TF2weapons.shieldExplosive;
			case 2:
				return TF2weapons.shieldFire;
			default:
				return TF2weapons.shieldBullet;
			}
		}
		return TF2Util.getEffect(ItemFromData.getData(stack).getString(PropertyType.EFFECT_TYPE));
	}

	public boolean isShieldResist(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 1;
	}

	public float getHealAmount(ItemStack stack, LivingEntity living, LivingEntity target) {
		float mult = 1f;
		if (this.getPotion(stack, living) == TF2weapons.quickFix && this.isAlreadyUbered(stack, target)) {
			mult *= 3f;
		}
		return mult * TF2Attribute.getModifierGlobal("Medic Heal", TF2Attribute.getModifier("Heal", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.HEAL), living), living)
				* TF2Attribute.getModifierGlobal("Healing From Medic", 1, target);
	}

	public int getUbers(ItemStack stack, LivingEntity living) {
		return this.isShieldResist(stack, living) ? 4 : 1;
	}

	public float getUberTime(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Uber Time", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.UBER_TIME), living);
	}

	public float getMaxOverheal(ItemStack stack, LivingEntity living, LivingEntity target) {
		if (target instanceof EntityTF2Character && ((EntityTF2Character) target).isGiant())
			return 0;
		return TF2Attribute.getModifier("Overheal", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.MAX_OVERHEAL), living);
	}

		@OnlyIn(Dist.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, world, tooltip, advanced);

		tooltip.add(Component.literal("Charge: " + Integer.toString(Math.round((stack.getTag().getFloat("ubercharge")) * 100)) + "%"));
	}

	@Override
	public boolean startUse(ItemStack stack, LivingEntity living, Level world, int oldState, int newState) {
		if (((newState & 1) - (oldState & 1)) == 1) {
			if (world.isClientSide) {
				HitResult trace = this.trace(stack, living, world);
				if (trace == null || TF2HitResult.getEntity(trace) == null || !(TF2HitResult.getEntity(trace) instanceof LivingEntity))
					ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.NO_TARGET_SOUND),
							false, 1, stack);
			} else {
				if (living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHealTarget() != -1) {
					living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setHealTarget(-1);
					// TF2weapons.network.sendToServer(new TF2Message.CapabilityMessage(living,
					// false));
				}
			}
			// System.out.println("Stop heal");
		}
		if (!world.isClientSide && ((newState & 2) - (oldState & 2)) == 2
				&& stack.getTag().getFloat("ubercharge") >= 1f / this.getUbers(stack, living)
				&& !this.isAlreadyUbered(stack, living)) {
			stack.getTag().putBoolean("Activated", true);
			TF2Util.playSound(living, ItemFromData.getSound(stack, PropertyType.UBER_START_SOUND), 0.75f, 1);
			Entity healTargetEnt = world
					.getEntity(living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHealTarget());
			if (healTargetEnt instanceof LivingEntity) {
				((LivingEntity) healTargetEnt).addEffect(new MobEffectInstance(this.getPotion(stack, living), 1));
			}
			if (this.isShieldResist(stack, living)) {
				if (healTargetEnt instanceof LivingEntity)
					TF2Util.addAndSendEffect(((LivingEntity) healTargetEnt),
							new MobEffectInstance(this.getPotion(stack, living),
									(int) (this.getUberTime(stack, living) / this.getUbers(stack, living)), 1));
				TF2Util.addAndSendEffect(living, new MobEffectInstance(this.getPotion(stack, living),
						(int) (this.getUberTime(stack, living) / this.getUbers(stack, living)), 1));
				stack.getTag().putFloat("ubercharge",
						stack.getTag().getFloat("ubercharge") - 1f / this.getUbers(stack, living));
				stack.getTag().putBoolean("Activated", false);
			}
			if (stack.getTag().getBoolean("Strange")) {
				stack.getTag().putInt("Ubercharges", stack.getTag().getInt("Ubercharges") + 1);
				TF2EventsCommon.onStrangeUpdate(stack, living);
			}
			// TF2weapons.sendTracking(new
			// TF2Message.PropertyMessage("UberCharged",
			// (byte)1,living),living);
		}
		return false;
	}

	@Override
	public boolean endUse(ItemStack stack, LivingEntity living, Level world, int oldState, int newState) {
		if (!world.isClientSide && !TF2ConfigVars.medigunLock && (oldState & 1 - newState & 1) == 1) {
			living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setHealTarget(-1);
			// TF2weapons.network.sendToServer(new TF2Message.CapabilityMessage(living,
			// false));
		}
		return false;
	}

	@Override
	@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
	public void drawOverlay(ItemStack stack, Player player, Tessellator tessellator, BufferBuilder renderer,
			ScaledResolution resolution) {
		RenderSystem.setShaderTexture(0, ClientProxy.healingTexture);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		ClientProxy.setColor(TF2Util.getTeamColor(player), 0.7f, 0, 0.25f, 0.8f);

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(resolution.getScaledWidth() - 138, resolution.getScaledHeight() - 20, 0.0D).tex(0.0D, 1D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 14, resolution.getScaledHeight() - 20, 0.0D).tex(0.01D, 1D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 14, resolution.getScaledHeight() - 50, 0.0D).tex(0.01D, 0.99D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 138, resolution.getScaledHeight() - 50, 0.0D).tex(0.0D, 0.99D)
				.endVertex();
		tessellator.draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(resolution.getScaledWidth() - 140, resolution.getScaledHeight() - 18, 0.0D).tex(0.0D, 0.265625D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 12, resolution.getScaledHeight() - 18, 0.0D).tex(1.0D, 0.265625D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 12, resolution.getScaledHeight() - 52, 0.0D).tex(1.0D, 0.0D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 140, resolution.getScaledHeight() - 52, 0.0D).tex(0.0D, 0.0D)
				.endVertex();
		tessellator.draw();

		float uber = stack.getTag().getFloat("ubercharge");
		String text;
		if (this.getUbers(stack, player) == 1)
			text = "UBERCHARGE: " + Math.round(uber * 100f) + "%";
		else
			text = "UBERCHARGES: " + Mth.floor(uber / (1f / this.getUbers(stack, player)));
		LegacyGui.drawString(Minecraft.getInstance().font, text,
				resolution.getScaledWidth() - 130, resolution.getScaledHeight() - 48, 16777215);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F);
		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 22, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 20, resolution.getScaledHeight() - 22, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 20, resolution.getScaledHeight() - 36, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 36, 0.0D).endVertex();
		tessellator.draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 22, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 132 + 112 * uber, resolution.getScaledHeight() - 22, 0.0D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 132 + 112 * uber, resolution.getScaledHeight() - 36, 0.0D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 36, 0.0D).endVertex();
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (this.isShieldResist(stack, player)) {
			float offset = 0f;
			if (TF2Util.getTeamForDisplay(player) != 0)
				offset = 96f;
			RenderSystem.setShaderTexture(0, ClientRender.VAC_EFFECT);
			switch (stack.getTag().getByte("ResType")) {
			case 0:
				offset += 0f;
				break;
			case 1:
				offset += 32f;
				break;
			case 2:
				offset += 64f;
				break;
			default:
				offset += 0f;
				break;
			}
			LegacyGui.drawModalRectWithCustomSizedTexture(resolution.getScaledWidth() - 95, resolution.getScaledHeight() - 90,
					offset, 0, 32, 32, 256, 32);
		}
	}
}
