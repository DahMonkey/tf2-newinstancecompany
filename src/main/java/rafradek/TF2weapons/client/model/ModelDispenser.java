package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

/**
 * ModelDispenser - Undefined Created using Tabula 5.1.0
 */
public class ModelDispenser extends EntityModel {
	public ModelPart shapemed2;
	public ModelPart shape1;
	public ModelPart shape2;
	public ModelPart shape3;
	public ModelPart shape4;
	public ModelPart shape5;
	public ModelPart shape1_1;
	public ModelPart shape4_1;
	public ModelPart shape3_1;
	public ModelPart shape2_1;
	public ModelPart shape5_1;
	public ModelPart shapemain;
	public ModelPart shapemed;
	public ModelPart shapemed_1;
	public ModelPart shapeammo;

	public ModelDispenser() {
		textureWidth = 64;
		textureHeight = 32;
		shapemed = new ModelPart(this, 32, 18);
		shapemed.setRotationPoint(-6.5F, 19.3F, 0.0F);
		shapemed.addBox(-1.0F, -5.0F, -1.5F, 3, 5, 3, -0.2F);
		shape4_1 = new ModelPart(this, 52, 0);
		shape4_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape4_1.addBox(3.5F, 1.8F, 1.5F, 1, 5, 1, -0.1F);
		shape2_1 = new ModelPart(this, 32, 3);
		shape2_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape2_1.addBox(-4.5F, 7.0F, 1.7F, 9, 9, 1, -0.1F);
		shapemed_1 = new ModelPart(this, 0, 20);
		shapemed_1.setRotationPoint(-6.5F, 16.0F, 1.0F);
		shapemed_1.addBox(-1.0F, -5.7F, -1.5F, 3, 3, 1, 0.1F);
		shape5 = new ModelPart(this, 32, 13);
		shape5.setRotationPoint(0.0F, 21.9F, -3.0F);
		shape5.addBox(-4.5F, -2.0F, -0.5F, 9, 4, 1, 0.0F);
		setRotateAngle(shape5, 0.47123889803846897F, 0.0F, 0.0F);
		shape2 = new ModelPart(this, 32, 3);
		shape2.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape2.addBox(-4.5F, 7.0F, -2.7F, 9, 9, 1, -0.1F);
		shapemain = new ModelPart(this, 0, 0);
		shapemain.setRotationPoint(0.0F, 16.0F, 0.0F);
		shapemain.addBox(-4.5F, -8.0F, -2.0F, 9, 16, 4, 0.0F);
		shapeammo = new ModelPart(this, 56, 0);
		shapeammo.setRotationPoint(4.2F, 10.0F, -1.5F);
		shapeammo.addBox(0.0F, 0.0F, 0.0F, 1, 9, 3, 0.0F);
		shape1_1 = new ModelPart(this, 32, 0);
		shape1_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape1_1.addBox(-4.5F, 0.0F, -3.0F, 9, 2, 1, 0.0F);
		shapemed2 = new ModelPart(this, 44, 18);
		shapemed2.setRotationPoint(-6.5F, 13.0F, -0.5F);
		shapemed2.addBox(0.0F, 0.0F, 0.0F, 1, 9, 1, 0.0F);
		shape1 = new ModelPart(this, 32, 0);
		shape1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape1.addBox(-4.5F, 0.0F, 2.0F, 9, 2, 1, 0.0F);
		shape3 = new ModelPart(this, 52, 0);
		shape3.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape3.addBox(-4.5F, 1.9F, -2.5F, 1, 5, 1, -0.1F);
		shape3_1 = new ModelPart(this, 52, 0);
		shape3_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape3_1.addBox(-4.5F, 1.9F, 1.5F, 1, 5, 1, -0.1F);
		shape5_1 = new ModelPart(this, 32, 13);
		shape5_1.setRotationPoint(0.0F, 21.9F, 3.0F);
		shape5_1.addBox(-4.5F, -2.0F, -0.5F, 9, 4, 1, 0.0F);
		setRotateAngle(shape5_1, -0.47123889803846897F, 0.0F, 0.0F);
		shape4 = new ModelPart(this, 52, 0);
		shape4.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape4.addBox(3.5F, 1.9F, -2.5F, 1, 5, 1, -0.1F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		shapemed.render(f5);
		shape4_1.render(f5);
		shape2_1.render(f5);
		shapemed_1.render(f5);
		shape5.render(f5);
		shape2.render(f5);
		shapemain.render(f5);
		shapeammo.render(f5);
		shape1_1.render(f5);
		GlStateManager.pushMatrix();
		GlStateManager.translate(shapemed2.offsetX, shapemed2.offsetY, shapemed2.offsetZ);
		GlStateManager.translate(shapemed2.rotationPointX * f5, shapemed2.rotationPointY * f5,
				shapemed2.rotationPointZ * f5);
		GlStateManager.scale(1.0D, 1.0D, 0.9D);
		GlStateManager.translate(-shapemed2.offsetX, -shapemed2.offsetY, -shapemed2.offsetZ);
		GlStateManager.translate(-shapemed2.rotationPointX * f5, -shapemed2.rotationPointY * f5,
				-shapemed2.rotationPointZ * f5);
		shapemed2.render(f5);
		GlStateManager.popMatrix();
		shape1.render(f5);
		shape3.render(f5);
		shape3_1.render(f5);
		shape5_1.render(f5);
		shape4.render(f5);
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
