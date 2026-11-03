package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntitySpy;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.ReflectionAccess;
import rafradek.TF2weapons.util.TF2Class;
import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemCloak extends ItemFromData {

	public ItemCloak() {
		super();
		this.addPropertyOverride(new ResourceLocation("active"), new ItemPropertyFunction() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (entityIn != null && isFeignDeath(stack, entityIn) && WeaponsCapability.get(entityIn).isFeign())
					return 1;
				return 0;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onUpdate(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		if (par1ItemStack.getTag().getBoolean("Active") && WeaponsCapability.get(par3Entity).isInvisible()) {
			// System.out.println("uncharge");
			int maxdamage = getMaxDamage(par1ItemStack);
			if (!(par3Entity instanceof Player && ((Player) par3Entity).getAbilities().instabuild)
					&& !(par3Entity instanceof EntityTF2Character && ((EntityTF2Character) par3Entity).isRobot())) {
				par1ItemStack.setDamageValue(Math.min(maxdamage, par1ItemStack.getDamageValue() + 3));
				if (par5 || par1ItemStack == ((LivingEntity) par3Entity).getOffhandItem())
					try {
						((NonNullList<ItemStack>) ReflectionAccess.entityHandInv.get(par3Entity)).get(par5 ? 0 : 1)
								.setDamageValue(par1ItemStack.getDamageValue());
					} catch (Exception e) {}
			}

			if (par1ItemStack.getTag().getBoolean("Strange") && par3Entity.tickCount % 20 == 0) {
				// par1ItemStack.getTagCompound().setInteger("CloakTicks",
				// par1ItemStack.getTagCompound().getInteger("CloakTicks") + 1);
				// if (par1ItemStack.getTagCompound().getInteger("CloakTicks") % 20 == 0)
				TF2EventsCommon.onStrangeUpdate(par1ItemStack, (LivingEntity) par3Entity);
			}
			if (par1ItemStack.getDamageValue() >= maxdamage) {
				par1ItemStack.setDamageValue(maxdamage);
				this.setCloak(false, par1ItemStack, (LivingEntity) par3Entity, par2World);
			}

		} else if (par1ItemStack.getTag().getBoolean("Active")
				&& !WeaponsCapability.get(par3Entity).isInvisible())
			par1ItemStack.getTag().putBoolean("Active", false);
		else if (par3Entity.tickCount % 2 == 0) {
			par1ItemStack
					.setDamageValue(Math.max(
							par1ItemStack.getDamageValue() - (int) TF2Attribute.getModifier("Effect Duration",
									par1ItemStack, TF2Attribute.getModifier("Charge", par1ItemStack, 2, null), null),
							0));
			if (par5 || par1ItemStack == ((LivingEntity) par3Entity).getOffhandItem())
				try {
					((NonNullList<ItemStack>) ReflectionAccess.entityHandInv.get(par3Entity)).get(par5 ? 0 : 1)
							.setDamageValue(par1ItemStack.getDamageValue());
				} catch (Exception e) {}
		}
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return (int) TF2Attribute.getModifier("Effect Duration", stack, 600, null);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		ItemStack stack = living.getItemInHand(hand);
		if (ItemToken.allowUse(living, TF2Class.SPY)) {
			if (living.isInvisible()
					|| (!isFeignDeath(stack, living) && stack.getDamageValue() < this.getMaxDamage(stack) - 72)) {
				this.setCloak(!WeaponsCapability.get(living).isInvisible(), stack, living, world);
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			} else if (!living.isInvisible() && this.isFeignDeath(stack, living) && stack.getDamageValue() == 0) {
				WeaponsCapability.get(living).setFeign(!WeaponsCapability.get(living).isFeign());
				if (WeaponsCapability.get(living).isFeign())
					living.playSound(getSound(stack, PropertyType.CHARGE_SOUND), 1.0f, 1.0f);
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

	public boolean isFeignDeath(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 1;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player) {
		if (item.getTag().getBoolean("Active"))
			this.setCloak(false, item, player, player.level);
		return super.onDroppedByPlayer(item, player);
	}

	public static Tuple<Integer, ItemStack> searchForWatches(LivingEntity living) {
		if (living instanceof EntitySpy)
			return new Tuple<>(3, ((EntitySpy) living).loadout.getStackInSlot(3));
		if (living instanceof Player) {
			Player player = (Player) living;
			if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() instanceof ItemCloak
					&& player.getOffhandItem().getTag().getBoolean("Active"))
				// System.out.println("Found offhand");
				return new Tuple<>(40, player.getOffhandItem());
			for (int i = 0; i < player.getInventory().items.size(); i++) {
				ItemStack stack = player.getInventory().items.get(i);
				if (!stack.isEmpty() && stack.getItem() instanceof ItemCloak
						&& stack.getTag().getBoolean("Active"))
					// System.out.println("Found hand");
					return new Tuple<>(i, stack);
			}

		}
		return new Tuple<>(-1, ItemStack.EMPTY);
	}

	public static ItemStack getFeignDeathWatch(LivingEntity living) {
		ItemStack stack = living.getMainHandItem();
		if (stack.getItem() instanceof ItemCloak && ((ItemCloak) stack.getItem()).isFeignDeath(stack, living)
				&& stack.getDamageValue() == 0) {
			return stack;
		} else {
			stack = living.getOffhandItem();
			if (stack.getItem() instanceof ItemCloak && ((ItemCloak) stack.getItem()).isFeignDeath(stack, living)
					&& stack.getDamageValue() == 0) {
				return stack;
			}
			IItemHandler items = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
			for (int i = 0; i < items.getSlots(); i++) {
				stack = items.getStackInSlot(i);
				if (stack.getItem() instanceof ItemCloak && ((ItemCloak) stack.getItem()).isFeignDeath(stack, living)
						&& stack.getDamageValue() == 0) {
					return stack;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public void setCloak(boolean active, ItemStack stack, LivingEntity living, Level world) {
		// System.out.println("set active: "+active);
		if (!active || !(living instanceof Player) || searchForWatches(living).getSecond().isEmpty()) {
			if (!active) {
				living.setInvisible(false);
				living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks = 20;
			}
			// System.out.println("ok: "+active);
			stack.getTag().putBoolean("Active", active);
			WeaponsCapability.get(living).setInvisible(active);

			// setInvisiblity(living);
			if (active) {
				living.playSound(ItemFromData.getSound(stack, PropertyType.CLOAK_SOUND), 1.5f, 1);
				stack.setDamageValue(Math.min(this.getMaxDamage(stack), stack.getDamageValue() + this.getMaxDamage(stack)
						- (int) TF2Attribute.getModifier("Cloak Drain", stack, this.getMaxDamage(stack), living)));
			} else {
				living.playSound(ItemFromData.getSound(stack, PropertyType.DECLOAK_SOUND), 1.5f, 1);
				if (this.isFeignDeath(stack, living))
					living.setSilent(false);
			}
			if (!world.isClientSide) {
				// TF2weapons.sendTracking(new
				// TF2Message.PropertyMessage("IsCloaked",
				// (byte)(active?1:0),living),living);
			}
		}
	}

	public static void setInvisiblity(LivingEntity living) {
		boolean cloaked = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks >= 20;
		boolean disguised = WeaponsCapability.get(living).isDisguised();
		living.setInvisible(cloaked || (disguised && living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks == 0
				&& !WeaponsCapability.get(living).isInvisible()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, world, tooltip, advanced);

		tooltip.add("Charge: "
				+ (100 - (int) (100 * ((float) stack.getDamageValue() / (float) this.getMaxDamage(stack)))) + "%");
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return true;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		return new String[] { "CLOAK",
				(100 - (int) (100 * ((float) stack.getDamageValue() / (float) this.getMaxDamage(stack)))) + "%" };
	}
}
