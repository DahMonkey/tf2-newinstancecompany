package rafradek.TF2weapons.arena;

import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemToken;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.tileentity.TileEntityGameConfigure;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import java.util.*;
import java.util.Map.Entry;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

public class GameArena implements INBTSerializable<CompoundTag> {

	private static int ID_INCREMENT;
	private boolean active;
	private AABB bounds = new AABB(0, 0, 0, 0, 0, 0);
	private Level world;
	private String name;
	private BlockPos confPos;
	private boolean first = true;
	public boolean markDelete;
	public Map<UUID, Player> playerUUID = new HashMap<>();
	public Map<UUID, SynchedEntityData> playerInfoUUID = new HashMap<>();
	private int defaultClass = -1;
	private Team defaultTeam = null;
	private boolean showClassSelection = false;
	private boolean showTeamSelection = false;
	private boolean disableHunger = true;
	private boolean forceClassTexture = false;
	public boolean clearInventory = false;
	private List<Team> allowedTeams;
	private int unbalanceLimit = 0;
	private int classLimit = 0;

	public static final EntityDataAccessor<String> PLAYER_NAME = new EntityDataAccessor<>(0, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = new EntityDataAccessor<>(1,
			EntityDataSerializers.OPTIONAL_UUID);
	public static final EntityDataAccessor<Integer> TEAM = new EntityDataAccessor<>(2, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> POINTS = new EntityDataAccessor<>(3, EntityDataSerializers.INT);

	private CompoundTag joinTeamDataCache;
	private CompoundTag joinClassDataCache;
	public int networkId;

	public GameArena(Level world, String name, BlockPos confPos) {
		this.level = world;
		this.name = name;
		this.confPos = confPos;
	}

	public GameArena(Level world, CompoundTag tag) {
		this.level = world;
		this.deserializeNBT(tag);

	}

	public GameArena(Level world, int id) {
		this.level = world;
		this.networkId = id;
	}

	public void addToWorld() {
		this.networkId = ID_INCREMENT++;
	}

	public void tick() {
		if (first) {
			this.allowedTeams = new ArrayList<>();
			this.allowedTeams.add(this.level.getScoreboard().getTeam("RED"));
			this.allowedTeams.add(this.level.getScoreboard().getTeam("BLU"));
			this.first = false;
			if (!(confPos != null && this.level.getBlockEntity(confPos) instanceof TileEntityGameConfigure
					&& ((TileEntityGameConfigure) this.level.getBlockEntity(confPos)).getName().equals(this.name))) {
				this.markDelete = true;
			}
		}
		if (active) {
			this.joinTeamDataCache = null;
			boolean checkAll = this.bounds == null || (this.bounds.maxX == this.bounds.minX
					&& this.bounds.maxY == this.bounds.minY && this.bounds.maxZ == this.bounds.minZ);
			List<Player> players = checkAll ? world.players()
					: world.getEntitiesOfClass(Player.class, bounds);
			for (Player player : players) {
				if (!playerUUID.containsKey(player.getUUID())
						&& (TF2PlayerCapability.get(player).getGameArena() == null
								|| TF2PlayerCapability.get(player).getGameArena() == this)) {
					playerUUID.put(player.getUUID(), player);
					this.onPlayerJoinMatch(player);
				}
			}

			boolean naturalRegen = this.level.getGameRules().getBoolean("naturalRegeneration");

			Iterator<Entry<UUID, Player>> itePly = playerUUID.entrySet().iterator();
			while (itePly.hasNext()) {
				Entry<UUID, Player> entry = itePly.next();
				Player player = entry.getValue();

				if (this.level.getPlayerByUUID(entry.getKey()) != player
						&& this.level.getPlayerByUUID(entry.getKey()) != null) {
					entry.setValue(player = this.level.getPlayerByUUID(entry.getKey()));

				}
				TF2PlayerCapability playercap = TF2PlayerCapability.get(player);
				WeaponsCapability cap = WeaponsCapability.get(player);
				if (this.level.getPlayerByUUID(entry.getKey()) == null
						|| (!checkAll && !player.getBoundingBox().intersects(this.bounds))) {
					itePly.remove();
					removePlayer(entry.getKey(), player, true);
					continue;
				}
				if (player.getTeam() == null && this.showTeamSelection && !player.isSpectator()) {
					player.setGameType(GameType.SPECTATOR);

					this.showJoinTeamDialog(player);
				}
				if (!player.isSpectator() && this.defaultTeam != null && player.getTeam() == null) {
					world.getScoreboard().addPlayerToTeam(player.getName(), this.defaultTeam.getName());
				}

				if (playercap.getRespawnTime() > 0 && this.level.getGameTime() % 20 == 0) {
					playercap.setRespawnTime(playercap.getRespawnTime() - 1);
				}

				if (player.isSpectator() && player.getTeam() != null && playercap.getRespawnTime() <= 0
						&& (cap.getUsedToken() != -1 || (this.defaultClass == -1 && !this.showClassSelection))) {
					player.setGameType(GameType.ADVENTURE);
					this.respawnPlayer(player);
				}
				/*
				 * if (this.allowedTeams.contains(player.getTeam())) {
				 * this.playerTeam.put(player, player.getTeam()); } else {
				 * this.playerTeam.remove(player); }
				 */

				if (this.defaultClass != -1 && !cap.forcedClass) {
					((ItemToken) TF2weapons.itemToken)
							.updateAttributes(TF2Util.metaStack(TF2weapons.itemToken, 1, this.defaultClass), player);
					WeaponsCapability.get(player).forcedClass = true;
				}

				if (this.disableHunger) {
					player.getFoodStats().setFoodLevel(naturalRegen ? 17 : 20);
				}
			}
		}
	}

	public void placePlayerAtSpawn(Player player) {
		BlockPos blockpos = player.getBedLocation(player.dimension);
		if (blockpos != null) {
			BlockPos blockpos1 = Player.getBedSpawnLocation(this.level, blockpos, true);

			if (blockpos1 != null) {
				player.moveTo(blockpos1.getX() + 0.5F, blockpos1.getY() + 0.1F, blockpos1.getZ() + 0.5F,
						0.0F, 0.0F);
			}
		}

		this.level.getChunkProvider().provideChunk((int) player.getX() >> 4, (int) player.getZ() >> 4);

		while (!this.level.getCollisionBoxes(player, player.getBoundingBox()).isEmpty() && player.getY() < 256.0D) {
			player.setPos(player.getX(), player.getY() + 1.0D, player.getZ());
		}
	}

	public void respawnPlayer(Player player) {
		// player.setDead();
		// EntityPlayer newplayer =
		// this.world.getMinecraftServer().getPlayerList().recreatePlayerEntity((EntityPlayerMP)player,
		// player.dimension, false);
		this.placePlayerAtSpawn(player);
		this.resupplyPlayer(player);
	}

	public void resupplyPlayer(Player player) {
		if (this.clearInventory) {
			player.getInventory().clear();
			player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).clear();
		} else {
			TF2Util.removeItemsMatching(new InvWrapper(player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null)), 64,
					stackl -> stackl.hasTag()
							&& stackl.getTag().getBoolean(NBTLiterals.STACK_ARENA_ASSIGNED));
			TF2Util.removeItemsMatching(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), 64,
					stackl -> stackl.hasTag()
							&& stackl.getTag().getBoolean(NBTLiterals.STACK_ARENA_ASSIGNED));
		}
		if (WeaponsCapability.get(player).getUsedToken() != -1) {
			for (int i = 0; i < 5; i++) {
				ItemStack weapon = ItemFromData.getRandomWeaponOfSlotMob(
						TF2Class.getClass(WeaponsCapability.get(player).getUsedToken()), i, player.getRandom(), false,
						0xFFFFFFFF, false);
				if (!weapon.isEmpty()) {
					weapon.getTag().putBoolean(NBTLiterals.STACK_ARENA_ASSIGNED, true);
					ItemHandlerHelper.giveItemToPlayer(player, weapon, i);
				}
			}
		}
		TF2Util.restoreAmmoToWeapons(player, 1f);
	}

	public void onPlayerJoinMatch(Player player) {
		TF2PlayerCapability.get(player).setGameArena(this);
		TF2PlayerCapability.get(player).setForceClassTexture(this.forceClassTexture);
		this.level.getScoreboard().removePlayerFromTeams(player.getName());
		player.setGameType(GameType.SPECTATOR);
		if (this.defaultTeam != null) {
			world.getScoreboard().addPlayerToTeam(player.getName(), this.defaultTeam.getName());
		}
		if (this.showTeamSelection) {
			this.showJoinTeamDialog(player);
		}
	}

	public void tryPlayerJoinTeam(Player player, int number) {
		Team team = this.allowedTeams.get(number % this.allowedTeams.size());
		this.level.getScoreboard().addPlayerToTeam(player.getName(), team.getName());
	}

	public void showJoinTeamDialog(Player player) {
		CompoundTag data = this.joinTeamDataCache;
		if (data == null) {
			data = new CompoundTag();
			ListTag listteamnbt = new ListTag();
			int[] count = new int[allowedTeams.size()];
			byte[] allowed = new byte[allowedTeams.size()];
			int lowestPlayerCount = Integer.MAX_VALUE;

			for (int i = 0; i < this.allowedTeams.size(); i++) {
				Team team = this.allowedTeams.get(i);
				listteamnbt.add(StringTag.valueOf(team.getName()));
				for (Player playerl : this.playerUUID.values()) {
					if (playerl.getTeam() == team) {
						count[i] += 1;
					}
				}
				if (count[i] <= lowestPlayerCount) {
					lowestPlayerCount = count[i];
				}
			}

			for (int i = 0; i < this.allowedTeams.size(); i++) {
				if (count[i] <= lowestPlayerCount + this.unbalanceLimit)
					allowed[i] = 1;
			}
			data.put("Teams", listteamnbt);
			data.putByteArray("Allowed", allowed);
			data.putIntArray("Count", count);
			this.joinTeamDataCache = data;
		}
		TF2weapons.network.sendTo(new TF2Message.ShowGuiMessage(100, data), (ServerPlayer) player);
	}

	public void showJoinClassDialog(Player player) {
		CompoundTag data = this.joinClassDataCache;
		if (data == null) {
			data = new CompoundTag();
			ListTag listteamnbt = new ListTag();
			int[] count = new int[allowedTeams.size()];
			byte[] allowed = new byte[allowedTeams.size()];
			for (int i = 0; i < this.allowedTeams.size(); i++) {
				Team team = this.allowedTeams.get(i);
				listteamnbt.add(StringTag.valueOf(team.getName()));
				for (Player playerl : this.playerUUID.values()) {
					if (playerl.getTeam() == team) {
						count[i] += 1;
					}
				}
				allowed[i] = 1;
			}
			data.put("Teams", listteamnbt);
			data.putByteArray("Allowed", allowed);
			data.putIntArray("Count", count);
			this.joinClassDataCache = data;
		}
		TF2weapons.network.sendTo(new TF2Message.ShowGuiMessage(100, data), (ServerPlayer) player);
	}

	public void removePlayer(UUID uuid, Player player, boolean alreadyRemoved) {
		if (!alreadyRemoved) {
			player = playerUUID.remove(uuid);
		}
		playerInfoUUID.remove(uuid);
		if (player != null) {
			this.level.getScoreboard().removePlayerFromTeams(player.getName());
			// playerTeam.remove(player);
			player.setGameType(this.level.getLevelData().getGameType());
			TF2PlayerCapability.get(player).setGameArena(null);
			TF2PlayerCapability.get(player).setForceClassTexture(false);
			WeaponsCapability cap = WeaponsCapability.get(player);
			if (cap.forcedClass) {
				((ItemToken) TF2weapons.itemToken).updateAttributes(ItemStack.EMPTY, player);
				WeaponsCapability.get(player).forcedClass = false;
			}
		}
	}

	public void setActive(boolean active) {
		this.active = active;
		if (!active) {
			this.playerUUID.entrySet().removeIf((entry) -> {
				removePlayer(entry.getKey(), entry.getValue(), true);
				return true;
			});
		}
	}

	public void onPlayerKill(Player player) {
		player.setGameType(GameType.SPECTATOR);
		TF2PlayerCapability.get(player).setRespawnTime(10);
	}

	public void changePlayerTeam(Player player, Team team) {
		if (this.playerUUID.containsKey(player.getUUID()) && this.allowedTeams.contains(team)
				&& player.getTeam() != team) {

		}
	}

	public void readConfig(CompoundTag tag) {
		if (tag.contains("Min Bounds")) {
			int[] boundsMin = tag.getIntArray("Min Bounds");
			int[] boundsMax = tag.getIntArray("Max Bounds");
			this.bounds = new AABB(boundsMin[0], boundsMin[1], boundsMin[2], boundsMax[0], boundsMax[1],
					boundsMax[2]);
		}
		this.defaultClass = TF2Class.getClass(tag.getString("C:Default Class")).get3DDataValue();
		this.showClassSelection = tag.getBoolean("Show Class Selection");
		this.showTeamSelection = tag.getBoolean("Show Team Selection");
		if (tag.contains("T:Default Team"))
			this.defaultTeam = this.level.getScoreboard().getTeam(tag.getString("T:Default Team"));

		this.disableHunger = tag.getBoolean("Disable Hunger");
		this.forceClassTexture = tag.getBoolean("Force Class Texture");
	}

	public CompoundTag writeConfig(CompoundTag tag) {
		if (bounds != null) {
			tag.putIntArray("Min Bounds", new int[] { (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ });
			tag.putIntArray("Max Bounds", new int[] { (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ });
		}
		tag.putString("C:Default Class", this.defaultClass == -1 ? "none" : TF2Class.getClass(this.defaultClass).getName());
		tag.putBoolean("Show Class Selection", this.showClassSelection);
		if (this.defaultTeam != null)
			tag.putString("T:Default Team", this.defaultTeam.getName());
		else
			tag.putString("T:Default Team", "");
		tag.putBoolean("Show Team Selection", this.showTeamSelection);
		tag.putBoolean("Disable Hunger", this.disableHunger);
		tag.putBoolean("Force Class Texture", this.forceClassTexture);
		return tag;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Name", this.name);
		tag.putLong("ConfPos", this.confPos.toLong());
		tag.putBoolean("Active", this.active);
		this.writeConfig(tag);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.name = nbt.getString("Name");
		this.active = nbt.getBoolean("Active");
		this.readConfig(nbt);
		this.confPos = BlockPos.fromLong(nbt.getLong("ConfPos"));
	}

	public String getName() {
		return name;
	}

	public static class PlayerMatchInfo {

	}
}
