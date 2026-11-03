package rafradek.TF2weapons.client.model;

import rafradek.TF2weapons.entity.building.EntitySentry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

/**
 * ModelSentry - Radafrek + Wolfkann
 * Created using Tabula 6.0.0
 */
public class ModelSentry extends EntityModel {
    public ModelPart head;
    public ModelPart leg1;
    public ModelPart leg2;
    public ModelPart foot3;
    public ModelPart foot2;
    public ModelPart legbase;
    public ModelPart backleg1;
    public ModelPart backleg2;
    public ModelPart main;
    public ModelPart leg3;
    public ModelPart leg4;
    public ModelPart foot1;
    public ModelPart foot4;
    public ModelPart backleg2_1;
    public ModelPart legbase_1;
    public ModelPart headChild;
    public ModelPart headChild_1;
    public ModelPart headChild_2;
    public ModelPart Bottem;
    public ModelPart Line;
    public ModelPart Line_1;
    public ModelPart LineSquare;
    public ModelPart minihead;
    public ModelPart minilight;

    public ModelSentry() {
        textureWidth = 64;
        textureHeight = 32;
        legbase_1 = new ModelPart(this, 13, 0);
        legbase_1.setRotationPoint(-1.5F, 16.7F, -2.4F);
        legbase_1.addBox(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
        setRotateAngle(legbase_1, -0.18360863730980345F, 0.0F, 0.0F);
        Bottem = new ModelPart(this, 0, 0);
        Bottem.setRotationPoint(-0.5F, 0.5F, 4.6F);
        Bottem.addBox(0.0F, 0.0F, 0.0F, 1, 2, 2, 0.0F);
        setRotateAngle(Bottem, -0.6850417314077744F, 0.0F, 0.0F);
        Line = new ModelPart(this, 0, 0);
        Line.setRotationPoint(0.0F, 2.0F, 4.1F);
        Line.addBox(-0.5F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
        setRotateAngle(Line, -0.7277663629049491F, 0.0F, 0.0F);
        headChild_2 = new ModelPart(this, 0, 17);
        headChild_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        headChild_2.addBox(0.0F, 0.0F, -4.0F, 1, 1, 1, 0.0F);
        foot4 = new ModelPart(this, 22, 6);
        foot4.setRotationPoint(-3.0F, 23.0F, 4.3F);
        foot4.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
        setRotateAngle(foot4, 0.0F, -0.08726646259971647F, 0.0F);
        leg1 = new ModelPart(this, 0, 0);
        leg1.setRotationPoint(0.0F, 20.0F, -1.5F);
        leg1.addBox(0.0F, -0.5F, -0.30000001192092896F, 1, 1, 7, 0.0F);
        setRotateAngle(leg1, -0.6981316804885863F, -2.5307273864746094F, 0.0F);
        leg4 = new ModelPart(this, 0, 0);
        leg4.setRotationPoint(1.7F, 20.3F, 3.5F);
        leg4.addBox(-0.4F, 0.0F, 0.0F, 1, 1, 7, 0.0F);
        setRotateAngle(leg4, -0.951378975262109F, 0.08726646259971647F, 0.0F);
        Line_1 = new ModelPart(this, 0, 0);
        Line_1.setRotationPoint(0.0F, 3.6F, 2.9F);
        Line_1.addBox(-0.5F, 0.0F, 0.0F, 1, 6, 1, 0.0F);
        setRotateAngle(Line_1, -2.2979004931757343F, 0.0F, 0.0F);
        headChild_1 = new ModelPart(this, 32, 10);
        headChild_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        headChild_1.addBox(-7.0F, -2.0F, -3.5F, 5, 4, 7, 0.0F);
        setRotateAngle(headChild_1, 0.0F, 1.570796012878418F, 0.0F);
        foot3 = new ModelPart(this, 22, 6);
        foot3.setRotationPoint(0.9F, 23.0F, 4.3F);
        foot3.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
        setRotateAngle(foot3, 0.0F, 0.08726646259971647F, 0.0F);
        leg2 = new ModelPart(this, 0, 0);
        leg2.setRotationPoint(0.0F, 20.0F, -1.5F);
        leg2.addBox(-1.0F, -0.5F, -0.30000001192092896F, 1, 1, 7, 0.0F);
        setRotateAngle(leg2, -0.6981316804885863F, 2.5307273864746094F, 0.04555309191346169F);
        backleg1 = new ModelPart(this, 15, 9);
        backleg1.setRotationPoint(0.5F, 19.2F, -0.1F);
        backleg1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
        setRotateAngle(backleg1, -0.4363323129985824F, 0.08726646259971647F, 0.0F);
        foot2 = new ModelPart(this, 22, 6);
        foot2.setRotationPoint(-4.4F, 23.0F, -5.2F);
        foot2.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
        setRotateAngle(foot2, 0.0F, 0.6108652381980153F, 0.0F);
        headChild = new ModelPart(this, 32, 10);
        headChild.setRotationPoint(0.0F, 0.0F, 0.0F);
        headChild.addBox(-2.5F, -2.0F, 1.0F, 5, 4, 7, 0.0F);
        LineSquare = new ModelPart(this, 0, 0);
        LineSquare.setRotationPoint(0.0F, 3.3F, 3.9F);
        LineSquare.addBox(-0.5F, 0.0F, 0.0F, 1, 2, 2, 0.0F);
        setRotateAngle(LineSquare, -1.8484782107871942F, 0.0F, 0.0F);
        backleg2 = new ModelPart(this, 15, 9);
        backleg2.setRotationPoint(-1.1F, 19.2F, -0.1F);
        backleg2.addBox(0.0F, 0.0F, 0.0F, 2, 1, 2, 0.0F);
        setRotateAngle(backleg2, -0.4363323129985824F, 0.0F, 0.0F);
        main = new ModelPart(this, 0, 0);
        main.setRotationPoint(0.0F, 18.0F, -1.1F);
        main.addBox(-0.5F, -3.5F, -0.5F, 1, 8, 1, 0.0F);
        setRotateAngle(main, -0.18369277626926808F, 0.0F, 0.0F);
        head = new ModelPart(this, 0, 15);
        head.setRotationPoint(0.0F, 13.0F, -0.2F);
        head.addBox(-2.0F, -1.5F, -3.0F, 4, 3, 4, 0.25F);
        legbase = new ModelPart(this, 13, 0);
        legbase.setRotationPoint(-1.5F, 18.6F, -2.8F);
        legbase.addBox(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
        setRotateAngle(legbase, -0.18360863730980345F, 0.0F, 0.0F);
        foot1 = new ModelPart(this, 22, 6);
        foot1.setRotationPoint(2.6F, 22.9F, -6.2F);
        foot1.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
        setRotateAngle(foot1, -0.0F, -0.6108652381980153F, 0.0F);
        leg3 = new ModelPart(this, 0, 0);
        leg3.setRotationPoint(-2.0F, 20.3F, 3.5F);
        leg3.addBox(-0.4F, 0.0F, 0.0F, 1, 1, 7, 0.0F);
        setRotateAngle(leg3, -0.9339256827421656F, -0.08726646259971647F, 0.0F);
        backleg2_1 = new ModelPart(this, 15, 9);
        backleg2_1.setRotationPoint(-1.6F, 19.2F, -0.1F);
        backleg2_1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
        setRotateAngle(backleg2_1, -0.4363323129985824F, -0.08726646259971647F, 0.0F);
        minihead = new ModelPart(this, 16, 25);
        minihead.mirror = true;
        minihead.setRotationPoint(0.0F, 0.0F, 0.0F);
        minihead.addBox(-1.0F, -6.0F, 3.0F, 3, 4, 3, 0.0F);
        minilight = new ModelPart(this, 0, 28);
        minilight.mirror = true;
        minilight.setRotationPoint(0.5F, -7.0F, 4.5F);
        minilight.addBox(-4.0F, 0.0F, 0.0F, 8, 4, 0, 0.0F);
        head.addChild(Bottem);
        head.addChild(Line);
        head.addChild(headChild_2);
        head.addChild(Line_1);
        head.addChild(headChild_1);
        head.addChild(headChild);
        head.addChild(LineSquare);
        head.addChild(minihead);
        head.addChild(minilight);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
    	
    	minihead.isHidden = minilight.isHidden = !((EntitySentry)entity).isMini();
    	
    	legbase_1.render(f5);
        foot4.render(f5);
        leg1.render(f5);
        leg4.render(f5);
        foot3.render(f5);
        leg2.render(f5);
        backleg1.render(f5);
        foot2.render(f5);
        backleg2.render(f5);
        main.render(f5);
        legbase.render(f5);
        foot1.render(f5);
        leg3.render(f5);
        backleg2_1.render(f5);
        GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		if (((EntitySentry)entity).isMini()) {
			int i = 0xFFffff;
	        int j = i % 65536;
	        int k = i / 65536;
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		}
        head.render(f5);
        GlStateManager.disableBlend();
        
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
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		head.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
		head.rotateAngleX = headPitch / (180F / (float) Math.PI);
		minilight.rotateAngleY = ageInTicks / 2;
	}
}
