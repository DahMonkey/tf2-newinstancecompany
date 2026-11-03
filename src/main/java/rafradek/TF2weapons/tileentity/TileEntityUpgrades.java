package rafradek.TF2weapons.tileentity;

import net.minecraft.util.RandomSource;
import rafradek.TF2weapons.common.TF2Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityUpgrades extends BlockEntity {

	public static final int UPGRADES_COUNT = 10;
	public HashMap<TF2Attribute, Integer> attributes = new HashMap<>();
	public List<TF2Attribute> attributeList = new ArrayList<>();
	private int maxSize;
	public boolean placed;

	public TileEntityUpgrades() {
		super();
	}

	public TileEntityUpgrades(Level world) {
		this.level = world;
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns
	 * them in a new stack.
	 */
	public void generateUpgrades(RandomSource random) {
		// System.out.println("Max Size: "+MapList.nameToAttribute.size());
		List<TF2Attribute> passAttributes = TF2Attribute.getAllPassibleAttributesForUpgradeStation();
		this.maxSize = passAttributes.size();
		int size = (int) (passAttributes.size() * 0.67f);
		for (int i = 0; i < size; i++)
			while (true) {
				TF2Attribute attr = passAttributes.get(random.nextInt(passAttributes.size()));
				if (!this.attributes.containsKey(attr) && attr.weight > random.nextInt(8)) {
					this.attributeList.add(attr);
					boolean high = i % 2 == 0;
					this.attributes.put(attr,
							Math.max(1, Math.round(attr.numLevels * (high ? 1f : 0.5f))/*- (i < 4 ? 0 : 1)*/));
					break;
				}
			}
		// this.world.markAndNotifyBlock(pos,
		// this.world.getChunkFromBlockCoords(getPos()),
		// this.world.getBlockState(getPos()),
		// this.world.getBlockState(pos), 2);
		this.setChanged();
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		this.attributeList.clear();
		this.attributes.clear();
		this.maxSize = compound.getShort("MaxS");
		this.placed = compound.getBoolean("Placed");
		if (compound.contains("Attributes")
				&& maxSize == TF2Attribute.getAllPassibleAttributesForUpgradeStation().size()) {
			CompoundTag attrs = compound.getCompound("Attributes");
			ListTag attrList = (ListTag) compound.get("AttributesList");
			for (String key : attrs.getAllKeys())
				this.attributes.put(TF2Attribute.attributes[Integer.parseInt(key)], attrs.getInt(key));
			for (int i = 0; i < attrList.size(); i++)
				this.attributeList.add(TF2Attribute.attributes[attrList.getInt(i)]);
		}

	}

	@Override
	public CompoundTag saveAdditional(CompoundTag compound) {
		super.writeToNBT(compound);
		CompoundTag attrs = new CompoundTag();
		ListTag attrList = new ListTag();
		compound.putShort("MaxS", (short) this.maxSize);
		compound.put("Attributes", attrs);
		compound.put("AttributesList", attrList);
		compound.putBoolean("Placed", placed);
		for (TF2Attribute attr : this.attributeList)
			if (attr != null) {
				attrList.add(IntTag.valueOf(attr.id));
				attrs.putInt(String.valueOf(attr.id), this.attributes.get(attr));
			}

		return compound;
	}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		// System.out.println("Sending packet");
		return new ClientboundBlockEntityDataPacket(this.pos, 29, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	@Override
	public void onDataPacket(Connection net,
			ClientboundBlockEntityDataPacket pkt) {
		// System.out.println("Received: "+pkt.getNbtCompound());
		this.readFromNBT(pkt.getNbtCompound());
	}
}
