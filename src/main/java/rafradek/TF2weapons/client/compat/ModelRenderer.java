package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Re-implementation of Minecraft 1.12's ModelRenderer (box model part) so the mod's hand-made
 * models (sentries, dispensers, projectiles...) keep their original code. Geometry and UV layout
 * match 1.12 exactly; drawing goes through {@link LegacyRender}.
 */
public class ModelRenderer {
	public float textureWidth;
	public float textureHeight;
	private int textureOffsetX;
	private int textureOffsetY;
	public float rotationPointX;
	public float rotationPointY;
	public float rotationPointZ;
	public float rotateAngleX;
	public float rotateAngleY;
	public float rotateAngleZ;
	public boolean mirror;
	public boolean showModel = true;
	public boolean isHidden;
	public List<Box> cubeList = new ArrayList<>();
	public List<ModelRenderer> childModels;
	public final String boxName;
	private final ModelBase baseModel;
	public float offsetX;
	public float offsetY;
	public float offsetZ;

	public ModelRenderer(ModelBase model, String boxNameIn) {
		this.textureWidth = 64.0F;
		this.textureHeight = 32.0F;
		this.baseModel = model;
		if (model != null) {
			model.boxList.add(this);
			this.setTextureSize(model.textureWidth, model.textureHeight);
		}
		this.boxName = boxNameIn;
	}

	public ModelRenderer(ModelBase model) {
		this(model, null);
	}

	public ModelRenderer(ModelBase model, int texOffX, int texOffY) {
		this(model);
		this.setTextureOffset(texOffX, texOffY);
	}

	public void addChild(ModelRenderer renderer) {
		if (this.childModels == null)
			this.childModels = new ArrayList<>();
		this.childModels.add(renderer);
	}

	public ModelRenderer setTextureOffset(int x, int y) {
		this.textureOffsetX = x;
		this.textureOffsetY = y;
		return this;
	}

	public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {
		return addBox(offX, offY, offZ, width, height, depth, 0.0F);
	}

	public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {
		return addBox(offX, offY, offZ, width, height, depth, 0.0F);
	}

	public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
		boolean old = this.mirror;
		this.mirror = mirrored;
		addBox(offX, offY, offZ, width, height, depth, 0.0F);
		this.mirror = old;
		return this;
	}

	public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {
		this.cubeList.add(new Box(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height,
				depth, scaleFactor, this.mirror));
		return this;
	}

	public void setRotationPoint(float x, float y, float z) {
		this.rotationPointX = x;
		this.rotationPointY = y;
		this.rotationPointZ = z;
	}

	public ModelRenderer setTextureSize(float w, float h) {
		this.textureWidth = w;
		this.textureHeight = h;
		return this;
	}

	/** Applies this part's transform to the current matrix (1.12 postRender). */
	public void postRender(float scale) {
		if (this.isHidden || !this.showModel)
			return;
		PoseStack pose = LegacyRender.pose();
		pose.translate(this.offsetX, this.offsetY, this.offsetZ);
		if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
			if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F)
				pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
		} else {
			pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
			if (this.rotateAngleZ != 0.0F)
				pose.mulPose(Vector3f.ZP.rotation(this.rotateAngleZ));
			if (this.rotateAngleY != 0.0F)
				pose.mulPose(Vector3f.YP.rotation(this.rotateAngleY));
			if (this.rotateAngleX != 0.0F)
				pose.mulPose(Vector3f.XP.rotation(this.rotateAngleX));
		}
	}

	public void render(float scale) {
		if (this.isHidden || !this.showModel)
			return;
		PoseStack pose = LegacyRender.pose();
		pose.pushPose();
		postRender(scale);
		drawCubes(scale);
		if (this.childModels != null)
			for (ModelRenderer child : this.childModels)
				child.render(scale);
		pose.popPose();
	}

	public void renderWithRotation(float scale) {
		render(scale);
	}

	private void drawCubes(float scale) {
		if (this.cubeList.isEmpty())
			return;
		PoseStack.Pose entry = LegacyRender.pose().last();
		VertexConsumer buffer = LegacyRender.buffer();
		for (Box box : this.cubeList)
			box.render(entry, buffer, scale);
	}

	/** One textured cuboid, equivalent to 1.12's ModelBox. */
	public static class Box {
		private final float[][] positions = new float[8][];
		private final Quad[] quads;
		public final float posX1, posY1, posZ1, posX2, posY2, posZ2;

		public Box(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz,
				float delta, boolean mirror) {
			this.posX1 = x;
			this.posY1 = y;
			this.posZ1 = z;
			this.posX2 = x + dx;
			this.posY2 = y + dy;
			this.posZ2 = z + dz;
			float x2 = x + dx, y2 = y + dy, z2 = z + dz;
			x -= delta;
			y -= delta;
			z -= delta;
			x2 += delta;
			y2 += delta;
			z2 += delta;
			if (mirror) {
				float t = x2;
				x2 = x;
				x = t;
			}
			float[] v0 = { x, y, z }, v1 = { x2, y, z }, v2 = { x2, y2, z }, v3 = { x, y2, z };
			float[] v4 = { x, y, z2 }, v5 = { x2, y, z2 }, v6 = { x2, y2, z2 }, v7 = { x, y2, z2 };
			float tw = renderer.textureWidth, th = renderer.textureHeight;
			quads = new Quad[] {
					new Quad(new float[][] { v5, v1, v2, v6 }, texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, tw, th, mirror),
					new Quad(new float[][] { v0, v4, v7, v3 }, texU, texV + dz, texU + dz, texV + dz + dy, tw, th, mirror),
					new Quad(new float[][] { v5, v4, v0, v1 }, texU + dz, texV, texU + dz + dx, texV + dz, tw, th, mirror),
					new Quad(new float[][] { v2, v3, v7, v6 }, texU + dz + dx, texV + dz, texU + dz + dx + dx, texV, tw, th, mirror),
					new Quad(new float[][] { v1, v0, v3, v2 }, texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, tw, th, mirror),
					new Quad(new float[][] { v4, v5, v6, v7 }, texU + dz + dx + dz, texV + dz, texU + dz + dx + dz + dx, texV + dz + dy, tw, th, mirror) };
		}

		void render(PoseStack.Pose entry, VertexConsumer buffer, float scale) {
			Matrix4f mat = entry.pose();
			Matrix3f normalMat = entry.normal();
			float r = LegacyRender.red(), g = LegacyRender.green(), b = LegacyRender.blue(), a = LegacyRender.alpha();
			int light = LegacyRender.light(), overlay = LegacyRender.overlay();
			for (Quad quad : quads) {
				Vector3f normal = quad.normal.copy();
				normal.transform(normalMat);
				for (int i = 0; i < 4; i++) {
					float[] p = quad.verts[i];
					Vector4f pos = new Vector4f(p[0] * scale, p[1] * scale, p[2] * scale, 1.0F);
					pos.transform(mat);
					buffer.vertex(pos.x(), pos.y(), pos.z(), r, g, b, a, quad.u[i], quad.v[i], overlay, light,
							normal.x(), normal.y(), normal.z());
				}
			}
		}
	}

	private static class Quad {
		final float[][] verts;
		final float[] u = new float[4], v = new float[4];
		final Vector3f normal;

		Quad(float[][] verts, float u1, float v1, float u2, float v2, float tw, float th, boolean mirror) {
			this.verts = verts;
			u[0] = u2 / tw;
			v[0] = v1 / th;
			u[1] = u1 / tw;
			v[1] = v1 / th;
			u[2] = u1 / tw;
			v[2] = v2 / th;
			u[3] = u2 / tw;
			v[3] = v2 / th;
			if (mirror) {
				float[][] rev = new float[4][];
				float[] ru = new float[4], rv = new float[4];
				for (int i = 0; i < 4; i++) {
					rev[i] = verts[3 - i];
					ru[i] = u[3 - i];
					rv[i] = v[3 - i];
				}
				System.arraycopy(rev, 0, this.verts, 0, 4);
				System.arraycopy(ru, 0, u, 0, 4);
				System.arraycopy(rv, 0, v, 0, 4);
			}
			float[] a = this.verts[1], b = this.verts[0], c = this.verts[2];
			Vector3f e1 = new Vector3f(b[0] - a[0], b[1] - a[1], b[2] - a[2]);
			Vector3f e2 = new Vector3f(c[0] - a[0], c[1] - a[1], c[2] - a[2]);
			e2.cross(e1);
			e2.normalize();
			this.normal = e2;
		}
	}
}
