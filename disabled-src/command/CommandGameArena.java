package rafradek.TF2weapons.command;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.arena.GameArena;
import rafradek.TF2weapons.util.TF2Class;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class CommandGameArena extends CommandBase {

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "commands.gamearena.usage";
	}

	@Override
	public String getName() {
		return "gamearena";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1)
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		try {
			ServerPlayer player = getCommandSenderAsPlayer(sender);
			switch (args[0]) {
			case "add":
				addGameArena(args[1], player.level, sender);
				break;
			case "remove":
				removeGameArena(args[1], player.level, sender);
				break;
			case "list":
				listGameArena(player.level, sender);
				break;
			case "activate":
				activateGameArena(args[1], player.level, sender);
				break;
			case "deactivate":
				deactivateGameArena(args[1], player.level, sender);
				break;
			case "config":
				configGameArena(args[1], args, player.level, sender);
				break;
			}

		} catch (Exception e) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

	public void addGameArena(String name, Level world, ICommandSender sender) throws CommandException {
		if (!world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.containsKey(name)) {
			world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.put(name, new GameArena(world, name, null));
			notifyCommandListener(sender, this, "commands.gamearena.add.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.add.alreadyExists", new Object[] { name });
		}
	}

	public void removeGameArena(String name, Level world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.get(name);
			arena.setActive(false);
			world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.remove(name);
			notifyCommandListener(sender, this, "commands.gamearena.remove.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.remove.notExist", new Object[] { name });
		}
	}

	public void listGameArena(Level world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.isEmpty())
			notifyCommandListener(sender, this, "commands.gamearena.list.empty");
		for (String name : world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.keySet()) {
			notifyCommandListener(sender, this, name);
		}
	}

	public void activateGameArena(String name, Level world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.get(name);
			arena.setActive(true);
			notifyCommandListener(sender, this, "commands.gamearena.activate.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.activate.notExist", new Object[] { name });
		}
	}

	public void deactivateGameArena(String name, Level world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.get(name);
			arena.setActive(false);
			notifyCommandListener(sender, this, "commands.gamearena.deactivate.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.deactivate.notExist", new Object[] { name });
		}
	}

	public void configGameArena(String name, String[] args, Level world, ICommandSender sender)
			throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.get(name);
			CompoundTag configtag = arena.writeConfig(new CompoundTag());
			if (args[2].equals("list")) {
				notifyCommandListener(sender, this, configtag.toString());
				// for (String key : configtag.getKeySet()) {
				// notifyCommandListener(sender, this, key+" =
				// "+configtag.get(key).toString());
				// }
			} else {
				try {
					configtag.merge(TagParser.parseTag(buildString(args, 2)));
				} catch (CommandSyntaxException e) {
					throw new CommandException("commands.gamearena.config.fail", new Object[] { name });
				}
				arena.readConfig(configtag);
				notifyCommandListener(sender, this, "commands.gamearena.config.success", new Object[] { name });
			}
		} else {
			throw new CommandException("commands.gamearena.config.notExist", new Object[] { name });
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, TF2Class.getClassNames());
		} else if (args.length == 2) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		} else {
			return Collections.emptyList();
		}
	}
}
