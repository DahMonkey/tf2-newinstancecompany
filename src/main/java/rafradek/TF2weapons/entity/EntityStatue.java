package rafradek.TF2weapons.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemStatue;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class EntityStatue extends Entity implements IEntityAdditionalSpawnData {

	public LivingEntity entity;
	public CompoundTag data;
	public boolean isFeign;
	public float renderYawOffset;
	public boolean first;
	public boolean clientOnly;
	public int ticksLeft;
	public boolean player;
	public boolean useHand;
	public GameProfile profile;
	public float armSwing;

	public EntityStatue(Level world) {
		super(world);
	}

	public EntityStatue(Level world, LivingEntity toCopy, boolean isFeign) {
		super(world);
		this.setPos(toCopy.getX(), toCopy.getY(), toCopy.getZ());
		width = toCopy.getBbWidth();
		height = toCopy.getBbHeight();
		entity = toCopy;
		if (!world.isClientSide) {
			if (toCopy instanceof Player) {
				data = toCopy.writeToNBT(new CompoundTag());
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
			motionX = entity.getDeltaMovement().x;
			motionY = entity.getDeltaMovement().y;
			motionZ = entity.getDeltaMovement().z;
		} else {
			entity.deathTime = 0;
			entity.hurtTime = 0;
			renderYawOffset = toCopy.yBodyRot;
			prevRotationYaw = entity.yRotO;
			rotationYaw = entity.getYRot();
		}
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	public boolean canBeCollidedWith() {
		return !isRemoved() && !clientOnly;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!isEntityInvulnerable(source) && source.getImmediateSource() instanceof Player) {
			if (data != null) entityDropItem(ItemStatue.getStatue(this), 0);
			discard();
			return true;
		}
		return false;
	}

	@Override
	public void onUpdate() {
		if (onGround) {
			motionX *= 0.1;
			motionZ *= 0.1;
		}
		motionX *= 0.98;
		motionY *= 0.98;
		motionZ *= 0.98;
		motionY -= 0.08;
		if (!world.isClientSide && ticksLeft >= 0 && ticksLeft-- == 0) discard();
		move(MoverType.SELF, motionX, motionY, motionZ);
		if (isFeign && tickCount >= 20) {
			discard();
			for (int k = 0; k < 20; ++k) {
				double d2 = rand.nextGaussian() * 0.02D;
				double d0 = rand.nextGaussian() * 0.02D;
				double d1 = rand.nextGaussian() * 0.02D;
				world.addParticle(ParticleTypes.EXPLOSION_NORMAL, posX + rand.nextFloat() * width * 2.0F - width,
						posY + rand.nextFloat() * height, posZ + rand.nextFloat() * width * 2.0F - width, d2, d0, d1);
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
		if (player) profile = NbtUtils.readGameProfileFromNBT(compound.getCompound("Profile"));
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
	public void writeSpawnData(ByteBuf buffer) {
		if (first) return;
		FriendlyByteBuf buff = new FriendlyByteBuf(buffer);
		buffer.writeBoolean(player);
		if (profile != null) {
			buff.writeUniqueId(profile.getId());
			buff.writeString(profile.getName());
			buff.writeVarInt(profile.getProperties().size());
			for (Property property : profile.getProperties().values()) {
				buff.writeString(property.getName());
				buff.writeString(property.getValue());
				if (property.hasSignature()) {
					buff.writeBoolean(true);
					buff.writeString(property.getSignature());
				} else buff.writeBoolean(false);
			}
		}
		int pos = buff.writerIndex();
		buff.writeCompoundTag(data);
		if (buff.writerIndex() - pos >= 2097152) buff.clear();
		buff.writeBoolean(useHand);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readSpawnData(ByteBuf additionalData) {
		if (additionalData.readableBytes() == 0) return;
		try {
			FriendlyByteBuf buff = new FriendlyByteBuf(additionalData);
			if (buff.readBoolean()) {
				profile = new GameProfile(buff.readUniqueId(), buff.readString(16));
				int l = buff.readVarInt();
				int i1 = 0;
				for (; i1 < l; ++i1) {
					String s = buff.readString(32767);
					String s1 = buff.readString(32767);
					if (buff.readBoolean()) profile.getProperties().put(s, new Property(s, s1, buff.readString(32767)));
					else profile.getProperties().put(s, new Property(s, s1));
				}
				final PlayerInfo info = new PlayerInfo(profile);
				entity = new RemotePlayer(world, profile) {
					@Override
					@Nullable
					protected PlayerInfo getPlayerInfo() {
						return info;
					}
				};
				entity.readFromNBT(buff.readCompoundTag());
			} else entity = (LivingEntity) EntityList.createEntityFromNBT(buff.readCompoundTag(), world);
			if (entity == null) {
				discard();
				return;
			}
			entity.deathTime = 0;
			entity.hurtTime = 0;
			entity.limbSwingAmount = 0.5f;
			entity.tickCount = 15;
			entity.limbSwing += rand.nextFloat() * 10;
			setSize(entity.getBbWidth(), entity.getBbHeight());
			entity.setPos(posX, posY, posZ);
			entity.rotationYawHead = rotationYaw;
			entity.yBodyRot = rotationYaw;
			entity.yBodyRotO = rotationYaw;
			if (buff.readBoolean()) WeaponsCapability.get(entity).state = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
