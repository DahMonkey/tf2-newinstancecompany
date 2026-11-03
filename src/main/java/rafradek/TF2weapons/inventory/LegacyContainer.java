package rafradek.TF2weapons.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Base for the mod's containers. 1.19 menus need their MenuType and window id in the constructor;
 * {@link TF2GuiHandler} provides them through {@link #withContext} so the 1.12 constructors stay as
 * they were.
 */
public abstract class LegacyContainer extends AbstractContainerMenu {
	private static final ThreadLocal<MenuType<?>> NEXT_TYPE = new ThreadLocal<>();
	private static final ThreadLocal<Integer> NEXT_ID = ThreadLocal.withInitial(() -> 0);

	protected LegacyContainer() {
		super(NEXT_TYPE.get(), NEXT_ID.get());
	}

	public static int currentWindowId() {
		return NEXT_ID.get();
	}

	public static <T> T withContext(MenuType<?> type, int windowId, java.util.function.Supplier<T> factory) {
		MenuType<?> oldType = NEXT_TYPE.get();
		int oldId = NEXT_ID.get();
		NEXT_TYPE.set(type);
		NEXT_ID.set(windowId);
		try {
			return factory.get();
		} finally {
			NEXT_TYPE.set(oldType);
			NEXT_ID.set(oldId);
		}
	}

	/** 1.12 name for quickMoveStack. */
	public ItemStack transferStackInSlot(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return this.transferStackInSlot(player, index);
	}

	/** 1.12 name for stillValid. */
	public boolean canInteractWith(Player player) {
		return true;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.canInteractWith(player);
	}

	public void detectAndSendChanges() {
		this.broadcastChanges();
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
	}

	public void onContainerClosed(Player player) {
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.onContainerClosed(player);
	}

	public void onCraftMatrixChanged(net.minecraft.world.Container inventory) {
	}

	@Override
	public void slotsChanged(net.minecraft.world.Container inventory) {
		super.slotsChanged(inventory);
		this.onCraftMatrixChanged(inventory);
	}

	public boolean enchantItem(Player player, int id) {
		return false;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		return this.enchantItem(player, id);
	}
}
