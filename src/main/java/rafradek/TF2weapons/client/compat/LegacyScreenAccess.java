package rafradek.TF2weapons.client.compat;

/** Implemented by legacy screens so legacy buttons can call back into them. */
public interface LegacyScreenAccess {
	void legacyActionPerformed(GuiButton button);
}
