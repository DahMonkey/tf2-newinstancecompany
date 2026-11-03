package rafradek.TF2weapons.entity.building;

import net.minecraft.resources.ResourceKey;
import rafradek.TF2weapons.client.compat.LegacyGui;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Util;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;

public class EntityTeleporter extends EntityBuilding {
	// public static ArrayList<BlockPosDimension> teleportersData=new
	// ArrayList<BlockPosDimension>();
	public static final int TP_PER_PLAYER = 128;

	public static int tpCount = 0;
	public static HashMap<UUID, TeleporterData[]> teleporters = new HashMap<>();

	public int tpID = -1;
	public int ticksToTeleport;

	public EntityTeleporter linkedTp;

	public float spin;
	public float spinRender;

	public long timestamp;

	private static final EntityDataAccessor<Integer> TELEPORTS = SynchedEntityData.defineId(EntityTeleporter.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> TPPROGRESS = SynchedEntityData.defineId(EntityTeleporter.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Byte> CHANNEL = SynchedEntityData.defineId(EntityTeleporter.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> EXIT = SynchedEntityData.defineId(EntityTeleporter.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(EntityTeleporter.class,
			EntityDataSerializers.BYTE);

	public EntityTeleporter(Level world) {
		this(TF2weapons.TELEPORTER, world);
	}

	public EntityTeleporter(EntityType<? extends EntityTeleporter> type, Level world) {
		super(type, world);
		this.setSize(1f, 0.2f);
	}

	/** 1.12 getCollisionBox(entity): called for entities standing on the teleporter; sends them to the exit. */
	private void tryTeleport(Entity entityIn) {
		if (!this.level.isClientSide && !this.isExit() && this.getTPprogress() <= 0 && entityIn != null
				&& entityIn instanceof LivingEntity && !(entityIn instanceof EntityBuilding)
				&& ((this.getOwner() != null && ((WeaponsCapability.get(this.getOwner()).teleporterEntity
						&& !(entityIn instanceof Player))
						|| (WeaponsCapability.get(this.getOwner()).teleporterPlayer && entityIn instanceof Player
								&& entityIn.getTeam() == null)))
						|| TF2Util.isAlliedTo(EntityTeleporter.this, entityIn))
				&& entityIn.getBoundingBox()
						.intersects(this.getBoundingBox().inflate(0, 0.5, 0).move(0, 0.5D, 0)))
			if (ticksToTeleport <= 0)
				if (ticksToTeleport < 0)
					ticksToTeleport = 10;
				else {
					TeleporterData exit = this.getTeleportExit();
					if (exit != null && this.consumeEnergy(this.getMinEnergy())) {
						if (exit.dimension != this.level.dimension()) {
							ServerLevel destworld = this.level.getServer().getLevel(exit.dimension);
							if (destworld != null && ForgeHooks.onTravelToDimension(entityIn, exit.dimension)) {
								if (entityIn instanceof ServerPlayer)
									((ServerPlayer) entityIn).teleportTo(destworld, exit.getX() + 0.5,
											exit.getY() + 0.23, exit.getZ() + 0.5, entityIn.getYRot(), entityIn.getXRot());
								else {
									Entity moved = entityIn.changeDimension(destworld, new TeleporterDim(exit));
									if (moved != null)
										entityIn = moved;
								}
							}
						}
						entityIn.teleportTo(exit.getX() + 0.5, exit.getY() + 0.23, exit.getZ() + 0.5);
						this.setTeleports(this.getTeleports() + 1);
						this.setTPprogress(this.getBuildingLevel() == 1 ? 200 : (this.getBuildingLevel() == 2 ? 100 : 60));
						this.playSound(TF2Sounds.MOB_TELEPORTER_SEND, 1.5f, 1f);
						entityIn.playSound(TF2Sounds.MOB_TELEPORTER_RECEIVE, 0.75f, 1f);
						if (this.getOwner() instanceof ServerPlayer) {
							((Player) this.getOwner()).awardStat(TF2Achievements.TELEPORTED);
							/*
							 * if(((EntityPlayerMP)
							 * this.getOwner()).getStatFile().readStat(TF2Achievements.TELEPORTED)>=100)
							 * ((EntityPlayer) this.getOwner()).awardStat(TF2Achievements.TELEPORTS);
							 */
						}
					}
				}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			if (this.tpID == -1) {
				tpCount++;
				this.tpID = tpCount;
			}
			if (!this.isExit()) {
				ticksToTeleport--;
				if (this.getTPprogress() > 0)
					this.setTPprogress(this.getTPprogress() - 1);
				if (this.getSoundState() == 1 && (this.getTPprogress() > 0 || this.getTeleportExit() == null)) {
					this.setSoundState(0);
					if (this.linkedTp != null)
						this.linkedTp.setSoundState(0);

				}
				if (this.getSoundState() == 0 && this.getTPprogress() <= 0 && this.getTeleportExit() != null) {
					this.setSoundState(1);
					if (this.linkedTp != null)
						this.linkedTp.setSoundState(1);
				}
				if (this.getTPprogress() <= 0)
					for (LivingEntity standing : this.level.getEntitiesOfClass(LivingEntity.class,
							this.getBoundingBox().inflate(0, 0.5, 0).move(0, 0.5D, 0))) {
						if (this.getTPprogress() > 0)
							break;
						this.tryTeleport(standing);
					}
				if (this.linkedTp != null && this.getColor() != this.linkedTp.getColor()) {
					this.getEntityData().set(COLOR, (byte) this.linkedTp.getColor());
				}
				/*
				 * if(ticksToTeleport<=0){ List<EntityLivingBase>
				 * targetList=this.world.getEntitiesWithinAABB( EntityLivingBase.class,
				 * this.getEntityBoundingBox().inflate(0, 0.5, 0).offset(0, 0.5D, 0), new
				 * Predicate<EntityLivingBase>(){
				 *
				 * @Override public boolean apply(EntityLivingBase input) {
				 *
				 * return !(input instanceof
				 * EntityBuilding)&&EntityTeleporter.this!=input&&((TF2weapons.
				 * dispenserHeal&&input instanceof EntityPlayer && getTeam()==null &&
				 * input.getTeam() ==
				 * null)||TF2weapons.isOnSameTeam(EntityTeleporter.this,input)); }
				 *
				 * });
				 *
				 * if(!targetList.isEmpty()){ if(ticksToTeleport<0){ ticksToTeleport=10; } else{
				 * BlockPosDimension exit=this.getTeleportExit(); if(exit !=null){
				 * if(exit.dimension!=this.dimension)
				 * targetList.get(0).travelToDimension(exit.dimension);
				 * targetList.get(0).setPositionAndUpdate(exit.getX()+0.5, exit.getY()+0.23,
				 * exit.getZ()+0.5); } } } }
				 */
			} else if (teleporters.get(this.getOwnerUUID())[this.getID()] == null
					|| teleporters.get(this.getOwnerUUID())[this.getID()].id != this.tpID) {
				this.setExit(false);
				this.updateTeleportersData(true);
			} else if (teleporters.get(this.getOwnerUUID())[this.getID()].id == this.tpID
					&& !this.blockPosition().equals(teleporters.get(this.getOwnerUUID())[this.getID()]))
				this.updateTeleportersData(false);
		} else if (this.getSoundState() == 1)
			this.spin += (float) Math.PI * (this.getBuildingLevel() == 1 ? 0.25f : (this.getBuildingLevel() == 2 ? 0.325f : 0.4f));
		else
			this.spin = 0;
	}

	public TeleporterData getTeleportExit() {
		UUID uuid = this.getOwnerUUID();
		if (this.getOwnerUUID() != null && teleporters.get(uuid) != null) {
			final TeleporterData data = teleporters.get(uuid)[this.getID()];
			List<EntityTeleporter> list = this.level.getEntitiesOfClass(EntityTeleporter.class, this.getBoundingBox().inflate(256), input -> data != null && data.id == input.tpID);
			if (!list.isEmpty())
				this.linkedTp = list.get(0);
			// System.out.println("linkedtpset");
			return data;
		}
		return null;
	}

	@Override
	public SoundEvent getSoundNameForState(int state) {
		switch (state) {
		case 0:
			return null;
		case 1:
			return this.getBuildingLevel() == 1 ? TF2Sounds.MOB_TELEPORTER_SPIN_1
					: (this.getBuildingLevel() == 2 ? TF2Sounds.MOB_TELEPORTER_SPIN_2 : TF2Sounds.MOB_TELEPORTER_SPIN_3);
		default:
			return super.getSoundNameForState(state);
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {

		if (player == this.getOwner() && player.getItemInHand(hand).getItem() instanceof DyeItem) {
			if (!this.level.isClientSide) {
				this.setColor(player.getItemInHand(hand).getDamageValue());
			}
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
		if (player == this.getOwner() && hand == InteractionHand.MAIN_HAND) {
			if (!this.level.isClientSide)
				if (TF2ConfigVars.disableBuildingGui)
					this.grab();
				else
					TF2weapons.openGui((net.minecraft.world.entity.player.Player) player, 5, this.level, this.getId(), 0, 0);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(TELEPORTS, 0);
		this.entityData.define(TPPROGRESS, 0);
		this.entityData.define(EXIT, false);
		this.entityData.define(CHANNEL, (byte) 0);
		this.entityData.define(COLOR, (byte) -1);
	}

	public boolean isExit() {
		return this.entityData.get(EXIT);
	}

	public void setExit(boolean exit) {
		// if(this.getOwner()!=null&&exit&&teleporters.get(UUID.fromString(this.getOwnerUUID()))[this.getID()]!=null)
		// return;
		if (this.tpID == -1) {
			tpCount++;
			this.tpID = tpCount;
		}
		this.entityData.set(EXIT, exit);
		if (this.getOwnerUUID() != null)
			this.updateTeleportersData(false);
	}

	public int getID() {
		return this.entityData.get(CHANNEL);
	}

	public void setID(int id) {
		if ((id >= TP_PER_PLAYER || id < 0)/*
											 * ||teleporters.get(UUID.fromString( this.getOwnerUUID()))[id]!=null
											 */)
			return;
		if (this.getOwnerUUID() != null)
			this.updateTeleportersData(true);
		this.entityData.set(CHANNEL, (byte) id);
		;
		if (this.getOwnerUUID() != null)
			this.updateTeleportersData(false);
	}

	public int getTPprogress() {
		return this.isDisabled() ? 20 : this.entityData.get(TPPROGRESS);
	}

	public void setTPprogress(int progress) {
		this.entityData.set(TPPROGRESS, progress);
	}

	public void setTeleports(int amount) {
		this.entityData.set(TELEPORTS, amount);
	}

	public int getTeleports() {
		return this.entityData.get(TELEPORTS);
	}

	public void setColor(int color) {
		this.entityData.set(COLOR, (byte) color);
		if (this.linkedTp != null && !this.isExit()) {
			this.linkedTp.setColor(color);
		}
	}

	public int getColor() {
		return this.entityData.get(COLOR);
	}

	/*
	 * public void setOwner(EntityLivingBase owner) { super.setOwner(owner);
	 * if(owner instanceof EntityPlayer){ this.dataManager.set(key, value);14,
	 * owner.getUniqueID().toString()); } }
	 */
	@Override
	public void upgrade() {
		super.upgrade();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	public void remove(net.minecraft.world.entity.Entity.RemovalReason removalReason) {
		// Chunk
		// chunk=this.world.getChunkFromBlockCoords(this.getPosition());
		// if(chunk.isLoaded()){
		if (!this.level.isClientSide)
			this.updateTeleportersData(true);
		// }
		// System.out.println("teleporter removed: "+chunk.isLoaded()+"
		// "+chunk.isEmpty()+" "+chunk.isPopulated());
		super.remove(removalReason);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (!this.level.isClientSide)
			if (OWNER_UUID.equals(key)) {
				UUID id = this.getOwnerUUID();
				if (!teleporters.containsKey(id))
					teleporters.put(id, new TeleporterData[TP_PER_PLAYER]);
			}
	}

	public void updateTeleportersData(boolean forceremove) {
		if (this.level.isClientSide)
			return;

		UUID id = this.getOwnerUUID();
		if (!forceremove && this.isExit()/* &&teleporters.get(id)[this.getID()]==null */)
			teleporters.get(id)[this.getID()] = new TeleporterData(this.blockPosition(), this.tpID, this.level.dimension());
		else if (teleporters.get(id)[this.getID()] != null && teleporters.get(id)[this.getID()].id == this.tpID) {
			// new Exception().printStackTrace();
			teleporters.get(id)[this.getID()] = null;
		}
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_TELEPORTER_DEATH;
	}

	@Override
	public float getCollHeight() {
		return 0.2f;
	}

	@Override
	public float getCollWidth() {
		return 1f;
	}

	@Override
	public int getIronDrop() {
		return 1 + this.getBuildingLevel() / 2;
	}

	@Override
	public boolean shouldUseBlocks() {
		return TF2ConfigVars.teleporterUseEnergy >= 0 && super.shouldUseBlocks();
	}

	@Override
	public int getMinEnergy() {
		return this.getOwnerUUID() != null ? TF2ConfigVars.teleporterUseEnergy : 0;
	}

	@Override
	public int getBuildingID() {
		return 2 + (this.isExit() ? 1 : 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.addAdditionalSaveData(par1NBTTagCompound);

		par1NBTTagCompound.putByte("TeleExitID", (byte) this.getID());
		par1NBTTagCompound.putBoolean("TeleExit", this.isExit());
		par1NBTTagCompound.putInt("TeleID", this.tpID);
		par1NBTTagCompound.putShort("Teleports", (short) this.getTeleports());
		par1NBTTagCompound.putByte("Color", (byte) this.getColor());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag par1NBTTagCompound) {

		super.readAdditionalSaveData(par1NBTTagCompound);
		this.tpID = par1NBTTagCompound.getInt("TeleID");
		this.setTeleports(par1NBTTagCompound.getShort("Teleports"));
		this.getEntityData().set(CHANNEL, par1NBTTagCompound.getByte("TeleExitID"));
		if (!this.level.isClientSide && ((teleporters.get(this.getOwnerUUID())[this.getID()] == null
				|| teleporters.get(this.getOwnerUUID())[this.getID()].id == this.tpID)))
			// this.setID(par1NBTTagCompound.getByte("TeleExitID"));
			this.setExit(par1NBTTagCompound.getBoolean("TeleExit"));
		if (this.level.isClientSide)
			this.getEntityData().set(EXIT, par1NBTTagCompound.getBoolean("TeleExit"));
		this.getEntityData().set(COLOR, par1NBTTagCompound.getByte("Color"));

	}

	@Override
	@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
	public void renderGUI(BufferBuilder renderer, Tessellator tessellator, Player player, int width, int height,
			GuiComponent gui) {
		// GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		// LegacyGui.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64,
		// event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
		ClientProxy.setColor(TF2Util.getTeamColor(this), 0.7f, 0, 0.25f, 0.8f);
		LegacyGui.drawTexturedModalRect(20, 2, 0, 112, 124, 44);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		LegacyGui.drawTexturedModalRect(0, 0, 0, 0, 144, 48);
		/*
		 * renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 72,
		 * event.getResolution().getScaledHeight() / 2 + 76, 0.0D).tex(0.0D,
		 * 0.1875D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 72, event.getResolution().getScaledHeight() / 2 + 76, 0.0D).tex(0.5625D,
		 * 0.1875D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 72, event.getResolution().getScaledHeight() / 2 + 28, 0.0D).tex(0.5625D,
		 * 0D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2 -
		 * 72, event.getResolution().getScaledHeight() / 2 + 28, 0.0D).tex(0.0D,
		 * 0D).endVertex(); tessellator.draw();
		 */

		double imagePos = this.isExit() ? 0.1875D : 0;
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(19, 48, 0.0D).tex(0.5625D + imagePos, 0.9375D).endVertex();
		renderer.pos(69, 48, 0.0D).tex(0.75D + imagePos, 0.9375D).endVertex();
		renderer.pos(69, 0, 0.0D).tex(0.75D + imagePos, 0.75D).endVertex();
		renderer.pos(19, 0, 0.0D).tex(0.5625D + imagePos, 0.75D).endVertex();
		tessellator.draw();

		if (!this.isAlive())
			return;

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(67, 22, 0.0D).tex(0.9375D, 0.1875D).endVertex();
		renderer.pos(83, 22, 0.0D).tex(1D, 0.1875D).endVertex();
		renderer.pos(83, 6, 0.0D).tex(1D, 0.125D).endVertex();
		renderer.pos(67, 6, 0.0D).tex(0.9375D, 0.125D).endVertex();
		tessellator.draw();

		imagePos = this.getBuildingLevel() == 1 ? 0.3125D : this.getBuildingLevel() == 2 ? 0.375D : 0.4375D;
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(50, 18, 0.0D).tex(0.9375D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 18, 0.0D).tex(1D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 2, 0.0D).tex(1D, imagePos).endVertex();
		renderer.pos(50, 2, 0.0D).tex(0.9375D, imagePos).endVertex();
		tessellator.draw();

		if (this.getBuildingLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(67, 42, 0.0D).tex(0.9375D, 0.125D).endVertex();
			renderer.pos(83, 42, 0.0D).tex(1D, 0.125D).endVertex();
			renderer.pos(83, 26, 0.0D).tex(1D, 0.0625).endVertex();
			renderer.pos(67, 26, 0.0D).tex(0.9375D, 0.0625).endVertex();
			tessellator.draw();
		}
		if (this.getTPprogress() <= 0) {
			LegacyGui.drawString(Minecraft.getInstance().font, this.getTeleports() + " (ID: " + (this.getID() + 1) + ")", 85, 10,
					16777215);
		}

		float health = this.getHealth() / this.getMaxHealth();
		if (health > 0.33f) {
			GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
		} else {
			GlStateManager.color(0.85F, 0.0F, 0.0F, 1F);
		}
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		for (int i = 0; i < health * 8; i++) {

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(19, 39 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 39 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 43 - i * 5, 0.0D).endVertex();
			renderer.pos(19, 43 - i * 5, 0.0D).endVertex();
			tessellator.draw();
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F);
		if (this.getTPprogress() > 0) {

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 21, 0.0D).endVertex();
			renderer.pos(140, 21, 0.0D).endVertex();
			renderer.pos(140, 7, 0.0D).endVertex();
			renderer.pos(85, 7, 0.0D).endVertex();
			tessellator.draw();
		}

		if (this.getBuildingLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 41, 0.0D).endVertex();
			renderer.pos(140, 41, 0.0D).endVertex();
			renderer.pos(140, 27, 0.0D).endVertex();
			renderer.pos(85, 27, 0.0D).endVertex();
			tessellator.draw();
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
		if (this.getTPprogress() > 0) {
			double tpProgress = (1 - ((double) this.getTPprogress()
					/ (this.getBuildingLevel() == 1 ? 200 : (this.getBuildingLevel() == 2 ? 100 : 60)))) * 55;

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 21, 0.0D).endVertex();
			renderer.pos(85 + tpProgress, 21, 0.0D).endVertex();
			renderer.pos(85 + tpProgress, 7, 0.0D).endVertex();
			renderer.pos(85, 7, 0.0D).endVertex();
			tessellator.draw();
		}
		if (this.getBuildingLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 41, 0.0D).endVertex();
			renderer.pos(85 + this.getProgress() * 0.275D, 41, 0.0D).endVertex();
			renderer.pos(85 + this.getProgress() * 0.275D, 27, 0.0D).endVertex();
			renderer.pos(85, 27, 0.0D).endVertex();
			tessellator.draw();
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		// LegacyGui.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64,
		// event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);

		/*
		 * renderer.begin(7, DefaultVertexFormats.POSITION_TEX); renderer.pos(69,
		 * event.getResolution().getScaledHeight() / 2 + 50, 0.0D).tex(0.9375D,
		 * 0.3125D).endVertex(); renderer.pos(83,
		 * event.getResolution().getScaledHeight() / 2 + 50, 0.0D).tex(1D,
		 * 0.3125D).endVertex(); renderer.pos(83,
		 * event.getResolution().getScaledHeight() / 2 + 34, 0.0D).tex(1D,
		 * 0.25D).endVertex(); renderer.pos(69, event.getResolution().getScaledHeight()
		 * / 2 + 34, 0.0D).tex(0.9375D, 0.25D).endVertex(); tessellator.draw();
		 * 
		 * imagePos = teleporter.getBuildingLevel() == 1 ? 0.3125D : teleporter.getBuildingLevel() == 2
		 * ? 0.375D : 0.4375D; renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 22,
		 * event.getResolution().getScaledHeight() / 2 + 46, 0.0D).tex(0.9375D, 0.0625D
		 * + imagePos).endVertex(); renderer.pos(event.getResolution().getScaledWidth()
		 * / 2 - 6, event.getResolution().getScaledHeight() / 2 + 46, 0.0D).tex(1D,
		 * 0.0625D + imagePos).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 6,
		 * event.getResolution().getScaledHeight() / 2 + 30, 0.0D).tex(1D,
		 * imagePos).endVertex(); renderer.pos(event.getResolution().getScaledWidth() /
		 * 2 - 22, event.getResolution().getScaledHeight() / 2 + 30, 0.0D).tex(0.9375D,
		 * imagePos).endVertex(); tessellator.draw();
		 * 
		 * if (teleporter.getBuildingLevel() < 3) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION_TEX);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 5,
		 * event.getResolution().getScaledHeight() / 2 + 70, 0.0D).tex(0.9375D,
		 * 0.125D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 11, event.getResolution().getScaledHeight() / 2 + 70, 0.0D).tex(1D,
		 * 0.125D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 11, event.getResolution().getScaledHeight() / 2 + 54, 0.0D).tex(1D,
		 * 0.0625).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * - 5, event.getResolution().getScaledHeight() / 2 + 54, 0.0D).tex(0.9375D,
		 * 0.0625).endVertex(); tessellator.draw(); } if (teleporter.getTPprogress() <=
		 * 0) { LegacyGui.drawString(Minecraft.getInstance().font, teleporter.getTeleports() +
		 * " (ID: " + (teleporter.getID() + 1) + ")",
		 * event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 38, 16777215); } float health =
		 * teleporter.getHealth() / teleporter.getMaxHealth(); if (health > 0.33f) {
		 * GlStateManager.color(0.9F, 0.9F, 0.9F, 1F); } else {
		 * GlStateManager.color(0.85F, 0.0F, 0.0F, 1F); }
		 * GL11.glDisable(GL11.GL_TEXTURE_2D); for (int i = 0; i < health * 8; i++) {
		 * 
		 * renderer.begin(7, DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 53,
		 * event.getResolution().getScaledHeight() / 2 + 67 - i * 5, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 63,
		 * event.getResolution().getScaledHeight() / 2 + 67 - i * 5, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 63,
		 * event.getResolution().getScaledHeight() / 2 + 71 - i * 5, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 53,
		 * event.getResolution().getScaledHeight() / 2 + 71 - i * 5, 0.0D).endVertex();
		 * tessellator.draw(); } GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F); if
		 * (teleporter.getTPprogress() > 0) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * tessellator.draw(); } if (teleporter.getBuildingLevel() < 3) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 69, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 69, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 55, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 55, 0.0D).endVertex();
		 * tessellator.draw(); } GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F); if
		 * (teleporter.getTPprogress() > 0) { double tpProgress = (1 - ((double)
		 * teleporter.getTPprogress() / (teleporter.getBuildingLevel() == 1 ? 200 :
		 * (teleporter.getBuildingLevel() == 2 ? 100 : 60)))) * 55; renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 + tpProgress,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 + tpProgress,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * tessellator.draw(); } if (teleporter.getBuildingLevel() < 3) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 69, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 +
		 * teleporter.getProgress() * 0.275D, event.getResolution().getScaledHeight() /
		 * 2 + 69, 0.0D) .endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 +
		 * teleporter.getProgress() * 0.275D, event.getResolution().getScaledHeight() /
		 * 2 + 55, 0.0D) .endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 55, 0.0D).endVertex();
		 * tessellator.draw(); }
		 */
	}

	public static class TeleporterData extends BlockPos {

		public final ResourceKey<Level> dimension;
		public final int id;

		public TeleporterData(BlockPos blockPos, int id, ResourceKey<Level> dimension) {
			super(blockPos);
			this.id = id;
			this.dimension = dimension;
		}
	}

	/** Moves non-player entities between dimensions to the exit teleporter. */
	public static class TeleporterDim implements net.minecraftforge.common.util.ITeleporter {

		public final BlockPos target;

		public TeleporterDim(BlockPos targetPos) {
			this.target = targetPos;
		}

		@Override
		public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw,
				java.util.function.Function<Boolean, Entity> repositionEntity) {
			Entity moved = repositionEntity.apply(false);
			moved.moveTo(this.target.getX() + 0.5, this.target.getY() + 0.23, this.target.getZ() + 0.5, yaw,
					moved.getXRot());
			return moved;
		}
	}

	private static java.nio.file.Path teleporterFile(net.minecraft.server.MinecraftServer server) {
		return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("teleports.dat");
	}

	public static void loadTeleporters(net.minecraft.server.MinecraftServer server) {
		teleporters.clear();
		tpCount = 0;
		java.io.File input = teleporterFile(server).toFile();
		if (!input.exists())
			return;
		try {
			CompoundTag tagRoot = net.minecraft.nbt.NbtIo.readCompressed(input);
			CompoundTag tag = tagRoot.getCompound("Teleporters");
			for (String key : tag.getAllKeys()) {
				TeleporterData[] blockArray = new TeleporterData[TP_PER_PLAYER];
				teleporters.put(UUID.fromString(key), blockArray);
				CompoundTag exitTag = tag.getCompound(key);
				for (int i = 0; i < TP_PER_PLAYER; i++)
					if (exitTag.contains(Integer.toString(i))) {
						CompoundTag entry = exitTag.getCompound(Integer.toString(i));
						blockArray[i] = new TeleporterData(
								new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z")), entry.getInt("Id"),
								ResourceKey.create(net.minecraft.core.Registry.DIMENSION_REGISTRY,
										new net.minecraft.resources.ResourceLocation(entry.getString("Dim"))));
					}
			}
			tpCount = tagRoot.getInt("TPCount");
		} catch (Exception e) {
			TF2weapons.LOGGER.warn("Reading teleporter data skipped", e);
		}
	}

	public static void saveTeleporters(net.minecraft.server.MinecraftServer server) {
		CompoundTag tagRoot = new CompoundTag();
		CompoundTag tag = new CompoundTag();
		tagRoot.put("Teleporters", tag);
		for (java.util.Map.Entry<UUID, TeleporterData[]> entry : teleporters.entrySet()) {
			CompoundTag exitTag = new CompoundTag();
			for (int i = 0; i < TP_PER_PLAYER; i++) {
				TeleporterData data = entry.getValue()[i];
				if (data != null) {
					CompoundTag dataTag = new CompoundTag();
					dataTag.putInt("X", data.getX());
					dataTag.putInt("Y", data.getY());
					dataTag.putInt("Z", data.getZ());
					dataTag.putInt("Id", data.id);
					dataTag.putString("Dim", data.dimension.location().toString());
					exitTag.put(Integer.toString(i), dataTag);
				}
			}
			tag.put(entry.getKey().toString(), exitTag);
		}
		tagRoot.putInt("TPCount", tpCount);
		try {
			net.minecraft.nbt.NbtIo.writeCompressed(tagRoot, teleporterFile(server).toFile());
		} catch (java.io.IOException e) {
			TF2weapons.LOGGER.error("Could not save teleporter data", e);
		}
		teleporters.clear();
		tpCount = 0;
	}
}
