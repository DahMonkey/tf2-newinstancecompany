package rafradek.TF2weapons.command;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

public class CommandResetStat extends CommandBase {

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "commands.resetbossstat.usage";
	}

	@Override
	public String getName() {
		return "resetbossstat";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {

			ServerPlayer entityplayermp = args.length > 0 ? getPlayer(server, sender, args[0])
					: getCommandSenderAsPlayer(sender);
			entityplayermp.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.clear();
			LevelChunk chunk = entityplayermp.level.getChunkAt(entityplayermp.blockPosition());
			int australium = 0;
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 128; y++) {
					for (int z = 0; z < 16; z++) {
						if (chunk.getBlockState(x, y, z).getBlock() == TF2weapons.blockAustraliumOre)
							australium++;
					}
				}
			}
			notifyCommandListener(sender, this, "commands.resetbossstat.success", entityplayermp.getName());
		} catch (Exception e) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

}
