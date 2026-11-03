package rafradek.TF2weapons.client.compat;

import java.util.ArrayList;

/** buttonList of legacy screens: adding or removing also adds/removes the 1.19 widget. */
class LegacyButtonList extends ArrayList<GuiButton> {
	private final Object owner;

	LegacyButtonList(Object owner) {
		this.owner = owner;
	}

	@Override
	public boolean add(GuiButton button) {
		if (owner instanceof GuiScreen)
			((GuiScreen) owner).addLegacyButton(button);
		else if (owner instanceof GuiContainer)
			((GuiContainer<?>) owner).addLegacyButton(button);
		return super.add(button);
	}

	@Override
	public boolean remove(Object button) {
		if (button instanceof GuiButton) {
			if (owner instanceof GuiScreen)
				((GuiScreen) owner).removeLegacyButton((GuiButton) button);
			else if (owner instanceof GuiContainer)
				((GuiContainer<?>) owner).removeLegacyButton((GuiButton) button);
		}
		return super.remove(button);
	}

	@Override
	public void clear() {
		for (GuiButton button : new ArrayList<>(this))
			remove(button);
		super.clear();
	}
}
