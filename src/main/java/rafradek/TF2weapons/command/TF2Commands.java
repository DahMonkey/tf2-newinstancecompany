package rafradek.TF2weapons.command;

import java.util.Map.Entry;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.InvasionEvent;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemToken;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

/**
 * The mod's server commands, rewritten for Brigadier: giveweapon, forceclass, invasion, resetbossstat
 * and resetitemdata. The 1.12 cmodc, debugcl, generatereferences and gamearena commands are not ported.
 */
public class TF2Commands {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("giveweapon").requires(s -> s.hasPermission(2))
				.then(Commands.argument("weapon", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(MapList.nameToData.keySet(), b))
						.executes(c -> giveWeapon(c, c.getSource().getPlayerOrException(), ""))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(c -> giveWeapon(c, EntityArgument.getPlayer(c, "player"), ""))
								.then(Commands.argument("attributes", StringArgumentType.greedyString())
										.executes(c -> giveWeapon(c, EntityArgument.getPlayer(c, "player"),
												StringArgumentType.getString(c, "attributes")))))));

		dispatcher.register(Commands.literal("forceclass").requires(s -> s.hasPermission(2))
				.then(Commands.argument("class", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(
								java.util.stream.Stream.concat(java.util.Arrays.stream(TF2Class.getClassNames()),
										java.util.stream.Stream.of("clear")),
								b))
						.executes(c -> forceClass(c, c.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(c -> forceClass(c, EntityArgument.getPlayer(c, "player"))))));

		dispatcher.register(Commands.literal("invasion").requires(s -> s.hasPermission(2))
				.then(Commands.literal("stop").executes(c -> {
					for (Entry<UUID, InvasionEvent> entry : c.getSource().getLevel()
							.getCapability(TF2weapons.WORLD_CAP).orElse(null).invasions.entrySet())
						entry.getValue().finish();
					return 1;
				}))
				.then(Commands.argument("difficulty", IntegerArgumentType.integer(1, InvasionEvent.DIFFICULTY.length))
						.executes(c -> invasion(c, new BlockPos(c.getSource().getPosition())))
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(c -> invasion(c, BlockPosArgument.getLoadedBlockPos(c, "pos"))))));

		dispatcher.register(Commands.literal("resetbossstat").requires(s -> s.hasPermission(2))
				.executes(c -> resetBossStat(c, c.getSource().getPlayerOrException()))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(c -> resetBossStat(c, EntityArgument.getPlayer(c, "player")))));

		dispatcher.register(Commands.literal("resetitemdata").requires(s -> s.hasPermission(2)).executes(c -> {
			TF2weapons.loadWeapons();
			if (c.getSource().getServer().isDedicatedServer())
				TF2weapons.network.sendToAll(new TF2Message.WeaponDataMessage(TF2weapons.itemDataCompressed));
			c.getSource().sendSuccess(Component.translatable("commands.resetitemdata.success"), true);
			return 1;
		}));
	}

	private static int giveWeapon(CommandContext<CommandSourceStack> c, ServerPlayer player, String attributes)
			throws CommandSyntaxException {
		String name = StringArgumentType.getString(c, "weapon");
		boolean giveNew = !name.startsWith("-");
		ItemStack item = giveNew ? ItemFromData.getNewStack(name) : player.getMainHandItem();
		if (item.isEmpty() || !item.hasTag()) {
			c.getSource().sendFailure(Component.translatable("commands.giveweapon.usage"));
			return 0;
		}
		boolean remove = name.equals("-r");
		CompoundTag attributeTag = item.getTag().getCompound("Attributes");
		item.getTag().put("Attributes", attributeTag);
		try {
			for (String arg : attributes.trim().split("\\s+")) {
				if (arg.isEmpty())
					continue;
				String[] attr = arg.split(":");
				if (attr.length == 2) {
					boolean streak = attr[1].startsWith("k");
					String id = null;
					if (attr[0].equals("u")) {
						item.getTag().putByte("UEffect", remove ? 0 : Byte.parseByte(attr[1]));
						continue;
					} else if (MapList.nameToAttribute.containsKey(attr[0]))
						id = Integer.toString(MapList.nameToAttribute.get(attr[0]).id);
					else if (TF2Attribute.attributes[Integer.parseInt(attr[0])] != null)
						id = attr[0];
					if (id == null)
						continue;
					if (remove)
						attributeTag.remove(id);
					else if (streak) {
						item.getTag().putByte(rafradek.TF2weapons.NBTLiterals.STREAK_LEVEL, Byte.parseByte(attr[1].substring(1)));
						item.getTag().putShort(rafradek.TF2weapons.NBTLiterals.STREAK_ATTRIB, Short.parseShort(id));
					} else
						attributeTag.putFloat(id, Float.parseFloat(attr[1]));
				} else if (attr[0].equals("a"))
					item.getTag().putBoolean("Australium", !remove);
				else if (attr[0].equals("s"))
					item.getTag().putBoolean("Strange", !remove);
				else if (attr[0].equals("v"))
					item.getTag().putBoolean("Valve", !remove);
			}
		} catch (RuntimeException e) {
			c.getSource().sendFailure(Component.translatable("commands.giveweapon.usage"));
			return 0;
		}
		if (giveNew)
			ItemHandlerHelper.giveItemToPlayer(player, item);
		else
			item.getCapability(TF2weapons.WEAPONS_DATA_CAP).ifPresent(cap -> cap.cached = false);
		return 1;
	}

	private static int forceClass(CommandContext<CommandSourceStack> c, ServerPlayer player) {
		String name = StringArgumentType.getString(c, "class");
		if (name.equals("clear")) {
			((ItemToken) TF2weapons.itemToken).updateAttributes(ItemStack.EMPTY, player);
			WeaponsCapability.get(player).forcedClass = false;
			c.getSource().sendSuccess(Component.translatable("commands.forceclass.success2", player.getName()), true);
		} else {
			TF2Class clazz = TF2Class.getClass(name);
			if (clazz == null) {
				c.getSource().sendFailure(Component.translatable("commands.forceclass.usage"));
				return 0;
			}
			((ItemToken) TF2weapons.itemToken).updateAttributes(TF2Util.metaStack(TF2weapons.itemToken, 1, clazz.getIndex()),
					player);
			WeaponsCapability.get(player).forcedClass = true;
			c.getSource().sendSuccess(Component.translatable("commands.forceclass.success", player.getName(),
					clazz.getLocalizedName()), true);
		}
		return 1;
	}

	private static int invasion(CommandContext<CommandSourceStack> c, BlockPos pos) {
		int difficulty = IntegerArgumentType.getInteger(c, "difficulty") - 1;
		ServerPlayer player = c.getSource().getPlayer();
		if (player == null)
			player = c.getSource().getServer().getPlayerList().getPlayers().stream().findFirst().orElse(null);
		if (player == null)
			return 0;
		var storage = c.getSource().getLevel().getCapability(TF2weapons.WORLD_CAP).orElse(null);
		InvasionEvent old = storage.invasions.get(player.getUUID());
		if (old != null)
			old.finish();
		storage.invasions.put(player.getUUID(), new InvasionEvent(c.getSource().getLevel(), pos, difficulty));
		return 1;
	}

	private static int resetBossStat(CommandContext<CommandSourceStack> c, ServerPlayer player) {
		player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.clear();
		c.getSource().sendSuccess(Component.translatable("commands.resetbossstat.success", player.getName()), true);
		return 1;
	}
}
