package rafradek.TF2weapons.item;

import rafradek.TF2weapons.util.TF2Util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemCrate extends ItemFromData {

	public static class PropertyContent extends PropertyType<CrateContent> {

		public PropertyContent(int id, String name, Class<CrateContent> type) {
			super(id, name, type);
		}

		@Override
		public CrateContent deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			CrateContent content = new CrateContent();
			HashMap<String, Integer> map = new HashMap<>();
			for (Entry<String, JsonElement> attribute : json.getAsJsonObject().entrySet()) {
				String itemName = attribute.getKey();
				int chance = attribute.getValue().getAsInt();
				content.content.put(itemName, chance);
				content.maxCrateValue += chance;
			}
			return content;
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, CrateContent value) throws IOException {
			buf.writeByte(value.content.size());
			for (Entry<String, Integer> entry : value.content.entrySet()) {
				buf.writeUTF(entry.getKey());
				buf.writeShort(entry.getValue());
			}
		}

		@Override
		public CrateContent deserialize(DataInput buf, WeaponData data) throws IOException {
			int attributeCount = buf.readByte();
			CrateContent content = new CrateContent();
			for (int i = 0; i < attributeCount; i++) {
				String entry = buf.readUTF();
				int value = buf.readShort();
				content.content.put(entry, value);
				content.maxCrateValue += value;
			}
			return content;
		}
	}

	public static class CrateContent {
		public HashMap<String, Integer> content = new HashMap<>();
		public int maxCrateValue;
	}

	public ItemCrate() {
		this.setMaxStackSize(64);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getItemInHand(hand);
		if (!world.isClientSide && !itemStackIn.getTag().getBoolean("Open")) {
			if (player.getInventory().contains(TF2Util.metaStack(TF2weapons.itemTF2, 1, 7))) {
				itemStackIn.getTag().putBoolean("Open", true);
				player.getInventory().clearMatchingItems(TF2weapons.itemTF2, 7, 1, null);
			}
		}
		if (!world.isClientSide && itemStackIn.getTag().getBoolean("Open")) {
			// ArrayList<String> list = new ArrayList<String>();

			ItemStack stack = ItemStack.EMPTY;
			if (player.getRandom().nextInt(32) == 0) {
				stack = ItemFromData.getRandomWeaponOfType("cosmetic", player.getRandom(), false);
				((ItemWearable) stack.getItem()).applyRandomEffect(stack, player.getRandom());
			} else {
				int choosen = player.getRandom().nextInt(getData(itemStackIn).get(PropertyType.CONTENT).maxCrateValue);
				int currVal = 0;
				for (Entry<String, Integer> entry : getData(itemStackIn).get(PropertyType.CONTENT).content.entrySet()) {
					currVal += entry.getValue();
					if (choosen < currVal) {
						stack = ItemFromData.getNewStack(entry.getKey());
						break;
					}
					/*
					 * for (int i = 0; i < entry.getValue(); i++) list.add(entry.getKey());
					 */
				}
			}
			//
			if (!(stack.getItem() instanceof ItemWearable))
				stack.getTag().putBoolean("Strange", true);

			if (!player.getInventory().add(stack))
				player.dropItem(stack, true);
			// player.addStat(TF2Achievements.LOOT_CRATE);
			player.addStat(TF2weapons.cratesOpened);
			/*
			 * if(!world.isRemote &&
			 * ((EntityPlayerMP)player).getStatFile().readStat(TF2weapons.cratesOpened)>=9
			 * ){ player.addStat(TF2Achievements.CRATES_10); }
			 */
			itemStackIn.shrink(1);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
		} else
			return new InteractionResultHolder<>(InteractionResult.FAIL, itemStackIn);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		/*
		 * if (!par1ItemStack.hasTagCompound()) { par1ItemStack.getTagCompound()=new
		 * NBTTagCompound(); par1ItemStack.getTagCompound().setTag("Attributes",
		 * (NBTTagCompound)
		 * ((ItemUsable)par1ItemStack.getItem()).buildInAttributes.copy()); }
		 */
		if (stack.hasTag()) {
			super.appendHoverText(stack, world, tooltip, advanced);

			if (stack.getTag().getBoolean("Open")) {
				tooltip.add("The crate is opened now");
				tooltip.add("Right click to get the item");
			}

			tooltip.add("Possible content:");
			for (String name : getData(stack).get(PropertyType.CONTENT).content.keySet()) {
				WeaponData data = MapList.nameToData.get(name);
				if (data != null)
					tooltip.add(I18n.get("weapon." + data.getName()));
			}
		}
	}

	@Override
	public int getItemBurnTime(ItemStack itemStack) {
		return 2400;
	}
}
