package rafradek.TF2weapons.client.compat;

/** 1.12 Tessellator replacement drawing immediately through 1.19's RenderSystem. */
public class Tessellator {
	private static final Tessellator INSTANCE = new Tessellator();
	private final BufferBuilder buffer = new BufferBuilder();

	public static Tessellator getInstance() {
		return INSTANCE;
	}

	public BufferBuilder getBuffer() {
		return buffer;
	}

	public void draw() {
		buffer.draw();
	}
}
