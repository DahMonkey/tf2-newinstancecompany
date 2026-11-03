package rafradek.TF2weapons.util;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerPersistStorage implements INBTSerializable<CompoundTag> {

	public final UUID uuid;
	public HashSet<BlockPos> lostMercPos = new HashSet<>();
	public HashSet<BlockPos> medicMercPos = new HashSet<>();
	public HashSet<BlockPos> restMercPos = new HashSet<>();
	@SuppressWarnings("unchecked")
	public Tuple<UUID, CompoundTag>[] buildings = new Tuple[4];
	public ArrayList<UUID> disposableBuildings = new ArrayList<>(5);
	public NonNullList<ItemStack> itemsToGive = NonNullList.create();
	public boolean save = false;

	public PlayerPersistStorage(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();

		ListTag list = new ListTag();
		for (BlockPos pos : lostMercPos) {
			list.add(new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
		}

		ListTag list2 = new ListTag();
		for (BlockPos pos : medicMercPos) {
			list.add(new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
		}

		ListTag list3 = new ListTag();
		for (BlockPos pos : restMercPos) {
			list.add(new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
		}

		tag.put("MercsLost", list);
		tag.put("MercsMedic", list2);
		tag.put("MercsRest", list3);

		if (buildings[0] != null) {
			tag.putUUID("SentryUUID", buildings[0].getFirst());
			tag.put("Sentry", buildings[0].getSecond());
		}

		if (buildings[1] != null) {
			tag.putUUID("DispenserUUID", buildings[1].getFirst());
			tag.put("Dispenser", buildings[1].getSecond());
		}

		if (buildings[2] != null) {
			tag.putUUID("TeleporterAUUID", buildings[2].getFirst());
			tag.put("TeleporterA", buildings[2].getSecond());
		}

		if (buildings[3] != null) {
			tag.putUUID("TeleporterBUUID", buildings[3].getFirst());
			tag.put("TeleporterB", buildings[3].getSecond());
		}
		tag.putByte("DispBuildingsCount", (byte) disposableBuildings.size());
		for (int i = 0; i < disposableBuildings.size(); i++) {
			tag.putUUID("SentryAdd" + i + "UUID", disposableBuildings.get(i));
			// tag.setTag("SentryAdd"+i, disposableBuildings.get(i).getSecond());
		}
		ContainerHelper.saveAllItems(tag, itemsToGive);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.save = true;
		for (String key : nbt.getCompound("MercsLost").getAllKeys()) {
			ListTag list = nbt.getList(key, 11);
			for (int i = 0; i < list.size(); i++) {
				int[] pos = list.getIntArrayAt(i);
				lostMercPos.add(new BlockPos(pos[0], pos[1], pos[2]));
			}
		}
		for (String key : nbt.getCompound("MercsMedic").getAllKeys()) {
			ListTag list = nbt.getList(key, 11);
			for (int i = 0; i < list.size(); i++) {
				int[] pos = list.getIntArrayAt(i);
				medicMercPos.add(new BlockPos(pos[0], pos[1], pos[2]));
			}
		}
		for (String key : nbt.getCompound("MercsRest").getAllKeys()) {
			ListTag list = nbt.getList(key, 11);
			for (int i = 0; i < list.size(); i++) {
				int[] pos = list.getIntArrayAt(i);
				restMercPos.add(new BlockPos(pos[0], pos[1], pos[2]));
			}
		}

		if (nbt.hasUUID("SentryUUID"))
			this.buildings[0] = new Tuple<>(nbt.getUUID("SentryUUID"), nbt.getCompound("Sentry"));

		if (nbt.hasUUID("DispenserUUID"))
			this.buildings[1] = new Tuple<>(nbt.getUUID("DispenserUUID"), nbt.getCompound("Dispenser"));

		if (nbt.hasUUID("TeleporterAUUID"))
			this.buildings[2] = new Tuple<>(nbt.getUUID("TeleporterAUUID"), nbt.getCompound("TeleporterA"));

		if (nbt.hasUUID("TeleporterBUUID"))
			this.buildings[3] = new Tuple<>(nbt.getUUID("TeleporterBUUID"), nbt.getCompound("TeleporterB"));

		int count = nbt.getByte("DispBuildingsCount");
		for (int i = 0; i < count; i++) {
			this.disposableBuildings.add(nbt.getUUID("SentryAdd" + i + "UUID"));
		}

		ContainerHelper.loadAllItems(nbt, this.itemsToGive);
	}

	public void setSave() {
		this.save = true;
	}

	public void setBuilding(EntityBuilding building, int maxsentries) {
		CompoundTag tag = new CompoundTag();
		building.addAdditionalSaveData(tag);
		if (building.getDisposableID() != -1) {
			this.disposableBuildings.add(building.getUUID());
			if (this.disposableBuildings.size() > maxsentries) {
				this.disposableBuildings.remove(0);
			}
		} else {
			if (building instanceof EntitySentry)
				this.buildings[0] = new Tuple<>(building.getUUID(), tag);
			else if (building instanceof EntityDispenser)
				this.buildings[1] = new Tuple<>(building.getUUID(), tag);
			else if (building instanceof EntityTeleporter && ((EntityTeleporter) building).isExit())
				this.buildings[3] = new Tuple<>(building.getUUID(), tag);
			else
				this.buildings[2] = new Tuple<>(building.getUUID(), tag);
		}
		this.setSave();
	}

	public boolean hasBuilding(int slot) {
		return slot != 4 && this.buildings[slot] != null;
	}

	public boolean allowBuilding(EntityBuilding building) {
		if (building.getDisposableID() == -1)
			return this.buildings[building.getBuildingID()] == null
					|| this.buildings[building.getBuildingID()].getFirst().equals(building.getUUID());
		else
			return this.disposableBuildings.contains(building.getUUID());
	}

	public static PlayerPersistStorage get(Player player) {
		return get(player.level, player.getUUID());
	}

	public static PlayerPersistStorage get(Level world, UUID uuid) {
		return world.getCapability(TF2weapons.WORLD_CAP).orElse(null).getPlayerStorage(uuid);
	}
}
