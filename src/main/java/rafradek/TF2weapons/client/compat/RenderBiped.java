package rafradek.TF2weapons.client.compat;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;

/** 1.12 RenderBiped: legacy biped model with held items. */
public class RenderBiped<T extends Mob> extends RenderLiving<T> {
	public RenderBiped(EntityRendererProvider.Context context, ModelBiped model, float shadowSize) {
		super(context, model, shadowSize);
		this.addLayer(new LayerHeldItem<>(this));
	}

	@Override
	protected net.minecraft.resources.ResourceLocation getEntityTexture(T entity) {
		return null;
	}
}
