package rafradek.TF2weapons.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;

public class TileEntityUpgrades extends BlockEntity {

	public static final int UPGRADES_COUNT = 10;
	public HashMap<TF2Attribute, Integer> attributes = new HashMap<>();
	public List<TF2Attribute> attributeList = new ArrayList<>();
	private int maxSize;
	public boolean placed;

	public TileEntityUpgrades(BlockPos pos, BlockState state) {
		super(TF2weapons.TILE_UPGRADES, pos, state);
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
	public void load(CompoundTag compound) {
		super.load(compound);
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
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
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
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (this.level != null && !this.level.isClientSide)
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
	}
}
