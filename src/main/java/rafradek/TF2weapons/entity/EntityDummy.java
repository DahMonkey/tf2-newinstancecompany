package rafradek.TF2weapons.entity;

import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;

public class EntityDummy extends Mob {

	public WeaponsCapability cap;

	public EntityDummy(Level world) {
		this(TF2weapons.DUMMY, world);
	}

	public EntityDummy(EntityType<? extends EntityDummy> type, Level world) {
		super(type, world);
		cap = new WeaponsCapability(this);
		this.setSize(0, 0);
		this.discard();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability,
			@Nullable Direction facing) {
		if (capability == TF2weapons.WEAPONS_CAP)
			return (T) cap;
		else
			return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability,
			@Nullable Direction facing) {
		return capability == TF2weapons.WEAPONS_CAP || super.hasCapability(capability, facing);
	}
}
