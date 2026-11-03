package rafradek.TF2weapons.item;

import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemJar extends ItemProjectileWeapon {

	public ItemJar() {
		super();
		this.setMaxStackSize(64);
		this.addPropertyOverride(new ResourceLocation("empty"), new LegacyItem.PropertyGetter() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float get(ItemStack stack, Level world, LivingEntity entityIn) {
				if (stack.getTag().getBoolean("IsEmpty"))
					return 1;
				return 0;
			}
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CreativeModeTab getCreativeTab() {
		return TF2weapons.tabutilitytf2;
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return !stack.getTag().getBoolean("IsEmpty") && super.canFire(world, living, stack)
				&& !(living instanceof Player && ((Player) living).getCooldowns().isOnCooldown(this));
	}

	@Override
	public Component getName(ItemStack stack) {
		String string = super.getName(stack).getString();
		if (stack.hasTag() && stack.getTag().getBoolean("IsEmpty"))
			string = "Empty Jar - ".concat(string);
		return Component.literal(string);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		if (stack.hasTag() && stack.getTag().getBoolean("IsEmpty"))
			tooltip.add(Component.literal("Right click to fill the container"));
		super.appendHoverText(stack, world, tooltip, advanced);
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		if (super.use(stack, living, world, hand, message) && !world.isClientSide) {
			if (living instanceof Player && !((Player) living).getAbilities().instabuild
					&& !TF2ConfigVars.freeUseItems)
				stack.shrink(1);
			if (living instanceof Player)
				((Player) living).getCooldowns().addCooldown(this, (int) (this.getFiringSpeed(stack, living)
						/ 50
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		}
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isBarVisible(ItemStack stack) {
		Integer value = Minecraft.getInstance().player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool
				.get(getData(stack).getName());
		return stack.getTag().getBoolean("IsEmpty") && value != null && value > 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * (1f - (float) this.getDurabilityForDisplayLegacy(stack)));
	}

	public double getDurabilityForDisplayLegacy(ItemStack stack) {
		Integer value = Minecraft.getInstance().player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool
				.get(getData(stack).getName());
		return (double) (value != null ? value : 0) / (double) 1600;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.inventoryTick(stack, par2World, par3Entity, par4, par5);
		if (!par2World.isClientSide && par3Entity instanceof Player && stack.getTag().getBoolean("IsEmpty")) {
			Integer value = WeaponsCapability.get(par3Entity).effectsCool.get(getData(stack).getName());
			if (value == null || value <= 0) {
				ItemStack newStack = stack.copy();
				newStack.setCount(1);
				newStack.getTag().remove("IsEmpty");
				String name = getData(stack).getName();
				if (((Player) par3Entity).getInventory().add(newStack) || stack.getCount() == 1) {
					if (stack.getCount() == 1)
						((Player) par3Entity).getInventory().setItem(par4, newStack);
					stack.shrink(1);
					WeaponsCapability.get(par3Entity).addEffectCooldown(name, 1600);
				}
			}
		}
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}
}
