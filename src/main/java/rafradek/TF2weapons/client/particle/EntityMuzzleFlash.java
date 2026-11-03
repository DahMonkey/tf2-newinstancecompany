package rafradek.TF2weapons.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityMuzzleFlash extends Particle {

	private int duration;
	private boolean nextDead;
	private LivingEntity owner;
	private InteractionHand hand;

	public EntityMuzzleFlash(Level par1World, LivingEntity shooter, InteractionHand hand) {
		super(par1World, 0, 0, 0);
		this.lifetime = 5;
		this.hand = hand;
		this.setSize(0.025f, 0.025f);
		// this.setParticleIcon(Item.itemsList[2498+256].getIconFromDamage(0));
		this.setParticleTextureIndex(65);
		// this.setParticleTextureIndex(81);
		this.multipleParticleScaleBy(1);
		this.owner = shooter;
		this.setPos();
		this.setColor(0.97f, 0.76f, 0.51f);
		// S/ystem.out.println("Crits: "+crits);e
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.quadSize *= 0.92f;
		this.alpha *= 0.8f;
		this.setPos();

	}

	public void setPosition() {
		float move = Minecraft.getInstance().player == this.owner ? 0.48f : 0.24f;
		this.setPos((owner.getX() - Mth.cos(owner.yHeadRot / 180.0F * (float) Math.PI) * move
				* (hand == InteractionHand.MAIN_HAND ? 1 : -1)), this.getY(), this.getZ());
		this.setPos(this.getX(), (owner.getY() + owner.getEyeHeight() - move * 0.45f), this.getZ());
		this.setPos(this.getX(), this.getY(), (owner.getZ() - Mth.sin(owner.yHeadRot / 180.0F * (float) Math.PI) * move
				* (hand == InteractionHand.MAIN_HAND ? 1 : -1)));

		this.setPos(this.getX() + (-Mth.sin(owner.yHeadRot / 180.0F * (float) Math.PI)
				* Mth.cos(owner.getXRot() / 180.0F * (float) Math.PI)), this.getY(), this.getZ());
		this.setPos(this.getX(), this.getY(), this.getZ() + (Mth.cos(owner.yHeadRot / 180.0F * (float) Math.PI)
				* Mth.cos(owner.getXRot() / 180.0F * (float) Math.PI)));
		this.setPos(this.getX(), this.getY() + ((-Mth.sin(owner.getXRot() / 180.0F * (float) Math.PI))), this.getZ());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}
}
