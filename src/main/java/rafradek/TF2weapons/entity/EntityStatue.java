package rafradek.TF2weapons.entity;

import net.minecraft.world.entity.EntityType;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemStatue;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class EntityStatue extends Entity implements IEntityAdditionalSpawnData {

	private net.minecraft.world.entity.EntityDimensions legacySize;

	/** 1.12 setSize: overrides the entity type's size for this entity. */
	public void setSize(float width, float height) {
		this.legacySize = net.minecraft.world.entity.EntityDimensions.scalable(width, height);
		this.refreshDimensions();
	}

	@Override
	public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
		return this.legacySize != null ? this.legacySize : super.getDimensions(pose);
	}

	public LivingEntity entity;
	public CompoundTag data;
	public boolean isFeign;
	public float yBodyRot;
	public boolean first;
	public boolean clientOnly;
	public int ticksLeft;
	public boolean player;
	public boolean useHand;
	public GameProfile profile;
	public float armSwing;

	public EntityStatue(Level world) {
		this(TF2weapons.STATUE, world);
	}

	public EntityStatue(EntityType<? extends EntityStatue> type, Level world) {
		super(type, world);
	}

	public EntityStatue(Level world, LivingEntity toCopy, boolean isFeign) {
		this(TF2weapons.STATUE, world);
		this.setPos(toCopy.getX(), toCopy.getY(), toCopy.getZ());
		this.setSize(toCopy.getBbWidth(), toCopy.getBbHeight());
		entity = toCopy;
		if (!world.isClientSide) {
			if (toCopy instanceof Player) {
				data = toCopy.saveWithoutId(new CompoundTag());
				player = true;
				profile = ((Player) toCopy).getGameProfile();
			} else data = toCopy.serializeNBT();
			ticksLeft = toCopy instanceof Player || !toCopy.canChangeDimensions() ? -1 : 1200;
			useHand = toCopy.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
					&& ((WeaponsCapability.get(toCopy).state & 3) != 0 || WeaponsCapability.get(toCopy).isCharging());
		} else clientOnly = true;
		// this.data = this.entity.serializeNBT();
		this.isFeign = isFeign;
		if (isFeign) {
			setDeltaMovement((entity.getDeltaMovement().x), getDeltaMovement().y, getDeltaMovement().z);
			setDeltaMovement(getDeltaMovement().x, (entity.getDeltaMovement().y), getDeltaMovement().z);
			setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y, (entity.getDeltaMovement().z));
		} else {
			entity.deathTime = 0;
			entity.hurtTime = 0;
			yBodyRot = toCopy.yBodyRot;
			yRotO = entity.yRotO;
			setYRot((entity.getYRot()));
		}
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	public boolean isPickable() {
		return !isRemoved() && !clientOnly;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!isInvulnerableTo(source) && source.getDirectEntity() instanceof Player) {
			if (data != null) spawnAtLocation(ItemStatue.getStatue(this), 0);
			discard();
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		if (isOnGround()) {
			setDeltaMovement(getDeltaMovement().x * (0.1), getDeltaMovement().y, getDeltaMovement().z);
			setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z * (0.1));
		}
		setDeltaMovement(getDeltaMovement().x * (0.98), getDeltaMovement().y, getDeltaMovement().z);
		setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y * (0.98), getDeltaMovement().z);
		setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z * (0.98));
		setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y - (0.08), getDeltaMovement().z);
		if (!level.isClientSide && ticksLeft >= 0 && ticksLeft-- == 0) discard();
		move(MoverType.SELF, getDeltaMovement());
		if (isFeign && tickCount >= 20) {
			discard();
			for (int k = 0; k < 20; ++k) {
				double d2 = random.nextGaussian() * 0.02D;
				double d0 = random.nextGaussian() * 0.02D;
				double d1 = random.nextGaussian() * 0.02D;
				float width = getBbWidth();
				level.addParticle(ParticleTypes.POOF, getX() + random.nextFloat() * width * 2.0F - width,
						getY() + random.nextFloat() * getBbHeight(), getZ() + random.nextFloat() * width * 2.0F - width, d2, d0, d1);
			}
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		if (!TF2ConfigVars.australiumStatue) {
			discard();
			return;
		}
		data = compound.getCompound("Entity");
		ticksLeft = compound.getShort("TicksLeft");
		player = compound.getBoolean("Player");
		if (player) profile = NbtUtils.readGameProfile(compound.getCompound("Profile"));
		useHand = compound.getBoolean("UseArm");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		try {
			if (data != null) compound.put("Entity", data);
		} catch (Exception e) {}
		compound.putShort("TicksLeft", (short) ticksLeft);
		if (profile != null) compound.put("Profile", NbtUtils.writeGameProfile(new CompoundTag(), profile));
		compound.putBoolean("Player", player);
		compound.putBoolean("UseArm", useHand);
	}

	@Override
	public net.minecraft.network.protocol.Packet<?> getAddEntityPacket() {
		return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buff) {
		if (first) return;
		buff.writeBoolean(profile != null);
		if (profile != null) {
			buff.writeUUID(profile.getId());
			buff.writeUtf(profile.getName());
			buff.writeVarInt(profile.getProperties().size());
			for (com.mojang.authlib.properties.Property property : profile.getProperties().values()) {
				buff.writeUtf(property.getName());
				buff.writeUtf(property.getValue());
				if (property.hasSignature()) {
					buff.writeBoolean(true);
					buff.writeUtf(property.getSignature());
				} else buff.writeBoolean(false);
			}
		}
		int pos = buff.writerIndex();
		buff.writeNbt(data);
		if (buff.writerIndex() - pos >= 2097152) buff.clear();
		buff.writeBoolean(useHand);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buff) {
		if (buff.readableBytes() == 0) return;
		try {
			if (buff.readBoolean()) {
				profile = new GameProfile(buff.readUUID(), buff.readUtf(16));
				int l = buff.readVarInt();
				int i1 = 0;
				for (; i1 < l; ++i1) {
					String s = buff.readUtf(32767);
					String s1 = buff.readUtf(32767);
					if (buff.readBoolean()) profile.getProperties().put(s, new Property(s, s1, buff.readUtf(32767)));
					else profile.getProperties().put(s, new Property(s, s1));
				}
				entity = rafradek.TF2weapons.client.ClientStatueHelper.createDisplayEntity(level, profile, buff.readNbt());
			} else
				entity = rafradek.TF2weapons.client.ClientStatueHelper.createDisplayEntity(level, null, buff.readNbt());
			if (entity == null) {
				discard();
				return;
			}
			entity.deathTime = 0;
			entity.hurtTime = 0;
			entity.animationSpeed = 0.5f;
			entity.tickCount = 15;
			entity.animationPosition += random.nextFloat() * 10;
			setSize(entity.getBbWidth(), entity.getBbHeight());
			entity.setPos(getX(), getY(), getZ());
			entity.yHeadRot = getYRot();
			entity.yBodyRot = getYRot();
			entity.yBodyRotO = getYRot();
			if (buff.readBoolean()) WeaponsCapability.get(entity).state = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
