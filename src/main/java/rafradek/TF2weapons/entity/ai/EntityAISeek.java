package rafradek.TF2weapons.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;

public class EntityAISeek extends RandomLookAroundGoal {

	private Mob host;

	public EntityAISeek(Mob entitylivingIn) {
		super(entitylivingIn);
		this.host = entitylivingIn;
	}

	@Override
	public boolean canUse() {
		return this.host.getRandom().nextFloat() < 0.13F;
	}
}
