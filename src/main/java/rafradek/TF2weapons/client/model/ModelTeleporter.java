package rafradek.TF2weapons.client.model;

import rafradek.TF2weapons.entity.building.EntityTeleporter;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

/**
 * ModelTeleporter - rafradek Created using Tabula 5.1.0
 */
public class ModelTeleporter extends EntityModel {
	public ModelPart rotating;
	public ModelPart base1;
	public ModelPart base2;
	public ModelPart base3;
	public ModelPart rotating2;
	public ModelPart rotating3;

	public ModelTeleporter() {
		textureWidth = 64;
		textureHeight = 32;
		rotating3 = new ModelPart(this, 0, 0);
		rotating3.setRotationPoint(0.0F, 0.0F, 0.0F);
		rotating3.addBox(4.0F, -0.7F, -1F, 2, 1, 2, 0.0F);
		base1 = new ModelPart(this, 0, 15);
		base1.setRotationPoint(-3.5F, 22.4F, -2.5F);
		base1.addBox(0.0F, 0.0F, 0.0F, 7, 1, 5, 0.0F);
		base3 = new ModelPart(this, 0, 18);
		base3.setRotationPoint(2.1F, 23.0F, -4F);
		base3.addBox(0.0F, 0.0F, 0.0F, 1, 1, 8, 0.0F);
		rotating = new ModelPart(this, 0, 10);
		rotating.setRotationPoint(0.0F, 21.4F, 0.0F);
		rotating.addBox(-7.0F, 0.0F, -2F, 14, 1, 4, 0.0F);
		base2 = new ModelPart(this, 0, 18);
		base2.setRotationPoint(-3.1F, 23.0F, -4F);
		base2.addBox(0.0F, 0.0F, 0.0F, 1, 1, 8, 0.0F);
		rotating2 = new ModelPart(this, 0, 0);
		rotating2.setRotationPoint(0.0F, 0.0F, 0.0F);
		rotating2.addBox(-6.0F, -0.7F, -1.0F, 2, 1, 2, 0.0F);
		rotating.addChild(rotating3);
		rotating.addChild(rotating2);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		base1.render(f5);
		base3.render(f5);
		rotating.render(f5);
		base2.render(f5);
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_,
			float p_78087_5_, float p_78087_6_, Entity entityIn) {
		rotating.rotateAngleY = ((EntityTeleporter) entityIn).spinRender;
	}
}
