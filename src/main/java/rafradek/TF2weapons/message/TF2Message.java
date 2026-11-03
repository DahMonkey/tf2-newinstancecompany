package rafradek.TF2weapons.message;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.arena.GameArena;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.Contract;
import rafradek.TF2weapons.util.Contract.Objective;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class TF2Message implements IMessage {

	public static class ActionMessage extends TF2Message {
		int value;
		int entity;

		public ActionMessage() {

		}

		public ActionMessage(int value, LivingEntity entity) {
			this.value = value;
			this.entity = entity.getId();
		}

		public ActionMessage(int value) {
			this.value = value;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.value = buf.readByte();
			if (buf.readableBytes() > 0)
				this.entity = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(this.value);
			if (this.entity != 0)
				buf.writeInt(this.entity);
		}
	}

	public static class DisguiseMessage extends TF2Message {
		String value;

		public DisguiseMessage() {

		}

		public DisguiseMessage(String name) {
			this.value = name;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			int stringLength = buf.readByte();
			value = buf.toString(buf.readerIndex(), stringLength, StandardCharsets.UTF_8);
			buf.readerIndex(buf.readerIndex() + stringLength);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			byte[] stringValueArray = value.getBytes(StandardCharsets.UTF_8);
			buf.writeByte(stringValueArray.length);
			buf.writeBytes(stringValueArray);
		}
	}

	public static class UseMessage extends TF2Message {
		int value;
		int newAmmo;
		boolean reload;
		InteractionHand hand;

		public UseMessage() {}

		public UseMessage(int value, boolean reload, int newAmmoValue, InteractionHand hand) {
			this.value = value;
			this.newAmmo = newAmmoValue;
			this.reload = reload;
			this.hand = hand;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.value = buf.readShort();
			this.reload = buf.readBoolean();
			this.newAmmo = buf.readShort();
			this.hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeShort(value);
			buf.writeBoolean(reload);
			buf.writeShort(newAmmo);
			buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
		}

	}

	public static class PredictionMessage extends TF2Message {
		public double x;
		public double y;
		public double z;
		public float pitch;
		public float yaw;
		public long time;
		// public int slot;
		public InteractionHand hand;
		public List<HitResult> target;
		public List<Object[]> readData;
		public int state;

		public PredictionMessage() {}

		public PredictionMessage(double x, double y, double z, float pitch, float yaw, int state, InteractionHand hand) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.pitch = pitch;
			this.yaw = yaw;
			this.hand = hand;
			this.state = state;
		}

		public PredictionMessage(double x, double y, double z, float pitch, float yaw, int state, InteractionHand hand,
				List<HitResult> target2) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.pitch = pitch;
			this.yaw = yaw;
			this.hand = hand;
			this.target = target2;
			this.state = state;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.x = buf.readDouble();
			this.y = buf.readDouble();
			this.z = buf.readDouble();
			this.pitch = buf.readFloat();
			this.yaw = buf.readFloat();
			this.hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			this.state = buf.readByte();
			if (buf.readableBytes() > 0) {
				this.readData = new ArrayList<>();
				while (buf.readableBytes() > 0) {
					Object[] obj = new Object[9];
					if (buf.readBoolean()) {
						obj[0] = buf.readInt();
						// obj[1]=buf.readFloat();
						// obj[2]=buf.readFloat();
						// obj[3]=buf.readFloat();
						obj[1] = buf.readBoolean();

					} else {
						obj[1] = buf.readByte();
						obj[3] = buf.readInt();
						obj[4] = buf.readInt();
						obj[5] = buf.readInt();
						// obj[1]=buf.readFloat();
						// obj[2]=buf.readFloat();
						// obj[3]=buf.readFloat();
					}
					obj[6] = buf.readByte();
					obj[7] = buf.readByte();
					obj[8] = buf.readByte();
					obj[2] = buf.readFloat();
					this.readData.add(obj);
				}
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeDouble(x);
			buf.writeDouble(y);
			buf.writeDouble(z);
			buf.writeFloat(pitch);
			buf.writeFloat(yaw);
			buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
			buf.writeByte(state);
			if (target != null)
				for (HitResult mop : target) {
					if (mop.getEntity() != null) {
						buf.writeBoolean(true);
						buf.writeInt(mop.getEntity().getId());
						// if(mop.hitVec!=null){
						// buf.writeFloat((float) mop.hitVec.x);
						// buf.writeFloat((float) mop.hitVec.y);
						// buf.writeFloat((float) mop.hitVec.z);
						// }
						// buf.writeInt(mop.entityHit.getEntityId());
						buf.writeBoolean(((float[]) mop.hitInfo)[0] == 1);
						buf.writeByte((byte) ((mop.getLocation().x - mop.getEntity().getX()) * 16));
						buf.writeByte((byte) ((mop.getLocation().y - mop.getEntity().getY()) * 16));
						buf.writeByte((byte) ((mop.getLocation().z - mop.getEntity().getZ()) * 16));
					} else {
						buf.writeBoolean(false);
						buf.writeByte(mop.getDirection().get3DDataValue());
						buf.writeInt(mop.getBlockPos().getX());
						buf.writeInt(mop.getBlockPos().getY());
						buf.writeInt(mop.getBlockPos().getZ());
						buf.writeByte((byte) ((mop.getLocation().x - mop.getBlockPos().getX()) * 16));
						buf.writeByte((byte) ((mop.getLocation().y - mop.getBlockPos().getY()) * 16));
						buf.writeByte((byte) ((mop.getLocation().z - mop.getBlockPos().getZ()) * 16));
					}
					buf.writeFloat(((float[]) mop.hitInfo)[1]);

				}
		}

	}

	public static class PropertyMessage extends TF2Message {
		String name;
		int intValue;
		float floatValue;
		short shortValue;
		byte byteValue;
		String stringValue;
		int entityID;
		byte type;

		public PropertyMessage() {}

		public PropertyMessage(String name, Number value) {
			this.name = name;
			if (value instanceof Integer) {
				this.type = 0;
				this.intValue = value.intValue();
			} else if (value instanceof Float) {
				this.type = 1;
				this.floatValue = value.floatValue();
			} else if (value instanceof Byte) {
				this.type = 2;
				this.byteValue = value.byteValue();
			}
		}

		public PropertyMessage(String name, Number value, Entity entity) {
			this(name, value);
			this.entityID = entity.getId();
		}

		public PropertyMessage(String name, String value, Entity entity) {
			this.type = 3;
			this.name = name;
			this.stringValue = value;
			this.entityID = entity.getId();
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			entityID = buf.readInt();
			type = buf.readByte();
			if (type == 0)
				intValue = buf.readInt();
			else if (type == 1)
				floatValue = buf.readFloat();
			else if (type == 2)
				byteValue = buf.readByte();
			else if (type == 3) {
				int stringLength = buf.readByte();
				stringValue = buf.toString(buf.readerIndex(), stringLength, StandardCharsets.UTF_8);
				buf.readerIndex(buf.readerIndex() + stringLength);
			}
			// value=buf.readInt();
			name = buf.toString(buf.readerIndex(), buf.readableBytes(), StandardCharsets.UTF_8);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(entityID);
			buf.writeByte(type);
			if (type == 0)
				buf.writeInt(intValue);
			else if (type == 1)
				buf.writeFloat(floatValue);
			else if (type == 2)
				buf.writeByte(byteValue);
			else if (type == 3) {
				byte[] stringValueArray = stringValue.getBytes(StandardCharsets.UTF_8);
				buf.writeByte(stringValueArray.length);
				buf.writeBytes(stringValueArray);
			}
			byte[] stringNameArray = name.getBytes(StandardCharsets.UTF_8);
			buf.writeBytes(stringNameArray);
		}

	}

	public static class CapabilityMessage extends TF2Message {
		int entityID;
		long totalTime;
		List<SynchedEntityData.DataEntry<?>> entries;
		boolean sendAll;

		public CapabilityMessage() {}

		public CapabilityMessage(Entity entity, boolean sendAll) {
			WeaponsCapability cap = entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
			this.entityID = entity.getId();
			this.totalTime = cap.ticksTotal;
			if (sendAll) {
				this.entries = cap.entityData.getAll();
			} else {
				this.entries = cap.entityData.getDirty();
			}
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			entityID = buf.readInt();
			totalTime = buf.readLong();
			try {
				entries = SynchedEntityData.readEntries(new FriendlyByteBuf(buf));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(entityID);
			buf.writeLong(totalTime);
			try {
				SynchedEntityData.writeEntries(entries, new FriendlyByteBuf(buf));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static class GameArenaMessage extends TF2Message {
		Map<UUID, List<SynchedEntityData.DataEntry<?>>> playerfoUUID = new HashMap<>();
		int arenaId;
		Type type;

		enum Type {
			ADD, UPDATE, REMOVE
		}

		public GameArenaMessage() {}

		public GameArenaMessage(GameArena arena, Type type) {
			this.type = type;
			this.arenaId = arena.networkId;
			if (type != Type.REMOVE) {
				for (Entry<UUID, SynchedEntityData> entry : arena.playerInfoUUID.entrySet()) {
					if (type == Type.ADD) {
						playerfoUUID.put(entry.getKey(), entry.getValue().getAll());
					} else {
						playerfoUUID.put(entry.getKey(), entry.getValue().getDirty());
					}
				}
			}
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.type = Type.values()[buf.readByte()];
			this.arenaId = buf.readInt();
			while (buf.readableBytes() > 0) {
				try {
					UUID uuid = new FriendlyByteBuf(buf).readUniqueId();
					playerfoUUID.put(uuid, SynchedEntityData.readEntries(new FriendlyByteBuf(buf)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(this.type.ordinal());
			buf.writeInt(this.arenaId);
			for (Entry<UUID, List<SynchedEntityData.DataEntry<?>>> entry : playerfoUUID.entrySet()) {
				new FriendlyByteBuf(buf).writeUniqueId(entry.getKey());
				try {
					SynchedEntityData.writeEntries(entry.getValue(), new FriendlyByteBuf(buf));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static class PlayerCapabilityMessage extends TF2Message {
		List<SynchedEntityData.DataEntry<?>> entries;
		int entityID;
		boolean global;

		public PlayerCapabilityMessage() {}

		public PlayerCapabilityMessage(Entity entity, boolean sendAll, boolean global) {
			TF2PlayerCapability cap = entity.getCapability(TF2weapons.PLAYER_CAP).orElse(null);
			this.entityID = entity.getId();
			this.global = global;
			if (global) {
				if (sendAll) {
					this.entries = cap.dataManagerGlobal.getAll();
				} else {
					this.entries = cap.dataManagerGlobal.getDirty();
				}
			} else {
				if (sendAll) {
					this.entries = cap.entityData.getAll();
				} else {
					this.entries = cap.entityData.getDirty();
				}
			}
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			entityID = buf.readInt();
			this.global = buf.readBoolean();
			try {
				entries = SynchedEntityData.readEntries(new FriendlyByteBuf(buf));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(entityID);
			buf.writeBoolean(global);
			try {
				SynchedEntityData.writeEntries(entries, new FriendlyByteBuf(buf));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static class BulletMessage extends TF2Message {
		// public int shooter;
		public ArrayList<HitResult> target;
		public ArrayList<Object[]> readData;
		public int slot;
		public InteractionHand hand;

		public BulletMessage() {

		}

		public BulletMessage(int slot, ArrayList<HitResult> target, InteractionHand hand) {
			// this.shooter=shooter.getEntityId();
			this.slot = slot;
			this.target = target;
			this.hand = hand;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			slot = buf.readByte();
			this.hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			this.readData = new ArrayList<>();
			while (buf.readableBytes() > 0) {
				Object[] obj = new Object[3];
				obj[0] = buf.readInt();
				// obj[1]=buf.readFloat();
				// obj[2]=buf.readFloat();
				// obj[3]=buf.readFloat();
				obj[1] = buf.readBoolean();
				obj[2] = buf.readFloat();
				this.readData.add(obj);
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			// buf.writeInt(shooter);
			buf.writeByte(this.slot);
			buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
			for (HitResult mop : target) {
				buf.writeInt(mop.getEntity().getId());
				// if(mop.hitVec!=null){
				// buf.writeFloat((float) mop.hitVec.x);
				// buf.writeFloat((float) mop.hitVec.y);
				// buf.writeFloat((float) mop.hitVec.z);
				// }
				// buf.writeInt(mop.entityHit.getEntityId());
				buf.writeBoolean(((float[]) mop.hitInfo)[0] == 1);
				buf.writeFloat(((float[]) mop.hitInfo)[1]);
			}

		}

	}

	public static class BuildingConfigMessage extends TF2Message {
		// public int shooter;
		int entityid;
		BlockPos pos;
		boolean isTile;
		byte id;
		boolean exit;
		boolean grab;
		int value;
		int targetFlags;

		public BuildingConfigMessage() {

		}

		public BuildingConfigMessage(int entityID, byte id, int value) {
			// this.shooter=shooter.getEntityId();
			this.isTile = true;
			this.id = id;
			this.value = value;
			this.entityid = entityID;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			id = buf.readByte();
			entityid = buf.readInt();
			value = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(id);
			buf.writeInt(entityid);
			buf.writeInt(value);
		}

	}

	public static class GuiConfigMessage extends TF2Message {
		// public int shooter;
		private CompoundTag tag;
		int containerid;
		private BlockPos pos;

		public GuiConfigMessage() {

		}

		public GuiConfigMessage(CompoundTag tag, BlockPos pos) {
			this.tag = tag;
			this.pos = pos;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			pos = new FriendlyByteBuf(buf).readBlockPos();
			try {
				tag = new FriendlyByteBuf(buf).readCompoundTag();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			new FriendlyByteBuf(buf).writeBlockPos(pos);
			new FriendlyByteBuf(buf).writeCompoundTag(tag);
		}

		public CompoundTag getTag() {
			return tag;
		}

		public BlockPos getPos() {
			return pos;
		}

	}

	public static class ShowGuiMessage extends TF2Message {
		// public int shooter;

		public int id;
		public CompoundTag data;

		public ShowGuiMessage() {

		}

		public ShowGuiMessage(int id) {
			// this.shooter=shooter.getEntityId();
			this.id = id;
		}

		public ShowGuiMessage(int id, CompoundTag data) {
			// this.shooter=shooter.getEntityId();
			this.id = id;
			this.data = data;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			id = buf.readByte();
			if (buf.readableBytes() > 0) {
				try {
					data = new FriendlyByteBuf(buf).readCompoundTag();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(id);
			if (data != null) {
				new FriendlyByteBuf(buf).writeCompoundTag(data);
			}
		}

	}

	public static class WeaponDataMessage extends TF2Message {

		byte[] bytes;

		public WeaponDataMessage() {}

		public WeaponDataMessage(byte[] bytes) {
			this.bytes = bytes;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			// System.out.println("Read bytes: "+buf.readableBytes());
			this.bytes = new byte[buf.readableBytes()];
			buf.readBytes(this.bytes);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			// System.out.println("Write bytes: "+bytes.length);
			buf.writeBytes(bytes);
		}

	}

	public static class WearableChangeMessage extends TF2Message {
		// public int shooter;

		public int slot;
		public int entityID;
		public ItemStack stack;

		public WearableChangeMessage() {

		}

		public WearableChangeMessage(Entity player, int slot, ItemStack stack) {
			// this.shooter=shooter.getEntityId();
			this.slot = slot;
			this.entityID = player.getId();
			this.stack = stack;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			slot = buf.readByte();
			entityID = buf.readInt();
			try {
				stack = new FriendlyByteBuf(buf).readItemStack();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(slot);
			buf.writeInt(entityID);
			new FriendlyByteBuf(buf).writeItemStack(stack);

		}

	}

	public static class WeaponDroppedMessage extends TF2Message {
		// public int shooter;

		public String name;

		public WeaponDroppedMessage() {

		}

		public WeaponDroppedMessage(String name) {
			// this.shooter=shooter.getEntityId();
			this.name = name;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.name = new FriendlyByteBuf(buf).readString(256);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			new FriendlyByteBuf(buf).writeString(name);

		}

	}

	public static class EffectCooldownMessage extends TF2Message {
		// public int shooter;

		public String name;
		public int time;

		public EffectCooldownMessage() {

		}

		public EffectCooldownMessage(String name, int time) {
			// this.shooter=shooter.getEntityId();
			this.time = time;
			this.name = name;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.name = new FriendlyByteBuf(buf).readString(256);
			this.time = buf.readShort();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			new FriendlyByteBuf(buf).writeString(name);
			buf.writeShort(time);
		}

	}

	public static class ContractMessage extends TF2Message {
		// public int shooter;

		public int id;
		public Contract contract;

		public ContractMessage() {

		}

		public ContractMessage(int id, Contract contract) {
			// this.shooter=shooter.getEntityId();
			this.id = id;
			this.contract = contract;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.id = buf.readByte();
			String name = new FriendlyByteBuf(buf).readString(256);
			int expireDay = buf.readInt();
			int progress = buf.readShort();
			Objective[] objectives = new Objective[buf.readByte()];
			for (int i = 0; i < objectives.length; i++) {
				objectives[i] = Objective.values()[buf.readByte()];
			}
			this.contract = new Contract(name, expireDay, objectives);
			this.contract.progress = progress;
			this.contract.active = buf.readBoolean();
			this.contract.rewards = buf.readByte();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(id);
			new FriendlyByteBuf(buf).writeString(contract.className);
			buf.writeInt(contract.expireDay);
			buf.writeShort(contract.progress);
			buf.writeByte(contract.objectives.length);
			for (Objective obj : contract.objectives) {
				buf.writeByte(obj.ordinal());
			}
			buf.writeBoolean(contract.active);
			buf.writeByte(contract.rewards);

		}

	}

	public static class ParticleSpawnMessage extends TF2Message {
		// public int shooter;

		public float x;
		public float y;
		public float z;
		public float offsetX;
		public float offsetY;
		public float offsetZ;
		public int count;
		public int[] params;
		public int id;

		public ParticleSpawnMessage() {

		}

		public ParticleSpawnMessage(int id, double x, double y, double z, double offsetX, double offsetY,
				double offsetZ, int count, int[] params) {
			super();
			this.id = id;
			this.x = (float) x;
			this.y = (float) y;
			this.z = (float) z;
			this.offsetX = (float) offsetX;
			this.offsetY = (float) offsetY;
			this.offsetZ = (float) offsetZ;
			this.count = count;
			this.params = params;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.id = buf.readByte();
			this.x = buf.readFloat();
			this.y = buf.readFloat();
			this.z = buf.readFloat();
			this.offsetX = buf.readFloat();
			this.offsetY = buf.readFloat();
			this.offsetZ = buf.readFloat();
			this.count = buf.readByte();
			this.params = new int[buf.readableBytes() / 4];
			for (int i = 0; i < this.params.length; i++)
				this.params[i] = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(id);
			buf.writeFloat(x);
			buf.writeFloat(y);
			buf.writeFloat(z);
			buf.writeFloat(offsetX);
			buf.writeFloat(offsetY);
			buf.writeFloat(offsetZ);
			buf.writeByte(count);
			for (int param : params)
				buf.writeInt(param);
		}

	}

	public static class VelocityAddMessage extends TF2Message {
		// public int shooter;

		public float x;
		public float y;
		public float z;
		public boolean airborne;

		public VelocityAddMessage() {

		}

		public VelocityAddMessage(Vec3 vec, boolean airborne) {
			// this.shooter=shooter.getEntityId();
			this.x = (float) vec.x;
			this.y = (float) vec.y;
			this.z = (float) vec.z;
			this.airborne = airborne;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.x = buf.readFloat();
			this.y = buf.readFloat();
			this.z = buf.readFloat();
			this.airborne = buf.readBoolean();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeFloat(x);
			buf.writeFloat(y);
			buf.writeFloat(z);
			buf.writeBoolean(airborne);
		}

	}

	public static class AttackSyncMessage extends TF2Message {
		long time;
		int entity;

		public AttackSyncMessage() {

		}

		public AttackSyncMessage(long value, LivingEntity entity) {
			this.time = value;
			this.entity = entity.getId();
		}

		public AttackSyncMessage(long value) {
			this.time = value;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.time = buf.readLong();
			if (buf.readableBytes() > 0)
				this.entity = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeLong(this.time);
			if (this.entity != 0)
				buf.writeInt(this.entity);
		}
	}

	public static class NetworkedSoundMessage extends TF2Message {
		SoundEvent event;
		int target;
		Vec3 pos;
		float volume;
		float pitch;
		SoundSource category;
		int id;
		boolean repeat;

		public NetworkedSoundMessage() {

		}

		private NetworkedSoundMessage(SoundEvent event, float volume, float pitch, SoundSource category, int id,
				boolean repeat) {
			this.event = event;
			this.volume = volume;
			this.pitch = pitch;
			this.category = category;
			this.id = id;
			this.repeat = true;
		}

		public NetworkedSoundMessage(Entity entity, SoundEvent event, float volume, float pitch, SoundSource category,
				int id, boolean repeat) {
			this(event, volume, pitch, category, id, repeat);
			this.target = entity.getId();
		}

		public NetworkedSoundMessage(Vec3 pos, SoundEvent event, float volume, float pitch, SoundSource category,
				int id, boolean repeat) {
			this(event, volume, pitch, category, id, repeat);
			this.pos = pos;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.event = SoundEvent.REGISTRY.getObjectById(buf.readInt());
			this.volume = buf.readFloat();
			this.pitch = buf.readFloat();
			this.category = SoundSource.values()[buf.readByte()];
			this.id = buf.readShort();
			this.repeat = buf.readBoolean();
			if (buf.readableBytes() > 4)
				this.pos = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
			else
				this.target = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(SoundEvent.REGISTRY.getIDForObject(this.event));
			buf.writeFloat(this.volume);
			buf.writeFloat(this.pitch);
			buf.writeByte(this.category.ordinal());
			buf.writeShort(this.id);
			buf.writeBoolean(this.repeat);
			if (this.pos != null) {
				buf.writeFloat((float) this.pos.x);
				buf.writeFloat((float) this.pos.y);
				buf.writeFloat((float) this.pos.z);
			} else
				buf.writeInt(this.target);
		}
	}

	public static class NetworkedSoundStopMessage extends TF2Message {

		int id;

		public NetworkedSoundStopMessage() {

		}

		public NetworkedSoundStopMessage(int id) {
			this.id = id;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.id = buf.readShort();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeShort(this.id);
		}
	}

	public static class InitMessage extends TF2Message {

		int port;

		int id;

		boolean energyUse;

		Multimap<String, Property> property;

		public InitMessage() {

		}

		public InitMessage(int port, int id, boolean energyUse) {
			this.port = port;
			this.id = id;
			this.energyUse = energyUse;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.port = buf.readUnsignedShort();
			this.id = buf.readShort();
			this.energyUse = buf.readBoolean();
			FriendlyByteBuf packet = new FriendlyByteBuf(buf);
			property = HashMultimap.create();
			while (packet.readableBytes() > 0) {
				property.put(packet.readString(255),
						new Property(packet.readString(255), packet.readString(255), Type.STRING));
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeShort(this.port);
			buf.writeShort(id);
			buf.writeBoolean(this.energyUse);
			FriendlyByteBuf packet = new FriendlyByteBuf(buf);
			for (Entry<ConfigCategory, Property> entry : TF2ConfigVars.propertyNetworked.entries()) {
				packet.writeString(entry.getKey().getName());
				packet.writeString(entry.getValue().getName());
				packet.writeString(entry.getValue().getString());
			}
		}
	}

	public static class InitClientMessage extends TF2Message {

		int sentryTargets;
		boolean dispenserPlayer;
		boolean teleporterPlayer;
		boolean teleporterEntity;
		boolean breakBlocks;

		public InitClientMessage() {

		}

		public InitClientMessage(Configuration conf) {
			ConfigCategory cat = conf.getCategory("default building targets");
			this.sentryTargets = cat.get("Attack on hurt").getBoolean() ? 1 : 0;
			this.sentryTargets += cat.get("Attack other players").getBoolean() ? 2 : 0;
			this.sentryTargets += cat.get("Attack hostile mobs").getBoolean() ? 4 : 0;
			this.sentryTargets += cat.get("Attack friendly creatures").getBoolean() ? 8 : 0;
			this.dispenserPlayer = cat.get("Dispensers heal neutral players").getBoolean();
			this.teleporterPlayer = cat.get("Neutral players can teleport").getBoolean();
			this.teleporterEntity = cat.get("Entities can teleport").getBoolean();
			this.breakBlocks = TF2ConfigVars.swapAttackButton;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.sentryTargets = buf.readByte();
			this.dispenserPlayer = buf.readBoolean();
			this.teleporterPlayer = buf.readBoolean();
			this.teleporterEntity = buf.readBoolean();
			this.breakBlocks = buf.readBoolean();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(this.sentryTargets);
			buf.writeBoolean(this.dispenserPlayer);
			buf.writeBoolean(this.teleporterPlayer);
			buf.writeBoolean(this.teleporterEntity);
			buf.writeBoolean(this.breakBlocks);
		}
	}

	public static class ContractNewMessage extends TF2Message {
		Packet<?> packet;
		int type;

		public ContractNewMessage() {

		}

		public ContractNewMessage(Packet<?> packet) {
			if (packet instanceof SPacketAdvancementInfo)
				type = 0;
			else if (packet instanceof SPacketSelectAdvancementsTab)
				type = 1;
			this.packet = packet;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.type = buf.readByte();
			if (type == 0)
				this.packet = new SPacketAdvancementInfo();
			else
				this.packet = new SPacketSelectAdvancementsTab();
			try {
				this.packet.readPacketData(new FriendlyByteBuf(buf));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(type);
			try {
				this.packet.writePacketData(new FriendlyByteBuf(buf));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
