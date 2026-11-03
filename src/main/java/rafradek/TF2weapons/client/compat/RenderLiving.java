package rafradek.TF2weapons.client.compat;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;

/** 1.12 RenderLiving. */
public abstract class RenderLiving<T extends Mob> extends RenderLivingBase<T> {
	protected RenderLiving(EntityRendererProvider.Context context, ModelBase model, float shadowSize) {
		super(context, model, shadowSize);
	}
}
