package rafradek.TF2weapons.client.compat;

import net.minecraft.world.entity.LivingEntity;

/** 1.12 LayerRenderer: an extra pass drawn after a living entity's main model. */
public interface LayerRenderer<T extends LivingEntity> {
	void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch, float scale);

	default boolean shouldCombineTextures() {
		return false;
	}
}
