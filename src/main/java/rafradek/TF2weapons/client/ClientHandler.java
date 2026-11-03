package rafradek.TF2weapons.client;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.gui.GuiTeamSelect;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.message.TF2Message.GuiConfigMessage;
import rafradek.TF2weapons.message.TF2Message.ParticleSpawnMessage;
import rafradek.TF2weapons.message.TF2Message.ShowGuiMessage;
import rafradek.TF2weapons.util.TF2Util;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ClientHandler {

	public static void processParticleSpawnMessage(ParticleSpawnMessage message) {
		IParticleFactory factory = ClientProxy.particleFactories.get(EnumTF2Particles.values()[message.id]);
		for (int i = 0; i < message.count; i++) {
			ParticleSpawnMessage message2 = message;
			Particle particle = factory.createParticle(message.id, Minecraft.getInstance().level, message.x,
					message.y, message.z, message.offsetX, message.offsetY, message.offsetZ, message.params);
			ClientProxy.spawnParticle(Minecraft.getInstance().level, particle);
		}
	}

	public static LivingEntity getClientEntity(int id) {
		return (LivingEntity) Minecraft.getInstance().level.getEntity(id);
	}

	public static void playerDropWeapon(String name) {
		ItemStack stack = ItemFromData.getNewStack(name);
		((ItemUsable) stack.getItem()).holster(
				Minecraft.getInstance().player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null), stack,
				Minecraft.getInstance().player, Minecraft.getInstance().level);
	}

	public static void createConfigGui(GuiConfigMessage message) {
		// The arena block configuration screen used Forge 1.12's config GUI, which no longer exists.
		// It returns with the arena features in a later stage of the port.
		rafradek.TF2weapons.TF2weapons.LOGGER.info("Arena block configuration is not available in this version yet");
	}

	public static void createGui(ShowGuiMessage message) {
		ListTag listTeams = message.data.getList("Teams", 8);
		List<String> teams = TF2Util.NBTTagListToList(listTeams, String.class);
		int[] counts = message.data.getIntArray("Count");
		boolean[] allowed = new boolean[counts.length];
		byte[] allowedbyte = message.data.getByteArray("Allowed");
		for (int i = 0; i < allowed.length; i++) {
			allowed[i] = allowedbyte[i] == 1;
		}
		Minecraft.getInstance().execute(
				() -> Minecraft.getInstance().setScreen(new GuiTeamSelect(teams, counts, allowed)));
	}

}
