package rafradek.TF2weapons.inventory;

import net.minecraft.world.Container;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemCleaver;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerUpgrades extends LegacyContainer {
	/** The crafting matrix inventory (3x3). */
	// public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
	public Container upgradedItem = new SimpleContainer(1);
	private final Level world;
	/** Position of the workbench */
	public final BlockPos pos;
	public Player player;
	public int currentRecipe = -1;
	public TileEntityUpgrades station;
	public ArrayList<TF2Attribute> applicable;// = new boolean[TileEntityUpgrades.UPGRADES_COUNT];
	public int playerCredits;
	public int[] transactions;// = new int[TileEntityUpgrades.UPGRADES_COUNT];
	public int[] transactionsCost;// = new int[TileEntityUpgrades.UPGRADES_COUNT];
	// public ArrayList<Integer> guiShowId = new ArrayList<>();

	public ContainerUpgrades(Player player, Inventory inventory, TileEntityUpgrades station,
			Level world, BlockPos posIn) {
		this.station = station;
		this.player = player;
		this.world = world;
		this.pos = posIn;
		this.addDataSlot(this.creditsSlot);
		this.applicable = new ArrayList<>();
		transactions = new int[0];
		transactionsCost = new int[0];
		this.addSlot(new Slot(upgradedItem, 0, 108, 8) {
			@Override
			public void setChanged() {
				super.setChanged();
				refreshData();
				transactions = new int[applicable.size()];
				transactionsCost = new int[applicable.size()];

			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				if (stack.isEmpty())
					return false;
				else
					return stack.getItem() instanceof ItemFromData
							&& (stack.getMaxStackSize() == 1 || stack.getItem() instanceof ItemCleaver);
			}
		});

		/*
		 * for (int k = 0; k < 3; ++k) { for (int i1 = 0; i1 < 9; ++i1) {
		 * this.addSlotToContainer(new Slot(cabinet, i1 + k * 9 + 9, 8 + i1 * 18, 91 + k
		 * * 18)); } }
		 */

		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlot(new Slot(inventory, i1 + k * 9 + 9, 36 + i1 * 18, 143 + k * 18));

		for (int l = 0; l < 9; ++l)
			this.addSlot(new Slot(inventory, l, 36 + l * 18, 201));
	}

	public void refreshData() {
		this.applicable.clear();
		for (int i = 0; i < this.station.attributeList.size(); i++) {

			if (!this.upgradedItem.getItem(0).isEmpty() && station.attributeList.get(i) != null
					&& station.attributeList.get(i).canApply(this.upgradedItem.getItem(0)))
				this.applicable.add(station.attributeList.get(i));
		}
	}

	private final net.minecraft.world.inventory.DataSlot creditsSlot = new net.minecraft.world.inventory.DataSlot() {
		@Override
		public int get() {
			return ContainerUpgrades.this.player.level.isClientSide ? ContainerUpgrades.this.playerCredits
					: TF2Util.getExperiencePoints(ContainerUpgrades.this.player);
		}

		@Override
		public void set(int value) {
			ContainerUpgrades.this.playerCredits = value;
		}
	};

	@Override
	public void onContainerClosed(Player player) {
		super.onContainerClosed(player);

		if (!this.world.isClientSide) {
			ItemStack itemstack = this.upgradedItem.removeItemNoUpdate(0);

			if (!itemstack.isEmpty())
				player.drop(itemstack, false);
		}
	}

	@Override
	public boolean canInteractWith(Player player) {
		return this.world.getBlockState(this.pos).getBlock() != TF2weapons.blockUpgradeStation ? false
				: player.distanceToSqr(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
						this.pos.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	@Nullable
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();

			if (index == 0) {
				if (!this.moveItemStackTo(itemstack1, 1, 28, true))
					return ItemStack.EMPTY;

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (index >= 1 && index < 28) {
				if (!this.moveItemStackTo(itemstack1, 0, 1, false))
					return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 1, 28, false))
				return ItemStack.EMPTY;

			if (itemstack1.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();

			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean enchantItem(Player player, int id) {
		ItemStack stack = this.upgradedItem.getItem(0);

		CompoundTag stacktag = stack.getTag();

		int expPoints = TF2Util.getExperiencePoints(player);
		if (id >= 0) {
			int idEnch = Math.min(this.applicable.size() - 1, Math.max(id / 2, 0));
			boolean adding = id % 2 == 0;
			TF2Attribute attr = this.applicable.get(idEnch).getAttributeReplacement(stack);
			TF2Attribute attrorig = this.applicable.get(idEnch);
			if (attr == null || stack.isEmpty() || !attr.canApply(stack))
				return false;

			int cost = attr.getUpgradeCost(stack);

			int currLevel = attr.calculateCurrLevel(stack);

			int austrUpgrade = stacktag.contains(NBTLiterals.AUSTR_UPGRADED)
					? stacktag.getShort(NBTLiterals.AUSTR_UPGRADED)
					: -1;
			boolean austrAdd = currLevel == attr.numLevels && attr.austrUpgrade != 0f
					&& stacktag.getBoolean("Australium") && austrUpgrade != attr.id;

			if (adding && currLevel < this.station.attributes.get(attrorig) && expPoints >= cost
					&& cost + stacktag.getInt("TotalSpent") <= TF2Attribute.getMaxExperience(stack, player)) {
				CompoundTag tag = stacktag.getCompound("Attributes");
				String key = String.valueOf(attr.id);

				if (!stacktag.contains("AttributesOrig")) {
					stacktag.put("AttributesOrig", stacktag.getCompound("Attributes").copy());
				}

				if (!tag.contains(key))
					tag.putFloat(key, attr.defaultValue);
				tag.putFloat(key, tag.getFloat(key) + attr.getPerLevel(stack));

				stacktag.putInt("TotalCost", stacktag.getInt("TotalCost") + attr.cost);
				stacktag.putInt("TotalSpent", stacktag.getInt("TotalSpent") + cost);

				if (currLevel == attr.numLevels - 1 && attr.numLevels > 1)
					stacktag.putInt("LastUpgradesCost", (int) (stacktag.getInt("LastUpgradesCost")
							+ attr.cost * (attr.numLevels > 2 ? 3 : 1.5f)));

				// stacktag.setInteger("TotalCostReal", stacktag.getInteger("TotalCostReal") +
				// attr.cost);
				TF2Util.setExperiencePoints(player, expPoints - cost);
				this.transactions[idEnch]++;
				this.transactionsCost[idEnch] += cost;

				/*
				 * player.awardStat(TF2Achievements.WEAPON_UPGRADE); if (attr.numLevels > 1 &&
				 * attr.calculateCurrLevel(stack) == attr.numLevels)
				 * player.awardStat(TF2Achievements.FULLY_UPGRADED);
				 */
			} else if (adding && austrAdd) {
				CompoundTag tag = stacktag.getCompound("Attributes");
				String key = String.valueOf(attr.id);

				if (!stacktag.contains("AttributesOrig")) {
					stacktag.put("AttributesOrig", stacktag.getCompound("Attributes").copy());
				}

				tag.putFloat(key, tag.getFloat(key) + attr.getPerLevel(stack) * attr.austrUpgrade);

				// this.transactions[idEnch]++;

				if (austrUpgrade != -1)
					tag.putFloat(String.valueOf(austrUpgrade),
							tag.getFloat(String.valueOf(austrUpgrade))
									- TF2Attribute.attributes[austrUpgrade].getPerLevel(stack)
											* TF2Attribute.attributes[austrUpgrade].austrUpgrade);
				stacktag.putShort(NBTLiterals.AUSTR_UPGRADED, (short) attr.id);
			} else if (!adding && this.transactions[idEnch] > 0) {
				cost = this.transactionsCost[idEnch];
				int count = this.transactions[idEnch];
				CompoundTag tag = stacktag.getCompound("Attributes");
				String key = String.valueOf(attr.id);

				if (!tag.contains(key))
					return false;

				if (attr.id == austrUpgrade) {
					tag.putFloat(key, tag.getFloat(key) - attr.getPerLevel(stack) * attr.austrUpgrade);
					stacktag.remove(NBTLiterals.AUSTR_UPGRADED);
				}

				tag.putFloat(key, tag.getFloat(key) - attr.getPerLevel(stack) * count);
				if (tag.getFloat(key) == attr.defaultValue)
					tag.remove(key);

				stacktag.putInt("TotalCost", stacktag.getInt("TotalCost") - attr.cost * count);
				stacktag.putInt("TotalSpent", stacktag.getInt("TotalSpent") - cost);

				if (currLevel >= attr.numLevels && attr.numLevels > 1)
					stacktag.putInt("LastUpgradesCost", (int) (stacktag.getInt("LastUpgradesCost")
							- attr.cost * (attr.numLevels > 2 ? 3 : 1.5f)));
				// stacktag.setInteger("TotalCostReal", stacktag.getInteger("TotalCostReal") -
				// this.transactionsCost[idEnch]);
				TF2Util.setExperiencePoints(player, expPoints + cost);
				this.transactions[idEnch] = 0;
				this.transactionsCost[idEnch] = 0;

			}
		} else if (id == -1) {
			if (stacktag.contains("AttributesOrig") && stacktag.getInt("TotalSpent") > 0) {
				stacktag.put("Attributes", stacktag.get("AttributesOrig").copy());
				stacktag.putInt("LastUpgradesCost", 0);
				TF2Util.setExperiencePoints(player, expPoints + stacktag.getInt("TotalSpent"));
				stacktag.putInt("TotalSpent", 0);
				stacktag.putInt("TotalCost", 0);
				for (int i = 0; i < this.transactions.length; i++) {
					this.transactions[i] = 0;
					this.transactionsCost[i] = 0;
				}
				stacktag.remove(NBTLiterals.AUSTR_UPGRADED);
			}
		}
		return true;
	}
}
