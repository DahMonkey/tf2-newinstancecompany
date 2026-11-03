package rafradek.TF2weapons.client.compat;

/** 1.12 vertex format names, as understood by the legacy {@link BufferBuilder}. */
public enum DefaultVertexFormats {
	POSITION(false, false), POSITION_COLOR(false, true), POSITION_TEX(true, false), POSITION_NORMAL(false, false),
	POSITION_TEX_NORMAL(true, false), POSITION_TEX_COLOR(true, true), POSITION_TEX_COLOR_NORMAL(true, true),
	POSITION_TEX_LMAP_COLOR(true, true), POSITION_TEX_COLOR_LMAP(true, true), PARTICLE_POSITION_TEX_COLOR_LMAP(true, true),
	BLOCK(true, true), ITEM(true, true), OLDMODEL_POSITION_TEX_NORMAL(true, false);

	public final boolean tex;
	public final boolean color;

	DefaultVertexFormats(boolean tex, boolean color) {
		this.tex = tex;
		this.color = color;
	}
}
