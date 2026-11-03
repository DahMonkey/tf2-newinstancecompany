package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import java.util.UUID;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemHuntsman extends ItemProjectileWeapon {

	public static UUID slowdownUUID = UUID.fromString("12843092-A5D6-BBCD-3D4F-A3DD4ABC65A9");
	public static AttributeModifier slowdown = new AttributeModifier(slowdownUUID, "sniper slowdown", -0.51D, 2);

	public ItemHuntsman() {
		super();
		this.addPropertyOverride(new ResourceLocation("loaded"), new ItemPropertyFunction() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (ItemHuntsman.this.getClip(stack) > 0)
					return 1;
				return 0;
			}
		});
		this.addPropertyOverride(new ResourceLocation("lighted"), new ItemPropertyFunction() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (stack.hasTag() && stack.getTag().getBoolean("ArrowLit"))
					return 1;
				return 0;
			}
		});
		this.addPropertyOverride(new ResourceLocation("charge"), new ItemPropertyFunction() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				return getCharge(entityIn, stack);
			}
		});
	}

	@Override
	public int holdingMode(ItemStack stack, LivingEntity shooter) {
		return (int) ((shooter instanceof EntityTF2Character
				? ((EntityTF2Character) shooter).scaleWithDifficulty(60, 20)
						: 20) * TF2Attribute.getModifier("Fire Rate", stack, 1f, shooter));
	}

	@Override
	public boolean shouldKeepCharged(ItemStack stack, LivingEntity shooter) {
		return true;
	}

	@Override
	public float getProjectileSpeed(ItemStack stack, LivingEntity living) {
		return super.getProjectileSpeed(stack, living) * (0.7f + 0.3f * (this.getCharge(living, stack)));
	}

	@Override
	public float getWeaponSpreadBase(ItemStack stack, LivingEntity living) {
		float base = living != null && WeaponsCapability.get(living).chargeTicks >= this.holdingMode(stack, living) * 5
				? super.getWeaponSpreadBase(stack, living)
						: 0;
		return base + this.getClip(stack) * 0.06f;
	}

	@Override
	public boolean canHeadshot(LivingEntity living, ItemStack stack) {
		return this.getCharge(living, stack) > 0;
	}

	@Override
	public boolean shootAllAtOnce(ItemStack stack, LivingEntity living) {
		return true;
	}

	@Override
	public float getWeaponDamage(ItemStack stack, LivingEntity living, Entity target) {
		return super.getWeaponDamage(stack, living, target) * (this.getCharge(living, stack) * 1.4f + 1f);
	}

	@Override
	public float getAdditionalGravity(LivingEntity living, ItemStack stack, double initial) {
		return super.getAdditionalGravity(living, stack, initial) * (1 - this.getCharge(living, stack) * 0.5f);
	}

	@Override
	public float getCharge(LivingEntity living, ItemStack stack) {
		if (living == null)
			return 0f;
		/*
		 * if (living instanceof EntityTF2Character) return 1f;
		 */
		WeaponsCapability cap = WeaponsCapability.get(living);
		if (cap == null) return 0;
		if (cap.lastHitCharge != 0)
			return cap.lastHitCharge;
		int chargeTicks = cap.chargeTicks;
		int maxCharge = this.holdingMode(stack, living);

		return chargeTicks <= maxCharge * 5 ? Mth.clamp((float) chargeTicks / (float) maxCharge, 0f, 1f) : 0f;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		if (!par2World.isClientSide && par3Entity.tickCount % 10 == 0) {
			WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
			if (par5 && (OreDictionary.itemMatches(new ItemStack(Blocks.TORCH),
					((LivingEntity) par3Entity).getOffhandItem(), false)
					|| OreDictionary.itemMatches(new ItemStack(Blocks.TORCH),
							new ItemStack(par2World.getBlockState(par3Entity.blockPosition()).getBlock()), false)
					|| OreDictionary.itemMatches(new ItemStack(Blocks.TORCH),
							new ItemStack(par2World.getBlockState(par3Entity.blockPosition().above()).getBlock()), false))) {
				par1ItemStack.getTag().putBoolean("ArrowLit", true);
			}
		}
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		boolean use = super.use(stack, living, world, hand, message);
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
		if (cap.isCharging()) {
			TF2Util.addModifierSafe(living, Attributes.MOVEMENT_SPEED, slowdown, false);
			// living.playSound(getSound(stack, PropertyType.CHARGE_SOUND), 1f, 1f);
		}

		return use;
	}

	@Override
	public void onProjectileShoot(ItemStack stack, EntityProjectileBase proj, LivingEntity living, Level world,
			int thisCritical, InteractionHand hand) {
		if (this.getClip(stack) != 0) {
			proj.damageModifier *= 0.66f;
			/*
			 * proj.rotationYaw += (this.getClip(stack) % 2 == 0 ? 1 : -1 ) *
			 * this.getClip(stack)+1/2 * 8; Vec3d rot = new Vec3d(proj.motionX,
			 * proj.motionY, proj.motionZ); rot = rot.rotateYaw((this.getClip(stack) % 2 ==
			 * 0 ? 1 : -1 ) * this.getClip(stack)+1/2 * 8); proj.motionX = rot.x;
			 * proj.motionY = rot.y; proj.motionZ = rot.z;
			 */
		}
	}

	@Override
	public void shoot(ItemStack stack, LivingEntity living, Level world, int thisCritical, InteractionHand hand) {
		if (!world.isClientSide) {
			ItemStack arrow = new ItemStack(stack.getTag().getCompound("LastLoaded"));
			if (arrow.isEmpty() || arrow.getItem() == Items.ARROW)
				super.shoot(stack, living, world, thisCritical, hand);
			else if (arrow.getItem() instanceof ArrowItem) {
				AbstractArrow entityarrow = ((ArrowItem) arrow.getItem()).createArrow(world, arrow, living);
				float motion = this.getProjectileSpeed(stack, living) * 2.6f - super.getProjectileSpeed(stack, living);
				entityarrow.shoot(living, living.getXRot(), living.getYRot(), 0.0F, motion,
						this.getWeaponSpread(stack, living) * 66);
				entityarrow.pickupStatus = living instanceof Player
						&& !((ArrowItem) arrow.getItem()).isInfinite(arrow, stack, (Player) living)
						? PickupStatus.ALLOWED
								: PickupStatus.DISALLOWED;
				entityarrow.setDamage(
						entityarrow.getDamage() - 2f + this.getWeaponDamage(stack, living, null) / motion * 0.975f);
				if (stack.getTag().getBoolean("ArrowLit")) {
					entityarrow.setSecondsOnFire(500);
					stack.getTag().putBoolean("ArrowLit", false);
				}
				entityarrow.getPersistentData().putBoolean("TF2Arrow", true);
				world.addFreshEntity(entityarrow);
			}

		}
	}

	@Override
	public boolean endUse(ItemStack stack, LivingEntity living, Level world, int action, int newState) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);

		boolean charging = cap.isCharging();
		boolean ret = super.endUse(stack, living, world, action, newState);

		if (charging)
			living.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(slowdown);

		return ret;
	}

	@Override
	public boolean altFireTick(ItemStack stack, LivingEntity living, Level world) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
		if (cap.isCharging()) {
			cap.setCharging(false);
			cap.setPrimaryCooldown(750);
			living.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(slowdown);
			living.playSound(getSound(stack, PropertyType.WIND_DOWN_SOUND), 1f, 1f);
		}
		return false;
	}

	static {
		slowdown.setSaved(false);
	}
}
