package rafradek.TF2weapons.tileentity;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemAmmoPackage;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityResupplyCabinet extends BlockEntity implements BlockEntityTicker, IEntityConfigurable {

	private static final String[] OUTPUT_NAMES = { "OnResupply", "OnResupplyLeave" };
	public Team team;
	public Map<LivingEntity, Integer> cooldownUse = new HashMap<>();
	public boolean usedBy;
	public boolean enabled = true;
	public boolean redstoneActivate;
	private EntityOutputManager outputManager = new EntityOutputManager(this);

	public void setEnabled(boolean enable) {

		this.level.addBlockEvent(this.pos, this.getBlockType(), 0, enabled ? 1 : 0);
		if (this.enabled != enable) {
			this.enabled = enable;
			this.level.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
		}

	}

	@Override
	public void update() {
		if (!this.level.isClientSide) {
			int playersold = cooldownUse.size();
			cooldownUse.entrySet().removeIf(entry -> {
				entry.setValue(entry.getValue() - 1);
				return entry.getValue() <= 0;
			});

			if (this.enabled)
				for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
						new AABB(this.pos).inflate(2),
						entityf -> (entityf.isAlive() && (team == null || entityf.getTeam() == team)
								&& !cooldownUse.containsKey(entityf)
								&& entityf.getCapability(TF2weapons.WEAPONS_CAP).isPresent()))) {
					living.setHealth(living.getMaxHealth());
					ArrayList<MobEffect> badEffects = new ArrayList<>();
					for (Entry<MobEffect, MobEffectInstance> entry : living.getActiveEffectsMap().entrySet()) {
						if (entry.getKey().isBadEffect())
							badEffects.add(entry.getKey());
					}
					for (MobEffect potion : badEffects) {
						living.removeEffect(potion);
					}

					if (living instanceof EntityTF2Character) {
						((EntityTF2Character) living).restoreAmmo(1);
					} else if (living instanceof Player) {
						Player player = ((Player) living);
						player.getFoodStats().addStats(20, 20);
						for (int i = 0; i < player.getInventory().getSizeInventory(); i++) {
							ItemStack stack = player.getInventory().getStackInSlot(i);
							if (stack.getItem() instanceof ItemFromData) {
								int ammotype = ((ItemFromData) stack.getItem()).getAmmoType(stack);
								int ammocount = ItemFromData.getAmmoAmountType(player, ammotype);
								if (ammocount < ItemFromData.getData(stack).getInt(PropertyType.MAX_AMMO)) {
									TF2Util.pickAmmo(ItemAmmoPackage.getAmmoForType(ammotype,
											ItemFromData.getData(stack).getInt(PropertyType.MAX_AMMO) - ammocount),
											player, true);
								}
							}
							if (stack.getItem() instanceof ItemWeapon) {
								((ItemWeapon) stack.getItem()).setClip(stack,
										((ItemWeapon) stack.getItem()).getWeaponClipSize(stack, living));
							}
						}
					}
					this.activateOutput("OnResupply");
					cooldownUse.put(living, 50);
					this.level.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
				}
			if (playersold > 0 && cooldownUse.size() == 0) {
				this.activateOutput("OnResupplyLeave");
				this.level.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
			}
		}
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		compound.put("Config", this.getOutputManager().writeConfig(new CompoundTag()));
		compound.putBoolean("Enabled", this.enabled);
		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		this.getOutputManager().readConfig(compound.getCompound("Config"));
		this.enabled = compound.getBoolean("Enabled");
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.enabled = type != 0;
			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}

	/*
	 * @Nullable public SPacketUpdateTileEntity getUpdatePacket() { NBTTagCompound
	 * tag = new NBTTagCompound(); if (this.maxprogress > 0) { tag.setByte("P",
	 * (byte) ((float)this.progress/(float)this.maxprogress*7f)); if (this.progress
	 * > 0) tag.setByte("C", (byte)
	 * ItemToken.getClassID(TF2Util.getWeaponUsedByClass(this.weapon.extractItem(
	 * hasWeapon,64,true)))); } return new SPacketUpdateTileEntity(this.pos, 9999,
	 * tag); }
	 * 
	 * public void onDataPacket(net.minecraft.network.NetworkManager net,
	 * net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
	 * this.progressClient = pkt.getNbtCompound().getByte("P"); this.classType =
	 * pkt.getNbtCompound().getByte("C"); }
	 */

	@Override
	public boolean hasCapability(Capability<?> capability,
			@Nullable Direction facing) {
		if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability,
			@Nullable Direction facing) {
		/*
		 * if (facing != null && capability ==
		 * net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) { if
		 * (facing.getAxis() == Axis.Y) return
		 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.weapon); else if
		 * (facing.getAxis() == Axis.X) return
		 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.parts); else return
		 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.money); } else
		 */
		return super.getCapability(capability, facing);
	}

	@Override
	public void onLoad() {}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return super.shouldRefresh(world, pos, oldState, newSate);
	}

	@Override
	protected void setWorldCreate(Level world) {
		this.setWorld(world);
	}

	@Override
	public void setWorld(Level world) {
		super.setWorld(world);
		this.getOutputManager().level = world;
	}

	@Override
	public CompoundTag writeConfig(CompoundTag tag) {
		tag.put("Outputs", this.getOutputManager().saveOutputs(new CompoundTag()));
		if (team != null)
			tag.putString("T:Team", this.team.getName());
		else
			tag.putString("T:Team", "");
		tag.putBoolean("Redstone Activates", this.redstoneActivate);
		return tag;
	}

	@Override
	public void readConfig(CompoundTag tag) {
		this.getOutputManager().loadOutputs(tag.getCompound("Outputs"));
		this.redstoneActivate = tag.getBoolean("Redstone Activates");
		if (this.hasWorld() && tag.contains("T:Team"))
			this.team = this.level.getScoreboard().getTeam(tag.getString("T:Team"));
	}

	@Override
	public EntityOutputManager getOutputManager() {
		return this.outputManager;
	}

	@Override
	public String[] getOutputs() {
		return OUTPUT_NAMES;
	}

}
