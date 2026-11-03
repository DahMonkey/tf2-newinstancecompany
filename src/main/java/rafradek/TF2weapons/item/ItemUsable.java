package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ItemUsable extends ItemFromData {
	// public ConfigCategory data;
	// public String render;
	public static int sps;
	// public static int tickleft;
	// public static boolean addedIcons;
	// public static ThreadLocalMap<EntityLivingBase, NBTTagCompound>
	// itemProperties=new ThreadLocalMap<EntityLivingBase, NBTTagCompound>();

	public ItemUsable() {
		super();
		this.setCreativeTab(TF2weapons.tabweapontf2);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getItemInHand(hand);
		return new InteractionResultHolder<>((this.canAltFire(world, player, itemStackIn)
				&& this.getAltFiringSpeed(itemStackIn, player) != Short.MAX_VALUE) || TF2ConfigVars.swapAttackButton
				|| player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getPrimaryCooldown() > 0
						? InteractionResult.SUCCESS
						: InteractionResult.PASS,
				itemStackIn);
	}

	public abstract boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message);

	public boolean startUse(ItemStack stack, LivingEntity living, Level world, int oldState, int newState) {
		living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).pressedStart = true;
		return false;
	}

	public boolean endUse(ItemStack stack, LivingEntity living, Level world, int oldState, int newState) {
		if ((oldState & 1) == 1 && (newState & 1) == 0)
			WeaponsCapability.get(living).hitNoMiss = 0;
		return false;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(stack, par2World, par3Entity, par4, par5);
		if (stack.isEmpty())
			return;
		/*
		 * if(itemProperties.get(par2World.isRemote).get(par3Entity)==null){
		 * itemProperties.get(par2World.isRemote).put((EntityLivingBase) par3Entity, new
		 * NBTTagCompound()); }
		 */
		WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
		WeaponData.WeaponDataCapability stackcap = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null);
		LivingEntity living = (LivingEntity) par3Entity;

		int mincool = 0;

		if (living instanceof Player && !par2World.isClientSide)
			mincool = WeaponsCapability.PLAYER_MINCOOL;

		if (stackcap.fire1Cool > mincool)
			stackcap.fire1Cool -= 50;
		else
			stackcap.fire1Cool = mincool;

		if (stackcap.fire2Cool > mincool)
			stackcap.fire2Cool -= 50;
		else
			stackcap.fire2Cool = mincool;
		boolean offhand = stack == living.getOffhandItem();
		boolean doublewield = offhand && isDoubleWielding(living);
		if (stackcap.active == 0 && (par5 || doublewield)) {
			stackcap.active = 1;
			// itemProperties.get(par2World.isRemote).get(par3Entity).setShort("reloadd",
			// (short) 800);

			if (!par2World.isClientSide && living.getAttributes().getInstance(Attributes.MAX_HEALTH)
					.getModifier(ItemWeapon.HEALTH_MODIFIER) != null) {
				float addHealth = (float) living.getAttributes()
						.getInstance(Attributes.MAX_HEALTH)
						.getModifier(ItemWeapon.HEALTH_MODIFIER).getAmount();
				living.setHealth((living.getMaxHealth()) / (living.getMaxHealth() - addHealth) * living.getHealth());
			}
			stackcap.fire1Cool = Math.max(stackcap.fire1Cool, this.getDeployTime(stack, living));
			stackcap.fire2Cool = Math.max(stackcap.fire2Cool, this.getDeployTime(stack, living));
		} else if (stackcap.active > 0 && ((offhand && !doublewield) || (!offhand && !par5))) {
			cap.setInactiveHand(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, stack);
			cap.lastWeapon = stack;
		}
		if (par3Entity.tickCount % 5 == 0 && stackcap.active == 2
				&& TF2Attribute.getModifier("Mark Death", stack, 0, living) > 0)
			living.addEffect(new MobEffectInstance(TF2weapons.markDeath,
					(int) TF2Attribute.getModifier("Mark Death", stack, 0, living) * 20));
	}

	public void draw(WeaponsCapability weaponsCapability, ItemStack stack, LivingEntity living, Level world) {
		if (living instanceof ServerPlayer)
			TF2weapons.network.sendTo(
					new TF2Message.UseMessage(-1, false, this.getAmmoAmount(living, stack), InteractionHand.MAIN_HAND),
					(ServerPlayer) living);
	}

	public void holster(WeaponsCapability cap, ItemStack stack, LivingEntity living, Level world) {
		cap.chargeTicks = 0;
		cap.setCharging(false);
	}

	public static double calculateModifiers(AttributeInstance attribute, UUID except, double initial,
			double additionToMult) {
		double initialO = initial;
		for (AttributeModifier modifier : attribute.getModifiersByOperation(0)) {
			if (!modifier.getID().equals(except)) {
				initial += initialO * modifier.getAmount() * additionToMult;
			}
		}
		for (AttributeModifier modifier : attribute.getModifiersByOperation(1)) {
			if (!modifier.getID().equals(except)) {
				initial += initialO * modifier.getAmount();
			}
		}
		for (AttributeModifier modifier : attribute.getModifiersByOperation(2)) {
			if (!modifier.getID().equals(except)) {
				initial *= 1 + modifier.getAmount();
			}
		}
		return initial;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player) {
		/*
		 * item.getTagCompound().removeTag("active");
		 * player.getCapability(TF2weapons.WEAPONS_CAP, null).state = 0;
		 * if(!player.world.isRemote) TF2weapons.network.sendTo(new
		 * TF2Message.WeaponDroppedMessage(ItemFromData.getData(item).getName()),
		 * (EntityPlayerMP) player);
		 * this.holster(player.getCapability(TF2weapons.WEAPONS_CAP, null), item,
		 * player, player.world);
		 */
		return true;
	}

	public boolean canFireInternal(Level world, LivingEntity living, ItemStack stack, InteractionHand hand) {
		if (world.isClientSide && living != Minecraft.getInstance().player)
			return WeaponsCapability.get(living).canFire(hand, true);
		return canFire(world, living, stack);
	}

	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {

		return stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active > 0
				&& ItemToken.allowUse(living, this.getUsableClasses(stack))
				&& (living.getUseItem().isEmpty() || this.getDoubleWieldBonus(stack, living) != 1)
				&& this.getFiringSpeed(stack, living) != Integer.MAX_VALUE;
	}

	public Set<TF2Class> getUsableClasses(ItemStack stack) {
		if ((getData(stack).get(PropertyType.SLOT).isEmpty()) && getData(stack).hasProperty(PropertyType.BASED_ON)) {
			stack = getNewStack(getData(stack).getString(PropertyType.BASED_ON));
		}
		return getData(stack).get(PropertyType.SLOT).keySet().stream().map(TF2Class::getClass).collect(Collectors.toSet());
	}

	public abstract boolean fireTick(ItemStack stack, LivingEntity living, Level world);

	public abstract boolean altFireTick(ItemStack stack, LivingEntity living, Level world);

	/*
	 * public void registerIcons(IIconRegister par1IconRegister) { this.itemIcon =
	 * par1IconRegister.registerIcon(this.getIconString()); if(addedIcons) return;
	 * Iterator<String> iterator=MapList.nameToCC.keySet().iterator();
	 * addedIcons=true; while(iterator.hasNext()){ String name=iterator.next();
	 * //System.out.println(MapList.nameToCC.get(name).get("Render").getString()
	 * +" "+name); MapList.nameToIcon.put(name,
	 * par1IconRegister.registerIcon(MapList.nameToCC.get(name).get("Render").
	 * getString())); } //this.itemIcon =
	 * par1IconRegister.registerIcon(getData(stack).get("Render").getString()); }
	 */
	/*
	 * @SideOnly(Side.CLIENT) public boolean requiresMultipleRenderPasses() { return
	 * true; } public int getRenderPasses(int metadata) { return 1; }
	 */

	public int getFiringSpeed(ItemStack stack, LivingEntity living) {
		int speed = (int) (TF2Attribute.getModifier("Fire Rate", stack,
				ItemFromData.getData(stack).getInt(PropertyType.FIRE_SPEED), living));
		if (living != null && isDoubleWielding(living))
			speed *= this.getDoubleWieldBonus(stack, living) * 2;
		if (TF2Attribute.getModifier("Fire Rate Health", stack, 1f, living) != 1f)
			speed *= this.getHealthBasedBonus(stack, living,
					TF2Attribute.getModifier("Fire Rate Health", stack, 1f, living));
		if (living != null && (WeaponsCapability.get(living).isExpJump() || living.isFallFlying())
				&& TF2Attribute.getModifier("Airborne Bonus", stack, 0, living) != 0)
			speed *= 0.35f;
		if (living != null && living.getAttribute(Attributes.ATTACK_SPEED) != null) {
			// System.out.println("Pre speed "+speed+"
			// "+living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue());
			double modifiers = calculateModifiers(living.getAttribute(Attributes.ATTACK_SPEED),
					ATTACK_SPEED_MODIFIER,
					living.getAttribute(Attributes.ATTACK_SPEED).getBaseValue(), 1.4);
			speed *= (living instanceof Player ? 4
					: living.getAttribute(Attributes.ATTACK_SPEED).getBaseValue()) / modifiers;
			// System.out.println("Post speed "+speed);
		}
		if (speed <= 0) {
			return Integer.MAX_VALUE;
		}
		return speed;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
		return (player.level.isClientSide && !TF2ConfigVars.swapAttackButton)
				|| (!player.level.isClientSide && !TF2PlayerCapability.get(player).breakBlocks);
	}

	public static boolean isDoubleWielding(LivingEntity living) {
		return ItemFromData.getData(living.getMainHandItem()) != ItemFromData.BLANK_DATA
				&& living.getMainHandItem().getItem() instanceof ItemUsable
				&& living.getOffhandItem().getItem() instanceof ItemUsable
				&& ((ItemUsable) living.getOffhandItem().getItem()).getDoubleWieldBonus(living.getOffhandItem(),
						living) != 1
				&& ((ItemUsable) living.getMainHandItem().getItem())
						.getDoubleWieldBonus(living.getMainHandItem(), living) != 1;
	}

	public float getDoubleWieldBonus(ItemStack stack, LivingEntity living) {
		// System.out.println("Double wield type:
		// "+ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED)+"
		// "+ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED));
		return !ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED) ? 1f
				: ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED);
	}

	public boolean canAltFireInternal(Level worldObj, LivingEntity player, ItemStack item, InteractionHand hand) {
		if (worldObj.isClientSide && player != Minecraft.getInstance().player)
			return WeaponsCapability.get(player).canFire(hand, false);
		return canAltFire(worldObj, player, item);
	}

	public boolean canAltFire(Level worldObj, LivingEntity player, ItemStack item) {
		return item.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active > 0
				&& player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks == 0
				&& ItemToken.allowUse(player, this.getUsableClasses(item))
				&& (player.getUseItem().isEmpty() || this.getDoubleWieldBonus(item, player) != 1);
	}

	public void altUse(ItemStack stack, LivingEntity living, Level world) {

	}

	public float getHealthBasedBonus(ItemStack item, LivingEntity living, float maxbonus) {
		if (living != null && living.getHealth() < living.getMaxHealth() * 0.8f) {
			float multiplier = Mth
					.clamp(TF2Util.position(0.1f, 0.8f, living.getHealth() / living.getMaxHealth()), 0f, 1f);

			return TF2Util.lerp(1, maxbonus, multiplier);
		}
		return 1f;
	}

	public short getAltFiringSpeed(ItemStack item, LivingEntity player) {
		return Short.MAX_VALUE;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent() && !slotChanged) {
			newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active = oldStack
					.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active;
			newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire1Cool = oldStack
					.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire1Cool;
			newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire2Cool = oldStack
					.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire2Cool;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	public int getDeployTime(ItemStack stack, LivingEntity living) {
		return (int) TF2Attribute.getModifier("Deploy Time", stack, 750, living);
	}

	public int getStateOverride(ItemStack stack, LivingEntity living, int original) {
		if (TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0) {
			// System.out.println("Act pre: "+original);
			boolean fire = ((original & 1) == 0) || WeaponsCapability.get(living).autoFire;
			if (fire) {
				return 1;
			} else {
				return 4;
			}
			// System.out.println("Act post: "+original);
		}
		return original;
	}

	public boolean shouldEntityFire(ItemStack stack, LivingEntity living, LivingEntity target) {
		/*
		 * if(TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0) { return
		 * stack.getItemDamage() > 0 && (stack.getItemDamage() == stack.getMaxDamage()
		 * || WeaponsCapability.get(living).reloadingHand != null); }
		 */

		return true;
	}

	@Override
	public boolean canSwitchTo(ItemStack stack) {
		return true;
	}
}
