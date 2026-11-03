package rafradek.TF2weapons.item;

import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;

import rafradek.TF2weapons.client.ItemMeshDefinition;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.TF2Attribute.State;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class ItemFromData extends LegacyItem implements IItemOverlay {

	public static class PropertyAttribute extends PropertyType<AttributeProvider> {

		public PropertyAttribute(int id, String name, Class<AttributeProvider> type) {
			super(id, name, type);
		}

		@Override
		public AttributeProvider deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			HashMap<TF2Attribute, Float> attributes = new HashMap<>();
			for (Entry<String, JsonElement> attribute : json.getAsJsonObject().entrySet()) {
				String attributeName = attribute.getKey();
				float attributeValue = attribute.getValue().getAsFloat();
				Iterator<String> iterator2 = MapList.nameToAttribute.keySet().iterator();
				// System.out.println("to je"+attributeName+"
				// "+attributeValue);
				boolean has = false;

				while (iterator2.hasNext())
					if (iterator2.next().equals(attributeName)) {
						attributes.put(MapList.nameToAttribute.get(attributeName), attributeValue);
						has = true;
					}
				if (has == false)
					attributes.put(TF2Attribute.attributes[Integer.parseInt(attributeName)], attributeValue);
			}
			return new AttributeProvider(attributes);
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, AttributeProvider value) throws IOException {
			buf.writeByte(value.attributes.size());
			for (Entry<TF2Attribute, Float> attr : value.attributes.entrySet()) {
				buf.writeByte(attr.getKey().id);
				buf.writeFloat(attr.getValue());
			}
		}

		@Override
		public AttributeProvider deserialize(DataInput buf, WeaponData data) throws IOException {
			HashMap<TF2Attribute, Float> map = new HashMap<>();
			int attributeCount = buf.readByte();
			for (int i = 0; i < attributeCount; i++) {
				map.put(TF2Attribute.attributes[buf.readUnsignedByte()], buf.readFloat());
			}
			return new AttributeProvider(map);
		}
	}

	public static class AttributeProvider {
		public Map<TF2Attribute, Float> attributes;

		public AttributeProvider(Map<TF2Attribute, Float> attributes) {
			this.attributes = attributes;
		}
	}

	public static final WeaponData BLANK_DATA = new WeaponData("toloadfiles");
	public static final Predicate<WeaponData> VISIBLE_WEAPON = input -> !input.getBoolean(PropertyType.HIDDEN)
			&& input.getInt(PropertyType.ROLL_HIDDEN) == 0 && !input.getString(PropertyType.CLASS).equals("cosmetic")
			&& !input.getString(PropertyType.CLASS).equals("crate");

	public ItemFromData() {
		this.setCreativeTab(TF2weapons.tabutilitytf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2usable");
		this.setMaxStackSize(1);
		this.setNoRepair();
		this.addPropertyOverride(new ResourceLocation("team"),
				(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) -> {
					if (entityIn != null) {
						if (TF2Util.getTeamColorNumber(entityIn) == 12)
							return 0;
						else if (TF2Util.getTeamColorNumber(entityIn) == 9
								|| TF2Util.getTeamColorNumber(entityIn) == 11)
							return 1f;
						return 0.5f;
					}
					return 0;
				});
	}

	@Override
	public void inventoryTick(ItemStack stack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		if (getData(stack) == BLANK_DATA && par3Entity instanceof Player) {
			((Player) par3Entity).getInventory().setItem(par4, ItemStack.EMPTY);
			stack.setCount(0);
			return;
		}
	}

	public static WeaponData getData(ItemStack stack) {
		WeaponData value = BLANK_DATA;
		if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent()) {
			value = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).inst;
			if (value == BLANK_DATA && stack.hasTag()
					&& MapList.nameToData.containsKey(stack.getTag().getString("Type"))) {
				value = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).inst = MapList.nameToData
						.get(stack.getTag().getString("Type"));
			}
		}
		return value;
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs) && par2CreativeTabs != TF2weapons.tabrareweapontf2)
			return;
		Iterator<Entry<String, WeaponData>> iterator = MapList.nameToData.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, WeaponData> entry = iterator.next();
			// System.out.println("Hidden:
			// "+entry.getValue().hasProperty(PropertyType.HIDDEN));
			boolean isRare = entry.getValue().getInt(PropertyType.ROLL_HIDDEN) == 2;
			if (entry.getValue().hasProperty(PropertyType.HIDDEN) && entry.getValue().getBoolean(PropertyType.HIDDEN))
				continue;
			if ((isRare && par2CreativeTabs == TF2weapons.tabrareweapontf2)
					|| (!isRare && par2CreativeTabs != TF2weapons.tabrareweapontf2)) {
				Item item = MapList.weaponClasses.get(entry.getValue().getString(PropertyType.CLASS));
				if (item == this)
					par3List.add(ItemFromData.getNewStack(entry.getKey()));
			}
		}
	}

	public static ItemStack getNewStack(String type) {
		// "+MapList.weaponClasses.get(MapList.nameToCC.get(type).get("Class").getString())+"
		// "+Thread.currentThread().getName());
		if (!MapList.nameToData.containsKey(type))
			return ItemStack.EMPTY;

		return getNewStack(MapList.nameToData.get(type));
	}

	public static ItemStack getNewStack(WeaponData type) {
		ItemStack stack = new ItemStack(MapList.weaponClasses.get(type.getString(PropertyType.CLASS)));
		// System.out.println(stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null));
		stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).inst = type;
		CompoundTag tag = new CompoundTag();
		tag.putString("Type", type.getName());
		tag.put("Attributes", new CompoundTag());
		stack.setTag(tag);
		// System.out.println(stack.toString());
		return stack;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack,
			CompoundTag nbt) {
		return new WeaponData.WeaponDataCapability();
	}

	public static List<ItemStack> getRandomWeapons(RandomSource random, Predicate<WeaponData> predicate, int count) {

		ArrayList<WeaponData> weapons = new ArrayList<>();
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet())
			if (predicate.apply(entry.getValue())) {
				weapons.add(entry.getValue());
			}
		ArrayList<ItemStack> ret = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			if (weapons.isEmpty())
				break;
			ret.add(getNewStack(weapons.remove(random.nextInt(weapons.size()))));
		}
		return ret;
	}

	public static ItemStack getRandomWeapon(RandomSource random, Predicate<WeaponData> predicate) {
		return Iterables.getFirst(getRandomWeapons(random, predicate, 1), ItemStack.EMPTY);
	}

	public static ItemStack getRandomWeaponOfType(String type, float chanceOfParent, RandomSource random) {
		// WeaponData parent=MapList.nameToData.get(type);
		if (chanceOfParent >= 0 && random.nextFloat() <= chanceOfParent)
			return getNewStack(type);
		else {
			ArrayList<String> weapons = new ArrayList<>();
			if (chanceOfParent < 0)
				weapons.add(type);
			for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet())
				if (!entry.getValue().getBoolean(PropertyType.HIDDEN)
						&& entry.getValue().getInt(PropertyType.ROLL_HIDDEN) == 0
						&& entry.getValue().getString(PropertyType.BASED_ON).equals(type))
					weapons.add(entry.getKey());
			if (weapons.size() > 0)
				return getNewStack(weapons.get(random.nextInt(weapons.size())));
			else
				return getNewStack(type);
		}

	}

	public static ItemStack getRandomWeaponOfType(String name, RandomSource random, boolean showHidden) {
		ArrayList<WeaponData> weapons = new ArrayList<>();
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet())
			if (!entry.getValue().getBoolean(PropertyType.HIDDEN)
					&& (showHidden || entry.getValue().getInt(PropertyType.ROLL_HIDDEN) == 0)
					&& entry.getValue().getString(PropertyType.CLASS).equals(name))
				weapons.add(entry.getValue());
		if (weapons.isEmpty())
			return ItemStack.EMPTY;
		return getNewStack(weapons.get(random.nextInt(weapons.size())));
	}

	public static ItemStack getRandomWeaponOfSlotMob(final TF2Class clazz, final int slot, RandomSource random,
													 final boolean showHidden, float stockWeight, boolean stockOnly) {
		Predicate<WeaponData> base = input -> !input.getBoolean(PropertyType.HIDDEN)
				&& !(input.getInt(PropertyType.ROLL_HIDDEN) > 0 && !showHidden)
				&& ItemFromData.isItemOfClassSlot(input, slot, clazz);

		if (stockWeight == 1f && !stockOnly)
			return getRandomWeapon(random, base);

		ItemStack stock = getRandomWeapon(random,
				Predicates.and(base, (Predicate<WeaponData>) input -> input.getBoolean(PropertyType.STOCK)));

		if (stockOnly)
			return stock;
		Predicate<WeaponData> unipredicate = Predicates.and(base,
				(Predicate<WeaponData>) input -> !input.getBoolean(PropertyType.STOCK));
		float unicount = getWeaponCount(unipredicate);
		ItemStack uni = getRandomWeapon(random, unipredicate);
		if (uni.isEmpty()) {
			return stock;
		} else if (stock.isEmpty()) {
			return uni;
		} else if (random.nextFloat() < unicount / (unicount + stockWeight)) {
			return uni;
		} else {
			return stock;
		}
	}

	public static List<ItemStack> getRandomWeaponsOfSlotMob(final TF2Class clazz, final int slot, RandomSource random,
															final boolean showHidden, int count) {
		return getRandomWeapons(random,
				input -> !input.getBoolean(PropertyType.HIDDEN)
						&& !(input.getInt(PropertyType.ROLL_HIDDEN) > 0 && !showHidden)
						&& ItemFromData.isItemOfClassSlot(input, slot, clazz),
				count);
	}

	public static List<ItemStack> getRandomWeapons(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		TF2Class clazz = TF2Class.NONE;
		if (nbt != null && nbt.contains("Token")) clazz = TF2Class.getClass(nbt.getByte("Token"));
		List<ItemStack> weapons = Lists.newArrayList();
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
			if (entry.getValue().getBoolean(PropertyType.HIDDEN)) continue;
			String weaponclass = entry.getValue().getString(PropertyType.CLASS);
			if ((stack.getDamageValue() == 10 && weaponclass.equals("cosmetic")) || (stack.getDamageValue() == 9 && (clazz == TF2Class.NONE ||
					ItemFromData.isItemOfClass(entry.getValue(), clazz)) &! weaponclass.equals("cosmetic") &! weaponclass.equals("crate")))
				weapons.add(getNewStack(entry.getValue()));
		}
		return weapons;
	}

	public static ItemStack getRandomWeapon(ItemStack stack, RandomSource random) {
		CompoundTag nbt = stack.getTag();
		TF2Class clazz = TF2Class.NONE;
		if (nbt != null && nbt.contains("Token")) clazz = TF2Class.getClass(nbt.getByte("Token"));
		List<WeaponData> weapons = Lists.newArrayList();
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
			if (entry.getValue().getBoolean(PropertyType.HIDDEN)) continue;
			String weaponclass = entry.getValue().getString(PropertyType.CLASS);
			if ((stack.getDamageValue() == 10 && weaponclass.equals("cosmetic")) || (stack.getDamageValue() == 9 && (clazz == TF2Class.NONE ||
					ItemFromData.isItemOfClass(entry.getValue(), clazz)) &! weaponclass.equals("cosmetic") &! weaponclass.equals("crate")))
				weapons.add(entry.getValue());
		}
		if (weapons.isEmpty())
			return ItemStack.EMPTY;
		ItemStack result = getNewStack(weapons.get(random.nextInt(weapons.size())));
		if (!result.hasTag()) result.setTag(new CompoundTag());
		if (nbt.contains("Australium") && nbt.getBoolean("Australium")) result.getTag().putBoolean("Australium", true);
		return result;
	}

	public static ItemStack getDisplayWeapon(ItemStack stack, long ticks) {
		CompoundTag nbt = stack.getTag();
		TF2Class clazz = TF2Class.NONE;
		if (nbt != null && nbt.contains("Token")) clazz = TF2Class.getClass(nbt.getByte("Token"));
		List<WeaponData> weapons = Lists.newArrayList();
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
			if (entry.getValue().getBoolean(PropertyType.HIDDEN)) continue;
			String weaponclass = entry.getValue().getString(PropertyType.CLASS);
			if ((stack.getDamageValue() == 10 && weaponclass.equals("cosmetic")) || (stack.getDamageValue() == 9 && (clazz == TF2Class.NONE ||
					ItemFromData.isItemOfClass(entry.getValue(), clazz)) &! weaponclass.equals("cosmetic") &! weaponclass.equals("crate")))
				weapons.add(entry.getValue());
		}
		if (weapons.isEmpty())
			return ItemStack.EMPTY;
		return getNewStack(weapons.get(((int)ticks % Integer.MAX_VALUE / 20) % weapons.size()));
	}

	public static int getWeaponCount(Predicate<WeaponData> predicate) {
		int count = 0;
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
			if (predicate.apply(entry.getValue()))
				count++;
		}
		return count;
	}

	public static boolean isSameType(ItemStack stack, String name) {
		return !stack.isEmpty() && getData(stack) != BLANK_DATA && (getData(stack).getName().equals(name)
				|| getData(stack).getString(PropertyType.BASED_ON).equals(name));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return getData(oldStack) != getData(newStack) || (slotChanged);

	}

	@Override
	public Component getName(ItemStack stack) {
		if (ItemFromData.getData(stack) == BLANK_DATA)
			return Component.literal("Weapon");
		String name = getTranslatedName(stack);
		if (stack.getTag().getBoolean("Strange"))
			name = ChatFormatting.GOLD
					+ TF2EventsCommon.STRANGE_TITLES[stack.getTag().getInt("StrangeLevel")] + " " + name;
		if (stack.getTag().getBoolean("Australium"))
			name = ChatFormatting.GOLD + "Australium " + name;
		if (stack.getTag().getBoolean("Valve"))
			name = ChatFormatting.DARK_PURPLE + "Valve " + name;
		return Component.literal(name);
	}

	public static SoundEvent getSound(ItemStack stack, PropertyType<String> name) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(getData(stack).getString(name)));
	}

	public static int getSlotForClass(WeaponData data, TF2Class clazz) {
		return data.hasProperty(PropertyType.SLOT) && data.get(PropertyType.SLOT).containsKey(clazz.getName())
				? data.get(PropertyType.SLOT).get(clazz.getName())
				: -1;
	}

	public static int getSlotForClass(WeaponData data, EntityTF2Character entity) {
		return getSlotForClass(data, entity.getTF2Class());
	}

	public static boolean isItemOfClassSlot(WeaponData data, int slot, TF2Class clazz) {
		return data.hasProperty(PropertyType.SLOT) && data.get(PropertyType.SLOT).containsKey(clazz.getName())
				&& data.get(PropertyType.SLOT).get(clazz.getName()) == slot;
	}

	public static boolean isItemOfClass(WeaponData data, TF2Class clazz) {
		return data.hasProperty(PropertyType.SLOT) && data.get(PropertyType.SLOT).containsKey(clazz.getName());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		/*
		 * if (!par1ItemStack.hasTagCompound()) { par1ItemStack.getTagCompound()=new
		 * NBTTagCompound(); par1ItemStack.getTagCompound().setTag("Attributes",
		 * (NBTTagCompound)
		 * ((ItemUsable)par1ItemStack.getItem()).buildInAttributes.copy()); }
		 */
		if (stack.hasTag()) {
			CompoundTag attributeList = stack.getTag().getCompound("Attributes");
			// attributeList.merge(MapList.buildInAttributes.get(getData(par1ItemStack).getName()));
			Iterator<String> iterator = attributeList.getAllKeys().iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				Tag tag = attributeList.get(name);
				if (tag instanceof FloatTag) {
					FloatTag tagFloat = (FloatTag) tag;
					TF2Attribute attribute = TF2Attribute.attributes[Integer.parseInt(name)];
					// System.out.println("Attribute id: "+name);
					if (attribute != null && attribute.state != State.HIDDEN)
						tooltip.add(Component.literal(attribute.getTranslatedString(tagFloat.getFloat(), true)));
				}
			}
			attributeList = MapList.buildInAttributes.get(getData(stack).getName());
			// attributeList.merge(MapList.buildInAttributes.get(getData(par1ItemStack).getName()));
			iterator = attributeList.getAllKeys().iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				Tag tag = attributeList.get(name);
				if (tag instanceof FloatTag) {
					FloatTag tagFloat = (FloatTag) tag;
					TF2Attribute attribute = TF2Attribute.attributes[Integer.parseInt(name)];
					// System.out.println("Attribute id: "+name);
					if (attribute != null && attribute.state != State.HIDDEN)
						tooltip.add(Component.literal(attribute.getTranslatedString(tagFloat.getFloat(), true)));
				}
			}

			if (stack.getTag().contains(NBTLiterals.STREAK_ATTRIB)) {
				tooltip.add(Component.literal("Killstreak Active"));
				TF2Attribute attribute = TF2Attribute.attributes[stack.getTag()
						.getShort(NBTLiterals.STREAK_ATTRIB)];
				if (attribute != null && attribute.state != State.HIDDEN)
					tooltip.add(Component.literal(attribute.getTranslatedString(
							ItemKillstreakKit.getKillstreakBonus(attribute,
									stack.getTag().getByte(NBTLiterals.STREAK_LEVEL),
									stack.getTag().getInt(NBTLiterals.STREAK_KILLS), getData(stack)),
							true)));
			}

			if (getData(stack).hasProperty(PropertyType.DESC)) {
				tooltip.add(Component.literal(""));
				for (String line : getData(stack).getString(PropertyType.DESC).split("\n"))
					tooltip.add(Component.literal(line));
			}
			if (stack.getTag().getBoolean("Bought")) {
				tooltip.add(Component.literal(""));
				tooltip.add(Component.literal("This item cannot be destroyed"));
			}
		}
	}

	public boolean hasKillstreak(ItemStack stack, int minLevel) {
		return stack.getTag().getByte(NBTLiterals.STREAK_LEVEL) >= minLevel;
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 12000;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return false;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		return null;
	}

	@Override
	public void drawOverlay(ItemStack stack, Player player, Tessellator tesselator, BufferBuilder buffer,
			ScaledResolution resolution) {}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return this.getMaxRage(stack, null) > 0f ? (this.getRage(stack, Minecraft.getInstance().player) < this
				.getMaxRage(stack, Minecraft.getInstance().player)) : super.showDurabilityBar(stack);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return this.getMaxRage(stack, null) > 0f
				? (1 - this.getRage(stack, Minecraft.getInstance().player)
						/ this.getMaxRage(stack, Minecraft.getInstance().player))
				: super.getDurabilityForDisplay(stack);
	}

	public RageType getRageType(ItemStack stack, LivingEntity living) {
		return null;
	}

	public float getMaxRage(ItemStack stack, LivingEntity living) {
		return 0f;
	}

	public float getRage(ItemStack stack, LivingEntity living) {
		return WeaponsCapability.get(living).getRage(this.getRageType(stack, living));
	}

	public void setRage(ItemStack stack, LivingEntity living, float value) {
		WeaponsCapability.get(living).setRage(this.getRageType(stack, living),
				Mth.clamp(value, 0f, this.getMaxRage(stack, living)));
	}

	public void addRage(ItemStack stack, LivingEntity living, float value) {
		this.setRage(stack, living, this.getRage(stack, living) + value);
	}

	public boolean isAmmoSufficient(ItemStack stack, LivingEntity living, boolean all) {
		return !this.searchForAmmo(living, stack).isEmpty();
	}

	public void consumeAmmoGlobal(LivingEntity living, ItemStack stack, int amount) {
		if (EntityDispenser.isNearDispenser(living.level, living))
			return;
		amount = this.getActualAmmoUse(stack, living, amount);
		if (living instanceof EntityTF2Character)
			((EntityTF2Character) living).useAmmo(amount);
		if (!(living instanceof Player) || ((Player) living).isCreative())
			return;
		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, living) != 0) {
			living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).consumeMetal(amount, false);
		}
		if (amount > 0) {
			// int
			// type=ItemFromData.getData(stack).getInt(PropertyType.AMMO_TYPE);

			// stack.getCount()-=amount;
			ItemStack stackAmmo;
			while (amount > 0 && !(stackAmmo = searchForAmmo(living, stack)).isEmpty()) {
				if (stackAmmo.getItem() instanceof ItemAmmo) {
					amount = ((ItemAmmo) stackAmmo.getItem()).consumeAmmo(living, stackAmmo, amount);
				} else {
					if (stackAmmo.getItem() instanceof ArrowItem)
						stack.getTag().put("LastLoaded", stackAmmo.serializeNBT());
					int ammo = amount;
					amount -= stackAmmo.getCount();
					stackAmmo.shrink(ammo);

				}

			}
		}
	}

	public ItemStack searchForAmmo(LivingEntity owner, ItemStack stack) {
		if (EntityDispenser.isNearDispenser(owner.level, owner)
				|| (owner instanceof Player && ((Player) owner).getAbilities().instabuild))
			return ItemAmmo.STACK_FILL;

		int type = ((ItemFromData) stack.getItem()).getAmmoType(stack);

		if (type == 0 || (type == 14 && TF2ConfigVars.freeUseItems))
			return ItemAmmo.STACK_FILL;

		if (owner instanceof EntityTF2Character) {
			return ((EntityTF2Character) owner)
					.getAmmo(ItemFromData.getSlotForClass(ItemFromData.getData(stack), (EntityTF2Character) owner)) > 0
							? ItemAmmo.STACK_FILL
							: ItemStack.EMPTY;
		} else if (!(owner instanceof Player))
			return ItemAmmo.STACK_FILL;
		int metalammo = (int) TF2Attribute.getModifier("Metal Ammo", stack, 0, owner);
		if (metalammo != 0) {
			return owner.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).hasMetal(metalammo) ? ItemAmmo.STACK_FILL
					: ItemStack.EMPTY;
		}

		if (owner.level.isClientSide && (type >= owner.getCapability(TF2weapons.PLAYER_CAP).orElse(null).cachedAmmoCount.length
				|| owner.getCapability(TF2weapons.PLAYER_CAP).orElse(null).cachedAmmoCount[type] > 0))
			return ItemAmmo.STACK_FILL;

		if (!owner.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3).isEmpty()) {
			IItemHandler inv = owner.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3)
					.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
			// System.out.println("Ammo Search: "+inv.getSlots());
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stackCap = inv.getStackInSlot(i);
				// System.out.println("Stack: "+stackCap);
				if (!stackCap.isEmpty() && ((stackCap.getItem() instanceof ItemAmmo
						&& ((ItemAmmo) stackCap.getItem()).getTypeInt(stackCap) == type)
						|| (type == 1000 && stackCap.getItem() instanceof ArrowItem))) {
					// System.out.println("Found: "+i);
					return stackCap;
				}
			}
		}
		for (int i = 0; i < ((Player) owner).getInventory().items.size(); i++) {
			ItemStack stackInv = ((Player) owner).getInventory().items.get(i);
			if (stackInv != null && ((stackInv.getItem() instanceof ItemAmmo
					&& ((ItemAmmo) stackInv.getItem()).getTypeInt(stackInv) == type)
					|| (type == 1000 && stackInv.getItem() instanceof ArrowItem)))
				return stackInv;
		}
		return ItemStack.EMPTY;
	}

	public int getAmmoType(ItemStack stack) {
		return TF2Attribute.getModifier("No Ammo", stack, 0, null) != 0 ? 0
				: getData(stack).getInt(PropertyType.AMMO_TYPE);
	}

	public static int getAmmoAmountType(Player owner, int type) {
		int ammoCount = 0;
		if (!owner.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3).isEmpty()) {
			IItemHandler inv = owner.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3)
					.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stackCap = inv.getStackInSlot(i);
				if (!stackCap.isEmpty() && stackCap.getItem() instanceof ItemAmmo
						&& ((ItemAmmo) stackCap.getItem()).getTypeInt(stackCap) == type) {
					// System.out.println("Found: "+i);
					ammoCount += ((ItemAmmo) stackCap.getItem()).getAmount(stackCap);
				} else if (type == 1000 && !stackCap.isEmpty() && stackCap.getItem() instanceof ArrowItem)
					ammoCount += stackCap.getCount();
			}
		}
		for (int i = 0; i < owner.getInventory().items.size(); i++) {
			ItemStack stackInv = owner.getInventory().items.get(i);
			if (!stackInv.isEmpty() && stackInv.getItem() instanceof ItemAmmo
					&& ((ItemAmmo) stackInv.getItem()).getTypeInt(stackInv) == type)
				ammoCount += ((ItemAmmo) stackInv.getItem()).getAmount(stackInv);
			else if (type == 1000 && !stackInv.isEmpty() && stackInv.getItem() instanceof ArrowItem)
				ammoCount += stackInv.getCount();
		}
		return ammoCount;
	}

	public int getAmmoAmount(LivingEntity owner, ItemStack stack) {

		int type = this.getAmmoType(stack);

		if (type == 0)
			return 999;

		if (type == 14 && owner instanceof Player && TF2ConfigVars.freeUseItems) {
			return ((Player) owner).getCooldowns().isOnCooldown(this) ? 0 : 1;
		}
		if (EntityDispenser.isNearDispenser(owner.level, owner)
				|| (owner instanceof Player && ((Player) owner).getAbilities().instabuild))
			return 999;

		if (owner instanceof EntityTF2Character)
			return (int) (((EntityTF2Character) owner).getAmmo()
					/ TF2Attribute.getModifier("Ammo Eff", stack, 1, owner));

		if (TF2Attribute.getModifier("Ball Release", stack, 0, owner) > 0)
			stack = ItemFromData.getNewStack("sandmanball");

		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, owner) != 0) {
			return owner.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal();
		}

		int ammoCount = getAmmoAmountType((Player) owner, type);

		return (int) (ammoCount / TF2Attribute.getModifier("Ammo Eff", stack, 1, owner));
	}

	public int getActualAmmoUse(ItemStack stack, LivingEntity living, int amount) {
		if (this.getAmmoType(stack) == 0 || amount == 0)
			return 0;

		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, living) != 0) {
			amount = (int) TF2Attribute.getModifier("Metal Ammo", stack, 0, living);
		}
		stack.getTag().putFloat("UsedAmmo", stack.getTag().getFloat("UsedAmmo")
				+ amount * TF2Attribute.getModifier("Ammo Eff", stack, 1, living));
		amount = 0;
		while (stack.getTag().getFloat("UsedAmmo") >= 1) {
			stack.getTag().putFloat("UsedAmmo", stack.getTag().getFloat("UsedAmmo") - 1);
			amount++;
		}
		return amount;
	}

	public int getVisibilityFlags(ItemStack stack, LivingEntity living) {
		return ItemFromData.getData(stack).getInt(PropertyType.WEAR);
	}

	public String getTranslatedName(ItemStack stack) {
		WeaponData data = getData(stack);
		String key = "weapon." + data.getName();
		return TF2Util.canTranslate(key) ? TF2Util.translate(key)
				: getData(stack).hasProperty(PropertyType.NAME) ? getData(stack).getString(PropertyType.NAME)
						: getData(stack).getName();
	}

	public boolean canSwitchTo(ItemStack stack) {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public ItemMeshDefinition getMeshDefinition() {
		return stack -> {
			if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent()) {
				if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).inst != ItemFromData.BLANK_DATA)
					return ClientProxy.nameToModel
							.get(stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).inst.getName());
				else if (stack.hasTag())
					return ClientProxy.nameToModel.get(stack.getTag().getString("Type"));
			}
			return ClientProxy.nameToModel.get("minigun");
		};
	}

	@OnlyIn(Dist.CLIENT)
	public void registerModels(WeaponData weapon) {
		String modelName = weapon.getString(PropertyType.RENDER);

		if (modelName == null || modelName.isEmpty())
			return;

		ModelResourceLocation model = new ModelResourceLocation(modelName, "inventory");
		ModelLoader.registerItemVariants(MapList.weaponClasses.get(weapon.getString(PropertyType.CLASS)), model);
		ClientProxy.nameToModel.put(weapon.getName(), model);
		if (weapon.hasProperty(PropertyType.RENDER_BACKSTAB)) {
			modelName = weapon.getString(PropertyType.RENDER_BACKSTAB);
			model = new ModelResourceLocation(modelName, "inventory");
			ModelLoader.registerItemVariants(MapList.weaponClasses.get(weapon.getString(PropertyType.CLASS)), model);
			ClientProxy.nameToModel.put(weapon.getName() + "/b", model);
		}
	}
}
