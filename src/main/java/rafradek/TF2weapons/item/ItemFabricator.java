package rafradek.TF2weapons.item;

import net.minecraft.network.chat.Component;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.function.Predicate;

public abstract class ItemFabricator extends LegacyItem {

	public static class PropertyItemList extends PropertyType<ItemList> {

		public PropertyItemList(int id, String name, Class<ItemList> type) {
			super(id, name, type);
		}

		@Override
		public ItemList deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			ItemList content = new ItemList();
			for (Entry<String, JsonElement> attribute : json.getAsJsonObject().entrySet()) {
				String itemName = attribute.getKey();
				int chance = attribute.getValue().getAsInt();
				content.items.put(itemName, chance);
			}
			return content;
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, ItemList value) throws IOException {
			buf.writeByte(value.items.size());
			for (Entry<String, Integer> entry : value.items.entrySet()) {
				buf.writeUTF(entry.getKey());
				buf.writeShort(entry.getValue());
			}
		}

		@Override
		public ItemList deserialize(DataInput buf, WeaponData data) throws IOException {
			int attributeCount = buf.readByte();
			ItemList content = new ItemList();
			for (int i = 0; i < attributeCount; i++) {
				String entry = buf.readUTF();
				int value = buf.readShort();
				content.items.put(entry, value);
			}
			return content;
		}
	}

	public static class ItemList {
		public HashMap<String, Integer> items = new HashMap<>();
	}

	public static abstract class TF2Ingredient implements Predicate<ItemStack> {

		public abstract String getName();

		public abstract int getCount();
	}

	public static class IngredientItemStack extends TF2Ingredient {

		public ItemStack test;

		public IngredientItemStack(ItemStack test) {
			super();
			this.test = test;
		}

		@Override
		public boolean test(ItemStack input) {
			return ItemHandlerHelper.canItemStacksStack(input, test);
		}

		@Override
		public String getName() {
			return I18n.get(test.getDescriptionId() + ".name");
		}

		@Override
		public int getCount() {
			return test.getCount();
		}

	}

	public static class IngredientPredicate extends TF2Ingredient {

		public Predicate<ItemStack> test;
		public int count;
		public String name;

		public IngredientPredicate(Predicate<ItemStack> test, int count, String name) {
			super();
			this.test = test;
			this.name = name;
			this.count = count;
		}

		@Override
		public boolean test(ItemStack input) {
			return test.test(input);
		}

		@Override
		public String getName() {
			return I18n.get(name);
		}

		@Override
		public int getCount() {
			return count;
		}

	}

	public abstract NonNullList<TF2Ingredient> getInput(ItemStack stack, Player player);

	public abstract NonNullList<ItemStack> getOutput(ItemStack stack, Player player);

	@OnlyIn(Dist.CLIENT)
	public List<String> getOutputNames(ItemStack stack) {
		ArrayList<String> list = new ArrayList<>();
		for (ItemStack output : getOutput(stack, null)) {
			list.add(output.getCount() + "x " + I18n.get(output.getDescriptionId() + ".name"));
		}
		return list;
	}

	@OnlyIn(Dist.CLIENT)
	public List<String> getInputNames(ItemStack stack) {
		ArrayList<String> list = new ArrayList<>();
		for (TF2Ingredient input : getInput(stack, null)) {
			list.add(input.getCount() + "x " + input.getName());
		}
		return list;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		tooltip.add(Component.literal("When used, this item will produce"));
		tooltip.addAll(this.getOutputNames(stack));
		tooltip.add(Component.literal(""));
		tooltip.add(Component.literal("After consuming those items from inventory"));
		tooltip.addAll(this.getInputNames(stack));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		if (!world.isClientSide) {
			ItemStack stack = living.getItemInHand(hand);
			IItemHandler handler = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
			for (TF2Ingredient input : getInput(stack, living)) {
				if (!TF2Util.hasEnoughItem(handler, input, input.getCount()))
					return new InteractionResultHolder<>(InteractionResult.FAIL, living.getItemInHand(hand));
			}
			for (TF2Ingredient input : getInput(stack, living)) {
				TF2Util.removeItemsMatching(handler, input.getCount(), input);
			}
			for (ItemStack out : getOutput(stack, living))
				ItemHandlerHelper.giveItemToPlayer(living, out);

			stack.shrink(1);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getItemInHand(hand));
	}
}
