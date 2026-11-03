package rafradek.TF2weapons.client.renderer;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.inventory.InventoryWearables;
import rafradek.TF2weapons.item.ItemAmmoBelt;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWearable;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;

@OnlyIn(Dist.CLIENT)
public class LayerWearables implements RenderLayer<LivingEntity> {
	public final HumanoidModel modelBig;
	public final HumanoidModel modelMedium;
	public final HumanoidModel modelSmall;
	public LivingEntityRenderer<?> renderer;

	public LayerWearables(LivingEntityRenderer<?> render) {
		this.renderer = render;
		this.modelBig = new HumanoidModel(1.15F);
		this.modelMedium = new HumanoidModel(0.75F);
		this.modelSmall = new HumanoidModel(0.25F);
	}

	@Override
	public void doRenderLayer(LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		// System.out.println("Rendering layer");
		InventoryWearables inventory = entitylivingbaseIn.getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
		if (inventory != null) {
			for (int i = 0; i < 4; i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (!stack.isEmpty())
					renderModel(entitylivingbaseIn, stack, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
							netHeadYaw, headPitch, scale);
			}
		}
		if (entitylivingbaseIn instanceof EntityTF2Character) {
			ItemStackHandler loadout = ((EntityTF2Character) entitylivingbaseIn).loadout;
			for (int i = 0; i < loadout.getSlots(); i++) {
				ItemStack stack = loadout.getStackInSlot(i);
				if (!stack.isEmpty())
					renderModel(entitylivingbaseIn, stack, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
							netHeadYaw, headPitch, scale);
			}
		}
		for (ItemStack stack : entitylivingbaseIn.getArmorSlots())
			if (!stack.isEmpty())
				renderModel(entitylivingbaseIn, stack, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
						headPitch, scale);
	}

	public void renderModel(LivingEntity living, ItemStack stack, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (stack.getItem() instanceof ItemFromData) {

			Minecraft minecraft = Minecraft.getInstance();
			int visibility = ((ItemFromData) stack.getItem()).getVisibilityFlags(stack, living);
			if ((visibility & 1) == 1) {
				GlStateManager.pushMatrix();

				if (living.isShiftKeyDown())
					GlStateManager.translate(0.0F, 0.2F, 0.0F);
				getHead().postRender(0.0625F);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

				GlStateManager.translate(0.0F, -0.25F, 0.0F);
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.scale(0.65F, -0.65F, -0.65F);

				ItemWearable.usedModel = 2;
				minecraft.getItemRenderer().renderItem(living, stack, ItemTransforms.TransformType.HEAD);
				GlStateManager.popMatrix();
			}

			if ((visibility & 2) == 2) {
				GlStateManager.pushMatrix();
				GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
				if (living.isShiftKeyDown())
					GlStateManager.translate(0.0F, -0.2F, 0.0F);
				this.getBody().postRender(0);

				ItemWearable.usedModel = 1;
				minecraft.getItemRenderer().renderItem(living, stack, ItemTransforms.TransformType.HEAD);
				GlStateManager.popMatrix();
			}

			if (!ItemFromData.getData(stack).getString(PropertyType.ARMOR_IMAGE).isEmpty()) {
				this.renderer.bindTexture(this.getArmorResource(living, stack, EquipmentSlot.CHEST, null));
				EntityModel model = this.modelBig;
				model.setModelAttributes(this.renderer.getMainModel());
				model.setLivingAnimations(living, limbSwing, limbSwingAmount, partialTicks);
				model.render(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			}
			ItemWearable.usedModel = 0;
		}
		if (stack.getItem() instanceof ItemAmmoBelt) {
			this.renderer.bindTexture(this.getArmorResource(living, stack, EquipmentSlot.CHEST, null));
			EntityModel model = this.modelBig;
			model.setModelAttributes(this.renderer.getMainModel());
			model.setLivingAnimations(living, limbSwing, limbSwingAmount, partialTicks);
			model.render(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}

	public ResourceLocation getArmorResource(Entity entity, ItemStack stack,
			EquipmentSlot slot, String type) {
		return new ResourceLocation(
				ForgeHooksClient.getArmorTexture(entity, stack, "", slot, type));
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

	private ModelPart getHead() {
		return ((HumanoidModel) this.renderer.getMainModel()).bipedHead;
	}

	private ModelPart getBody() {
		return ((HumanoidModel) this.renderer.getMainModel()).bipedBody;
	}
}