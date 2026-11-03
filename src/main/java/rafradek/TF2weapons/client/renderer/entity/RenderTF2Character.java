package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.client.compat.RenderBiped;
import rafradek.TF2weapons.client.compat.LayerRenderer;
import rafradek.TF2weapons.client.compat.LayerHeldItem;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.ModelBiped;
import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;
import rafradek.TF2weapons.client.compat.OpenGlHelper;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.TF2EventsClient;
import rafradek.TF2weapons.client.model.ModelHeavy;
import rafradek.TF2weapons.client.model.ModelTF2Character;
import rafradek.TF2weapons.client.renderer.LayerArmorTint;
import rafradek.TF2weapons.client.renderer.LayerWearables;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntityHeavy;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemMeleeWeapon;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RenderTF2Character extends RenderBiped<EntityTF2Character> {

	public static final String TEXTURE_PATH_BASE = TF2weapons.MOD_ID + ":textures/entity/tf2/";

	public ModelBiped modelHeavy = new ModelHeavy();
	public ModelBiped modelMain;

	public RenderTF2Character(EntityRendererProvider.Context renderManager) {
		super(renderManager, new ModelTF2Character(), 0.5F);
		this.modelMain = (ModelBiped) this.mainModel;
		this.addLayer(new LayerHeldItem(this));
		this.addLayer(new LayerArmorTint(this));
		this.addLayer(new LayerWearables(this));
		this.layerRenderers.removeIf(layer -> (LayerRenderer<?>) layer instanceof LayerCustomHead);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTF2Character entity) {
		StringBuilder texture = new StringBuilder(TEXTURE_PATH_BASE);
		boolean disguised = WeaponsCapability.get(entity).isDisguised() &! TF2Util.isAlliedTo(Minecraft.getInstance().player, entity);
		if (entity.isRobot()) {
			texture.append("robot");
			if (entity.getOwnerUUID() != null) texture.append("_" + (entity.getTeam().getName().equals("BLU") ? "blu" : "red"));
		} else texture.append((entity.getEntTeam() + (disguised ? 1 : 0)) % 2 == 0 ? "red" : "blu");
		texture.append("/");
		if (disguised && WeaponsCapability.get(entity).getDisguiseType().startsWith("T:"))
			texture.append(TF2Class.getClass(WeaponsCapability.get(entity).getDisguiseType().substring(2)).getName());
		else texture.append(entity.getTF2Class().getName());
		return new ResourceLocation(texture.append(".png").toString());
	}

	@Override
	public void doRender(EntityTF2Character living, double p_76986_2_, double p_76986_4_, double p_76986_6_,
			float p_76986_8_, float p_76986_9_) {
		this.setModel(living);
		boolean sniperZoomed = false;
		if (living.getMainHandItem() != null
				&& !(living.getMainHandItem().getItem() instanceof ItemMeleeWeapon)) {
			sniperZoomed = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isCharging();
			// System.out.println("pos: "+p_76986_2_+" "+p_76986_4_+"
			// "+p_76986_8_);
			((ModelBiped) this.mainModel).rightArmPose = ((living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state
					& 3) > 0) || sniperZoomed ? ModelBiped.ArmPose.BOW_AND_ARROW : ModelBiped.ArmPose.EMPTY;
		}
		super.doRender(living, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
		if (living.isAlive()) {
			TF2EventsClient.renderBeam(living, p_76986_9_, 0.04f);
			if (sniperZoomed) {
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder renderer = tessellator.getBuffer();
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) p_76986_2_, (float) p_76986_4_ + living.getEyeHeight(),
						(float) p_76986_6_);
				GL11.glRotatef(
						(living.yHeadRotO
								+ (living.yHeadRot - living.yHeadRotO) * p_76986_9_) * -1,
						0.0F, 1.0F, 0.0F);
				GL11.glRotatef(
						(living.xRotO + (living.getXRot() - living.xRotO) * p_76986_9_),
						1.0F, 0.0F, 0.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.disableLighting();
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				ClientProxy.setColor(TF2Util.getTeamColor(living), 0.28f, 0, 0f, 1f);
				renderer.begin(7, DefaultVertexFormats.POSITION);
				renderer.pos(-0.03, -0.03, 0).endVertex();
				renderer.pos(0.03, 0.03, 0).endVertex();
				renderer.pos(0.03, 0.03, 50).endVertex();
				renderer.pos(-0.03, -0.03, 50).endVertex();
				tessellator.draw();
				renderer.begin(7, DefaultVertexFormats.POSITION);
				renderer.pos(-0.03, -0.03, 50).endVertex();
				renderer.pos(0.03, 0.03, 50).endVertex();
				renderer.pos(0.03, 0.03, 0).endVertex();
				renderer.pos(-0.03, -0.03, 0).endVertex();
				tessellator.draw();
				renderer.begin(7, DefaultVertexFormats.POSITION);
				renderer.pos(0.03, -0.03, 0).endVertex();
				renderer.pos(-0.03, 0.03, 0).endVertex();
				renderer.pos(-0.03, 0.03, 50).endVertex();
				renderer.pos(0.03, -0.03, 50).endVertex();
				tessellator.draw();
				renderer.begin(7, DefaultVertexFormats.POSITION);
				renderer.pos(0.03, -0.03, 50).endVertex();
				renderer.pos(-0.03, 0.03, 50).endVertex();
				renderer.pos(-0.03, 0.03, 0).endVertex();
				renderer.pos(0.03, -0.03, 0).endVertex();
				tessellator.draw();
				/*
				 * renderer.startDrawingQuads(); renderer.addVertex(-0.03,
				 * p_76986_1_.getEyeHeight()-0.03,0); renderer.addVertex(0.03,
				 * p_76986_1_.getEyeHeight()+0.03,0);
				 * renderer.addVertex(lookVec.x*50+0.03,lookVec.y*64+
				 * p_76986_1_.getEyeHeight()+0.03, lookVec.z*64);
				 * renderer.addVertex(lookVec.x*50-0.03,lookVec.y*64+
				 * p_76986_1_.getEyeHeight()-0.03, lookVec.z*64); tessellator.draw();
				 */
				/*
				 * renderer.startDrawingQuads(); renderer.addVertex(-0.03,
				 * p_76986_1_.getEyeHeight()-0.03, -0.03); renderer.addVertex(0.03,
				 * p_76986_1_.getEyeHeight()+0.03, +0.03);
				 * renderer.addVertex(lookVec.x*50+0.03,lookVec.y*64+
				 * p_76986_1_.getEyeHeight()+0.03, lookVec.z*64+0.03);
				 * renderer.addVertex(lookVec.x*50-0.03,lookVec.y*64+
				 * p_76986_1_.getEyeHeight()-0.03, lookVec.z*64-0.03); tessellator.draw();
				 * renderer.startDrawingQuads(); renderer.addVertex(0.03,
				 * p_76986_1_.getEyeHeight()-0.03, 0.03); renderer.addVertex(-0.03,
				 * p_76986_1_.getEyeHeight()+0.03, -0.03);
				 * renderer.addVertex(lookVec.x*50-0.03,lookVec.y*64+
				 * p_76986_1_.getEyeHeight()+0.03, lookVec.z*64-0.03);
				 * renderer.addVertex(lookVec.x*50+0.03,lookVec.y*64+
				 * p_76986_1_.getEyeHeight()-0.03, lookVec.z*64+0.03); tessellator.draw();
				 */
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
				GL11.glDisable(GL11.GL_BLEND);
				GlStateManager.enableTexture2D();
				GlStateManager.enableLighting();
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	protected void preRenderCallback(EntityTF2Character entitylivingbaseIn, float partialTickTime) {
		float f = 0.9375F;
		if (entitylivingbaseIn instanceof EntityHeavy)
			f = 1f;
		if (entitylivingbaseIn.getRobotSize() == 2)
			f *= 1.75f;
		else if (entitylivingbaseIn.getRobotSize() == 3)
			f *= 2f;
		GlStateManager.scale(f, f, f);
	}

	private void setModel(LivingEntity living) {
		if (living instanceof EntityHeavy) {
			this.mainModel = this.modelHeavy;
		} else {
			this.mainModel = this.modelMain;
		}
		/*
		 * if(living.getEntityData().getByte("Disguised")!=0){ String
		 * mobType=living.getEntityData().getString("DisguiseType");
		 * this.mainModel=ClientProxy.entityModel.get(mobType); }
		 */
	}
}
