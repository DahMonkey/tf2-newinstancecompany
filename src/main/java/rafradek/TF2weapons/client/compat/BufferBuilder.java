package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.GameRenderer;

/**
 * 1.12-style immediate-mode vertex builder. Vertices are transformed by the current legacy pose and
 * drawn with a position+colour or position+texture+colour shader depending on what was supplied.
 * Normals and lightmap coordinates are accepted and ignored, as 1.19's basic shaders do not use them.
 */
public class BufferBuilder {
	private final com.mojang.blaze3d.vertex.BufferBuilder real = new com.mojang.blaze3d.vertex.BufferBuilder(2097152);
	private DefaultVertexFormats format;
	private boolean textured;
	private boolean drawing;
	private double x, y, z;
	private float u, v;
	private float r = 1, g = 1, b = 1, a = 1;
	private boolean colorSet;
	private double offX, offY, offZ;

	public void begin(int glMode, DefaultVertexFormats format) {
		this.format = format;
		this.textured = format.tex;
		VertexFormat.Mode mode = switch (glMode) {
		case 4 -> VertexFormat.Mode.TRIANGLES;
		case 5 -> VertexFormat.Mode.TRIANGLE_STRIP;
		case 6 -> VertexFormat.Mode.TRIANGLE_FAN;
		case 1 -> VertexFormat.Mode.DEBUG_LINES;
		case 3 -> VertexFormat.Mode.DEBUG_LINE_STRIP;
		default -> VertexFormat.Mode.QUADS;
		};
		real.begin(mode, textured ? DefaultVertexFormat.POSITION_TEX_COLOR : DefaultVertexFormat.POSITION_COLOR);
		drawing = true;
	}

	public BufferBuilder pos(double x, double y, double z) {
		this.x = x + offX;
		this.y = y + offY;
		this.z = z + offZ;
		return this;
	}

	public BufferBuilder tex(double u, double v) {
		this.u = (float) u;
		this.v = (float) v;
		return this;
	}

	public BufferBuilder color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		colorSet = true;
		return this;
	}

	public BufferBuilder color(int r, int g, int b, int a) {
		return color(r / 255F, g / 255F, b / 255F, a / 255F);
	}

	public BufferBuilder normal(float x, float y, float z) {
		return this;
	}

	public BufferBuilder lightmap(int sky, int block) {
		return this;
	}

	public void setTranslation(double x, double y, double z) {
		offX = x;
		offY = y;
		offZ = z;
	}

	public void endVertex() {
		Matrix4f mat = LegacyRender.pose().last().pose();
		Vector4f p = new Vector4f((float) x, (float) y, (float) z, 1);
		p.transform(mat);
		float cr = colorSet ? r : 1, cg = colorSet ? g : 1, cb = colorSet ? b : 1, ca = colorSet ? a : 1;
		real.vertex(p.x(), p.y(), p.z());
		if (textured)
			real.uv(u, v);
		real.color(cr, cg, cb, ca);
		real.endVertex();
		if (!format.color)
			colorSet = false;
	}

	void draw() {
		if (!drawing)
			return;
		drawing = false;
		colorSet = false;
		if (textured)
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		else
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferUploader.drawWithShader(real.end());
	}
}
