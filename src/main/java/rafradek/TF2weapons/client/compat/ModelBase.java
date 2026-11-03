package rafradek.TF2weapons.client.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Re-implementation of Minecraft 1.12's ModelBase for the mod's legacy box models. */
public abstract class ModelBase {
	public float swingProgress;
	public boolean isRiding;
	public boolean isChild = true;
	public List<ModelRenderer> boxList = new ArrayList<>();
	public int textureWidth = 64;
	public int textureHeight = 32;

	/** Draws the model. Arguments match 1.12: limb swing, swing amount, age, head yaw, head pitch, scale. */
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scaleFactor, Entity entity) {
	}

	public void setLivingAnimations(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
	}

	public ModelRenderer getRandomModelBox(Random rand) {
		return this.boxList.get(rand.nextInt(this.boxList.size()));
	}

	public void setModelAttributes(ModelBase model) {
		this.swingProgress = model.swingProgress;
		this.isRiding = model.isRiding;
		this.isChild = model.isChild;
	}

	public static void copyModelAngles(ModelRenderer source, ModelRenderer dest) {
		dest.rotateAngleX = source.rotateAngleX;
		dest.rotateAngleY = source.rotateAngleY;
		dest.rotateAngleZ = source.rotateAngleZ;
		dest.rotationPointX = source.rotationPointX;
		dest.rotationPointY = source.rotationPointY;
		dest.rotationPointZ = source.rotationPointZ;
	}
}
