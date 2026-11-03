package rafradek.TF2weapons;

import com.google.common.collect.Multimap;
import rafradek.TF2weapons.arena.GameArena;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.boss.EntityTF2Boss;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.Contract;
import rafradek.TF2weapons.util.Contract.Objective;
import rafradek.TF2weapons.util.PlayerPersistStorage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class TF2PlayerCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {

	public Player owner;
	// public ArrayList<TF2Message.PredictionMessage> predictionList = new
	// ArrayList<TF2Message.PredictionMessage>();
	public boolean pressedStart;
	public int zombieHuntTicks;
	public boolean wornEye;
	public int cratesOpened;
	public float dodgedDmg;
	public int tickAirblasted;

	// public int itProtection;

	public ItemStackHandler lostItems;
	public HashMap<Class<? extends Entity>, Short> highestBossLevel = new HashMap<>();
	public int nextBossTicks;
	public int stickybombKilled;
	public boolean engineerKilled;
	public boolean sentryKilled;
	public boolean dispenserKilled;
	public int[] cachedAmmoCount = new int[ItemAmmo.AMMO_TYPES.length];
	public int sapperTime;
	public int headshotsRow;
	public LivingEntity buildingOwnerKill;
	public Input lastMovementInput;
	public ArrayList<Contract> contracts = new ArrayList<>();
	public int mercenariesKilled;
	public int nextContractDay = -1;
	public boolean newContracts;
	public boolean newRewards;
	public int fastKillTimer;
	public float healed;
	public boolean sendContractsNextTick;
	public LivingEntity lastMedic;

	public short udpServerId;
	public int medicCall;
	public boolean medicCharge;
	public boolean breakBlocks;
	public boolean blockUse;
	public float robotsKilledInvasion;

	public CompoundTag carrying;
	public int carryingType;
	public int maxInvasionBeaten;

	public int hhhSummonedDay;
	public int monoculusSummonedDay;
	public int merasmusSummonedDay;
	public long bossSpawnTicks;
	public EntityTF2Boss bossToSpawn;

	public int lastDayInvasion;
	private float[] lastDamage = new float[20];
	private float totalLastDamage;
	private boolean backpackItemEquipped;

	private GameArena gameArena;
	public SynchedEntityData entityData;
	public SynchedEntityData dataManagerGlobal;
	private static final CompoundTag EMPTY = new CompoundTag();
	public static final EntityDataAccessor<CompoundTag> SENTRY_VIEW = new EntityDataAccessor<>(0,
			EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<CompoundTag> DISPENSER_VIEW = new EntityDataAccessor<>(1,
			EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<CompoundTag> TELEPORTERA_VIEW = new EntityDataAccessor<>(2,
			EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<CompoundTag> TELEPORTERB_VIEW = new EntityDataAccessor<>(3,
			EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<ItemStack> BACKPACK_ITEM_HOLD = new EntityDataAccessor<>(4,
			EntityDataSerializers.ITEM_STACK);
	public static final EntityDataAccessor<Float> INVASION_ATTACK_DIR = new EntityDataAccessor<>(5, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Boolean> USE_CLASS_TEXTURE = new EntityDataAccessor<>(0,
			EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Integer> RESPAWN_TIME = new EntityDataAccessor<>(1, EntityDataSerializers.INT);

	@SuppressWarnings("unchecked")
	public Multimap<String, AttributeModifier>[] wearablesAttrib = new Multimap[5];

	public TF2PlayerCapability(Player entity) {
		this.owner = entity;
		this.lostItems = new ItemStackHandler(27);
		this.nextBossTicks = (int) (entity.level.getDayTime() + entity.getRandom().nextInt(360000));

		this.highestBossLevel.put(EntityHHH.class, (short) 0);
		this.highestBossLevel.put(EntityMonoculus.class, (short) 0);
		this.highestBossLevel.put(EntityMerasmus.class, (short) 0);

		this.entityData = new SynchedEntityData(entity);
		this.dataManagerGlobal = new SynchedEntityData(entity);
		this.entityData.define(SENTRY_VIEW, EMPTY);
		this.entityData.define(DISPENSER_VIEW, EMPTY);
		this.entityData.define(TELEPORTERA_VIEW, EMPTY);
		this.entityData.define(TELEPORTERB_VIEW, EMPTY);
		this.entityData.define(BACKPACK_ITEM_HOLD, ItemStack.EMPTY);
		this.entityData.define(INVASION_ATTACK_DIR, Float.MIN_VALUE);
		this.dataManagerGlobal.register(USE_CLASS_TEXTURE, false);
		this.dataManagerGlobal.register(RESPAWN_TIME, 0);
	}

	public void clone(TF2PlayerCapability cap) {
		for (Entry<Class<? extends Entity>, Short> entry : cap.highestBossLevel.entrySet()) {
			this.highestBossLevel.put(entry.getKey(), entry.getValue());
		}

		this.udpServerId = cap.udpServerId;

		if (this.owner instanceof ServerPlayer && TF2weapons.udpServer != null)
			TF2weapons.udpServer.playerList.put(cap.udpServerId, (ServerPlayer) this.owner);

		this.contracts = cap.contracts;
		this.newContracts = cap.newContracts;
		this.nextContractDay = cap.nextContractDay;
		if (this.owner != null)
			if (this.owner instanceof ServerPlayer) {
				this.sendContractsNextTick = true;
			}
		this.setForceClassTexture(cap.isForceClassTexture());
		this.setRespawnTime(cap.getRespawnTime());
		this.lostItems = cap.lostItems;
		this.gameArena = cap.gameArena;
		this.lastDayInvasion = cap.lastDayInvasion;
		this.hhhSummonedDay = cap.hhhSummonedDay;
		this.merasmusSummonedDay = cap.merasmusSummonedDay;
		this.monoculusSummonedDay = cap.monoculusSummonedDay;
		this.maxInvasionBeaten = cap.maxInvasionBeaten;
	}

	public void tick() {
		if (Float.isNaN(this.owner.getHealth())) {
			this.owner.setHealth(this.owner.getMaxHealth());
			this.owner.setAbsorptionAmount(0);
		}
		// System.out.println("graczin"+state);
		// ItemStack stack = owner.getHeldItem(EnumHand.MAIN_HAND);
		/*
		 * if(itemProperties.get(client).get(player)==null){
		 * itemProperties.get(client).put(player, new NBTTagCompound()); }
		 */
		// player.getEntityData().setTag("TF2", tag);
		this.zombieHuntTicks--;
		this.sapperTime--;

		if (this.fastKillTimer > 0)
			this.fastKillTimer--;
		if (this.dodgedDmg > 0 && this.owner.getEffect(TF2weapons.bonk) == null) {
			this.dodgedDmg = 0;
		}

		if (!this.owner.level.isClientSide) {

			if (this.backpackItemEquipped) {
				ItemStack backpack = ItemBackpack.getBackpack(owner);
				if (backpack.getItem() instanceof ItemBackpack
						&& (this.owner.getInventory().selected == 0 || this.owner.getInventory().selected == 8)) {
					ItemStack toUse = ((ItemBackpack) backpack.getItem()).getBackpackItemToUse(backpack, owner);
					if (!toUse.sameItem(this.owner.getMainHandItem())) {
						if (!this.owner.getMainHandItem().isEmpty()) {
							this.owner.dropItem(this.getBackpackItemHold(), false);
							this.setBackpackItemHold(this.owner.getMainHandItem());
						}
						this.owner.setItemInHand(InteractionHand.MAIN_HAND, toUse.copy());
					}

				} else {
					this.setEquipBackpackItem(false);
				}
			}

			if (this.owner.tickCount % 2 == 0)
				this.updateBuildings();

			if (this.owner.tickCount % 20 == 0) {
				this.totalLastDamage = 0;
				for (int i = 19; i >= 0; i--)
					if (i > 0) {
						lastDamage[i] = lastDamage[i - 1];
						totalLastDamage += lastDamage[i];
					} else {
						lastDamage[0] = 0;
					}

				if (medicCharge)
					this.medicCharge = false;

				Iterator<BlockPos> it = PlayerPersistStorage.get(this.owner).lostMercPos.iterator();
				while (it.hasNext()) {
					BlockPos pos = it.next();
					ArrayList<EntityTF2Character> list = new ArrayList<>();
					this.owner.level.getChunkFromBlockCoords(pos).getEntitiesOfTypeWithinAABB(EntityTF2Character.class,
							new AABB(pos), list, test -> {
								return test.getOrder() == Order.FOLLOW && owner.getUUID().equals(test.getOwnerUUID());
							});
					boolean success = false;
					for (EntityTF2Character living : list) {
						success = TF2Util.teleportSafe(living, owner);
					}
					;
					if (success || list.isEmpty())
						it.remove();

				}
			}
			this.medicCall--;

			if (this.sendContractsNextTick)
				for (int i = 0; i < this.contracts.size(); i++) {
					TF2weapons.network.sendTo(new TF2Message.ContractMessage(i, this.contracts.get(i)),
							(ServerPlayer) this.owner);
				}

			if (this.bossSpawnTicks != 0 && this.bossToSpawn != null
					&& this.bossSpawnTicks < this.owner.level.getDayTime()) {
				if (this.owner.level.getDayTime() % 24000 < 13000) {
					this.bossToSpawn = null;
					this.bossSpawnTicks = 0;

				} else {
					BlockPos spawnPos = null;
					int i = 0;
					do {
						i++;
						spawnPos = this.owner.level.getTopSolidOrLiquidBlock(this.owner.blockPosition()
								.add(this.owner.getRandom().nextInt(48) - 24, 0, this.owner.getRandom().nextInt(48) - 24));
						this.bossToSpawn.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
					} while (i < 2 && !this.owner.level.getCollisionBoxes(null, this.bossToSpawn.getBoundingBox())
							.isEmpty());

					if (spawnPos != null) {
						this.bossToSpawn.onInitialSpawn(
								this.owner.level.getCurrentDifficultyAt(this.bossToSpawn.blockPosition()), null);
						this.owner.level.addFreshEntity(this.bossToSpawn);
						this.bossToSpawn = null;
						this.bossSpawnTicks = 0;
					}
				}
			}

			int contractDay;
			if (!PlayerPersistStorage.get(this.owner).itemsToGive.isEmpty()) {
				Component text = Component.translatable("gui.robotinvasion.reward");
				text.getStyle().setColor(ChatFormatting.GOLD);
				this.owner.sendSystemMessage(text);
				for (ItemStack stack : PlayerPersistStorage.get(this.owner).itemsToGive) {
					this.owner.sendSystemMessage(Component.literal(stack.getHoverName()));
					if (stack.getItem() instanceof ItemMoney)
						ItemMoney.collect(stack, owner);
					ItemHandlerHelper.giveItemToPlayer(owner, stack);
				}
				PlayerPersistStorage.get(this.owner).itemsToGive.clear();

			}
			if (!TF2ConfigVars.disableContracts && this.contracts.size() < 2
					&& (contractDay = ((ServerPlayer) this.owner).getStatFile()
							.readStat(TF2Achievements.CONTRACT_DAY)) != 0
					&& this.owner.level.getDayTime() % 24000 > 1000
					&& this.owner.level.getDayTime() / 24000 >= contractDay) {
				String name = "kill";
				do {
					switch (this.owner.getRandom().nextInt(10)) {
					case 0:
						name = "scout";
						break;
					case 1:
						name = "pyro";
						break;
					case 2:
						name = "soldier";
						break;
					case 3:
						name = "demoman";
						break;
					case 4:
						name = "heavy";
						break;
					case 5:
						name = "engineer";
						break;
					case 6:
						name = "medic";
						break;
					case 7:
						name = "sniper";
						break;
					case 8:
						name = "spy";
						break;
					default:
						name = "kill";
					}
				} while (this.contracts.size() > 0 && this.contracts.get(0).className.equals(name));
				((ServerPlayer) this.owner).getStatFile().unlockAchievement(this.owner, TF2Achievements.CONTRACT_DAY,
						(int) (this.owner.level.getDayTime() / 24000 + 7));
				Contract contract = new Contract(name, 0, this.owner.getRandom());
				this.contracts.add(contract);
				// ((EntityPlayerMP)this.owner).sendMessage(new TextComponentString("f"));
				TF2weapons.network.sendTo(new TF2Message.ContractMessage(-1, contract), (ServerPlayer) this.owner);
			}
			if (this.entityData.isDirty()) {
				TF2weapons.network.sendTo(new TF2Message.PlayerCapabilityMessage(this.owner, false, false),
						(ServerPlayer) this.owner);
			}
			if (this.dataManagerGlobal.isDirty()) {
				TF2Util.sendTracking(new TF2Message.PlayerCapabilityMessage(this.owner, false, true), this.owner);
			}
		} else if (this.owner == Minecraft.getInstance().player) {
			((LocalPlayer) this.owner).connection.send(new CPacketInput(owner.xxa, owner.zza,
					((LocalPlayer) this.owner).movementInput.jump,
					((LocalPlayer) this.owner).movementInput.sneak));
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return TF2weapons.PLAYER_CAP != null && capability == TF2weapons.PLAYER_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
		if (TF2weapons.PLAYER_CAP != null && capability == TF2weapons.PLAYER_CAP)
			return TF2weapons.PLAYER_CAP.cast(this);
		return null;
	}

	public void completeObjective(Objective objective, ItemStack stack) {
		int i = 0;
		for (Contract contract : this.contracts) {
			if (contract.active)
				for (Objective objective2 : contract.objectives) {
					if (objective2 == objective && (contract.className.equals("kill")
							|| ItemFromData.getData(stack).get(PropertyType.SLOT).containsKey(contract.className))) {
						int oldProgress = contract.progress;
						contract.progress += objective.getPoints();

						if (oldProgress < Contract.REWARD_LOW && contract.progress >= Contract.REWARD_LOW) {
							contract.rewards += 1;
						}
						if (oldProgress < Contract.REWARD_HIGH && contract.progress >= Contract.REWARD_HIGH) {
							contract.rewards += 2;
						}
						if (contract.progress > 135)
							contract.progress = 135;

						((ServerPlayer) this.owner).sendSystemMessage(Component.translatable("gui.contracts.progress",
								contract.className, contract.progress));
						TF2weapons.network.sendTo(new TF2Message.ContractMessage(i, contract),
								(ServerPlayer) this.owner);
						break;
					}
				}
			i++;
		}
	}

	public ItemStack getBackpackItemHold() {
		return this.entityData.get(BACKPACK_ITEM_HOLD);
	}

	public void setBackpackItemHold(ItemStack stack) {
		this.entityData.set(BACKPACK_ITEM_HOLD, stack);
	}

	public float getInvasionDir() {
		return this.entityData.get(INVASION_ATTACK_DIR);
	}

	public void setInvasionDir(float dir) {
		this.entityData.set(INVASION_ATTACK_DIR, dir);
	}

	public boolean isForceClassTexture() {
		return this.dataManagerGlobal.get(USE_CLASS_TEXTURE);
	}

	public void setForceClassTexture(boolean force) {
		this.dataManagerGlobal.set(USE_CLASS_TEXTURE, force);
	}

	public int getRespawnTime() {
		return this.dataManagerGlobal.get(RESPAWN_TIME);
	}

	public void setRespawnTime(int time) {
		this.dataManagerGlobal.set(RESPAWN_TIME, time);
	}

	public CompoundTag getSentryView() {
		return this.entityData.get(SENTRY_VIEW);
	}

	public CompoundTag getDispenserView() {
		return this.entityData.get(DISPENSER_VIEW);
	}

	public CompoundTag getTeleporterAView() {
		return this.entityData.get(TELEPORTERA_VIEW);
	}

	public CompoundTag getTeleporterBView() {
		return this.entityData.get(TELEPORTERB_VIEW);
	}

	public void updateBuildings() {
		PlayerPersistStorage storage = PlayerPersistStorage.get((this.owner));
		if (storage.buildings[0] != null) {
			this.entityData.set(SENTRY_VIEW, storage.buildings[0].getSecond());
			this.entityData.setDirty(SENTRY_VIEW);
		} else
			this.entityData.set(SENTRY_VIEW, EMPTY);

		if (storage.buildings[1] != null) {
			this.entityData.set(DISPENSER_VIEW, storage.buildings[1].getSecond());
			this.entityData.setDirty(DISPENSER_VIEW);
		} else
			this.entityData.set(DISPENSER_VIEW, EMPTY);

		if (storage.buildings[2] != null) {
			this.entityData.set(TELEPORTERA_VIEW, storage.buildings[2].getSecond());
			this.entityData.setDirty(TELEPORTERA_VIEW);
		} else
			this.entityData.set(TELEPORTERA_VIEW, EMPTY);

		if (storage.buildings[3] != null) {
			this.entityData.set(TELEPORTERB_VIEW, storage.buildings[3].getSecond());
			this.entityData.setDirty(TELEPORTERB_VIEW);
		} else
			this.entityData.set(TELEPORTERB_VIEW, EMPTY);
		/*
		 * if (storage.buildings[1] != null)
		 * stack.getTagCompound().setTag("DispenserView",
		 * storage.buildings[1].getSecond()); else
		 * stack.getTagCompound().removeTag("DispenserView"); if (storage.buildings[2]
		 * != null) stack.getTagCompound().setTag("TeleporterAView",
		 * storage.buildings[2].getSecond()); else
		 * stack.getTagCompound().removeTag("TeleporterAView"); if (storage.buildings[3]
		 * != null) stack.getTagCompound().setTag("TeleporterBView",
		 * storage.buildings[3].getSecond()); else
		 * stack.getTagCompound().removeTag("TeleporterBView");
		 */
	}

	public void addLastDamage(float damage, boolean isPlayerTarget) {
		if (!isPlayerTarget) {
			damage *= 0.5f;
			if (this.getTotalLastDamage() + damage > 60) {
				damage = 60 - this.getTotalLastDamage();
			}
		}
		this.lastDamage[0] += damage;
	}

	public float getTotalLastDamage() {
		return this.totalLastDamage + this.lastDamage[0];
	}

	public int calculateMaxSentries() {
		ItemStack wrench = TF2Util.getBestItem(this.owner.inventory, (stack1, stack2) -> {
			float sentries1 = TF2Attribute.getModifier("Sentry Bonus", stack1, 1, this.owner);
			float sentries2 = TF2Attribute.getModifier("Sentry Bonus", stack2, 1, this.owner);
			return sentries1 > sentries2 ? 1 : sentries1 == sentries2 ? 0 : -1;
		}, stack -> stack.getItem() instanceof ItemWrench);
		ItemStack pda = TF2Util.getBestItem(this.owner.inventory, (stack1, stack2) -> {
			float sentries1 = TF2Attribute.getModifier("Extra Sentry", stack1, 0, this.owner);
			float sentries2 = TF2Attribute.getModifier("Extra Sentry", stack2, 0, this.owner);
			return sentries1 > sentries2 ? 1 : sentries1 == sentries2 ? 0 : -1;
		}, stack -> stack.getItem() instanceof ItemPDA);
		return (int) (TF2Attribute.getModifier("Sentry Bonus", wrench, 1, this.owner)
				* TF2Attribute.getModifier("Extra Sentry", pda, 0, this.owner));
	}

	public void setEquipBackpackItem(boolean equip) {
		ItemStack backpack = ItemBackpack.getBackpack(this.owner);
		if (backpack.isEmpty() || (this.owner.getInventory().selected != 0 && this.owner.getInventory().selected != 8))
			equip = false;

		if (!this.owner.level.isClientSide) {
			ItemStack toUse = ItemStack.EMPTY;
			if (!backpack.isEmpty())
				toUse = ((ItemBackpack) backpack.getItem()).getBackpackItemToUse(backpack, owner);
			if (toUse.isEmpty())
				equip = false;
			if (equip) {
				if (!toUse.sameItem(this.owner.getMainHandItem())) {
					this.owner.dropItem(this.getBackpackItemHold(), false);
					this.setBackpackItemHold(this.owner.getMainHandItem());
				}
				this.owner.setItemInHand(InteractionHand.MAIN_HAND, toUse.copy());
			} else {
				if (toUse.sameItem(this.owner.getMainHandItem())) {

					this.owner.setItemInHand(InteractionHand.MAIN_HAND, this.getBackpackItemHold());
				} else {
					this.owner.dropItem(this.getBackpackItemHold(), false);

				}
				this.setBackpackItemHold(ItemStack.EMPTY);
			}
			if (this.owner instanceof ServerPlayer)
				TF2weapons.network.sendTo(new TF2Message.ActionMessage(equip ? 31 : 32, this.owner),
						(ServerPlayer) this.owner);
		}
		this.backpackItemEquipped = equip;
	}

	public boolean isBackpackItemEquipped() {
		return this.backpackItemEquipped;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		// tag.setBoolean("Uber",
		// this.owner.getDataManager().get(TF2EventBusListener.ENTITY_UBER));
		CompoundTag bossInfo = new CompoundTag();
		for (Entry<Class<? extends Entity>, Short> entry : this.highestBossLevel.entrySet())
			bossInfo.putShort(EntityList.getKey(entry.getKey()).toString(), entry.getValue());
		tag.put("BossInfo", bossInfo);
		tag.putInt("NextBossTick", this.nextBossTicks);
		tag.putFloat("DodgedDmg", this.dodgedDmg);
		ListTag list = new ListTag();
		tag.put("Contracts", list);
		for (Contract contract : this.contracts) {
			CompoundTag com = new CompoundTag();
			com.putBoolean("Active", contract.active);
			com.putShort("Progress", (short) contract.progress);
			com.putByte("Rewards", (byte) contract.rewards);
			byte[] objs = new byte[contract.objectives.length];
			for (int i = 0; i < contract.objectives.length; i++) {
				objs[i] = (byte) contract.objectives[i].ordinal();
			}
			com.putByteArray("Objectives", objs);
			com.putString("Name", contract.className);
			list.add(com);
		}
		tag.putInt("NextContractDay", this.nextContractDay);
		if (this.carrying != null) {
			tag.put("Carrying", this.carrying);
			tag.putByte("CarryingType", (byte) this.carryingType);
		}
		tag.putFloat("RobotsKilled", (short) this.robotsKilledInvasion);
		tag.putByte("MaxInvasionBeaten", (byte) this.maxInvasionBeaten);
		tag.putLong("BossSpawnTime", this.bossSpawnTicks);
		tag.putBoolean("HHHSummonedDay", this.hhhSummonedDay == this.owner.level.getDayTime() / 24000);
		tag.putBoolean("MonoculusSummonedDay", this.monoculusSummonedDay == this.owner.level.getDayTime() / 24000);
		tag.putBoolean("MerasmusSummonedDay", this.merasmusSummonedDay == this.owner.level.getDayTime() / 24000);
		if (this.bossToSpawn != null) {
			CompoundTag bosstag = new CompoundTag();
			this.bossToSpawn.writeToNBTOptional(bosstag);
			tag.put("BossSpawn", bosstag);
		}
		tag.putInt("LastDayInvasion", this.lastDayInvasion);
		tag.putBoolean("BackpackEquip", this.backpackItemEquipped);
		tag.put("BackpackEquipItem", this.getBackpackItemHold().serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		CompoundTag bossInfo = nbt.getCompound("BossInfo");
		for (String key : bossInfo.getAllKeys())
			this.highestBossLevel.put(EntityList.getClass(new ResourceLocation(key)), bossInfo.getShort(key));
		this.nextBossTicks = nbt.getInt("NextBossTick");
		this.dodgedDmg = nbt.getFloat("DodgedDmg");
		ListTag list = (ListTag) nbt.getTag("Contracts");
		this.contracts.clear();
		if (list != null)
			for (int i = 0; i < list.size() && i < 16; i++) {
				CompoundTag com = list.getCompound(i);
				byte[] objsb = com.getByteArray("Objectives");
				Objective[] objs = new Objective[objsb.length];
				for (int j = 0; j < objsb.length; j++) {
					objs[j] = Objective.values()[objsb[j]];
				}
				Contract contract = new Contract(com.getString("Name"), 0, objs);
				contract.active = com.getBoolean("Active");
				contract.progress = com.getShort("Progress");
				contract.rewards = com.getByte("Rewards");
				this.contracts.add(contract);
				// TF2weapons.network.sendTo(new TF2Message.ContractMessage(i, contract),
				// (EntityPlayerMP) this.owner);
			}
		this.nextContractDay = nbt.getInt("NextContractDay");
		this.carrying = (CompoundTag) nbt.getTag("Carrying");
		this.carryingType = nbt.getByte("CarryingType");
		this.robotsKilledInvasion = nbt.getFloat("RobotsKilled");
		this.maxInvasionBeaten = nbt.getByte("MaxInvasionBeaten");
		this.bossSpawnTicks = nbt.getLong("BossSpawnTime");
		if (nbt.getBoolean("HHHSummonedDay"))
			this.hhhSummonedDay = (int) (this.owner.level.getDayTime() / 24000);
		if (nbt.getBoolean("MonoculusSummonedDay"))
			this.monoculusSummonedDay = (int) (this.owner.level.getDayTime() / 24000);
		if (nbt.getBoolean("MerasmusSummonedDay"))
			this.merasmusSummonedDay = (int) (this.owner.level.getDayTime() / 24000);
		Entity ent = EntityList.createEntityFromNBT(nbt.getCompound("BossSpawn"), this.owner.level);
		if (ent instanceof EntityTF2Boss)
			this.bossToSpawn = (EntityTF2Boss) ent;
		this.lastDayInvasion = nbt.getInt("LastDayInvasion");
		this.backpackItemEquipped = nbt.getBoolean("BackpackEquip");
		this.setBackpackItemHold(new ItemStack(nbt.getCompound("BackpackEquipItem")));
	}

	public static TF2PlayerCapability get(Player player) {
		return player.getCapability(TF2weapons.PLAYER_CAP).orElse(null);
	}

	public void onChangeValue(EntityDataAccessor<?> key, Object value) {

	}

	public GameArena getGameArena() {
		return gameArena;
	}

	public void setGameArena(GameArena gameArena) {
		this.gameArena = gameArena;
	}
}
