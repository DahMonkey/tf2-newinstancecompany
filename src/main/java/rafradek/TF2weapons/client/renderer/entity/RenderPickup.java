package rafradek.TF2weapons.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import rafradek.TF2weapons.entity.EntityPickup;

/** Health/ammo pickups: a bobbing, spinning item like a dropped item. */
public class RenderPickup extends EntityRenderer<EntityPickup> {

	private final ItemRenderer itemRenderer;

	public RenderPickup(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	@Override
	public ResourceLocation getTextureLocation(EntityPickup entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	@Override
	public void render(EntityPickup entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers,
			int light) {
		if (entity.isDisabled())
			return;
		ItemStack model = entity.getPickupType().model;
		if (model.isEmpty())
			return;
		pose.pushPose();
		BakedModel bakedmodel = this.itemRenderer.getModel(model, entity.level, null, entity.getId());
		float bob = Mth.sin((entity.age + partialTick) / 10.0F + entity.hoverStart) * 0.1F + 0.1F;
		float scaleY = bakedmodel.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
		pose.translate(0.0D, bob + 0.25F * scaleY, 0.0D);
		float spin = (entity.age + partialTick) / 20.0F + entity.hoverStart;
		pose.mulPose(Vector3f.YP.rotation(spin));
		this.itemRenderer.render(model, ItemTransforms.TransformType.GROUND, false, pose, buffers, light,
				OverlayTexture.NO_OVERLAY, bakedmodel);
		pose.popPose();
		super.render(entity, yaw, partialTick, pose, buffers, light);
	}
}
