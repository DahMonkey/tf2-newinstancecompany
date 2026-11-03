package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

/**
 * ModelFlare - Undefined Created using Tabula 5.1.0
 */
public class ModelFlare extends EntityModel {
	public ModelPart shape1;

	public ModelFlare() {
		textureWidth = 16;
		textureHeight = 16;
		shape1 = new ModelPart(this, 0, 0);
		shape1.setRotationPoint(-2.5F, -1.0F, -1.0F);
		shape1.addBox(0.0F, 0.0F, 0.0F, 5, 2, 2, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		shape1.render(f5);
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
