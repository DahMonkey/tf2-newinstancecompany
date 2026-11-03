package rafradek.TF2weapons.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import rafradek.TF2weapons.TF2weapons;

import java.util.ArrayList;
import java.util.List;

/** The mod's statistics. In 1.19 these are custom stats identified by resource location. */
public class TF2Achievements {

	public static final List<ResourceLocation> ALL = new ArrayList<>();

	public static final ResourceLocation SANDVICH_EATEN = stat("sandvich_eaten");
	public static final ResourceLocation KILLED_WRANGLER_SNIPER = stat("killed_wrangler_sniper");
	public static final ResourceLocation KILLED_SENTRYGUN = stat("killed_sentrygun");
	public static final ResourceLocation TELEPORTED = stat("teleported");
	public static final ResourceLocation KILLED_DOUBLEJUMP = stat("killed_doublejump");
	public static final ResourceLocation KILLED_STUNNED = stat("killed_stunned");
	public static final ResourceLocation FLAREGUN_IGNITED = stat("flaregun_ignited");
	public static final ResourceLocation PROJECTILES_REFLECTED = stat("projectiles_reflected");
	public static final ResourceLocation KILLED_NOSCOPE = stat("killed_noscope");
	public static final ResourceLocation KILLED_BACKSTAB = stat("killed_backstab");
	public static final ResourceLocation KILLED_MERC = stat("killed_mercs");
	public static final ResourceLocation CONTRACT_DAY = stat("contract_day");
	public static final ResourceLocation CRATES_OPENED = stat("crates_opened");
	public static final ResourceLocation ROBOTS_KILLED = stat("robots_killed");

	private static ResourceLocation stat(String name) {
		ResourceLocation id = TF2weapons.id(name);
		ALL.add(id);
		return id;
	}

	/** Called once the stats are registered, so they appear in the statistics screen. */
	public static void init() {
		for (ResourceLocation id : ALL)
			Stats.CUSTOM.get(id, StatFormatter.DEFAULT);
	}

	public static int read(ServerPlayer player, ResourceLocation stat) {
		return player.getStats().getValue(Stats.CUSTOM.get(stat));
	}

	public static void set(ServerPlayer player, ResourceLocation stat, int value) {
		player.getStats().setValue(player, Stats.CUSTOM.get(stat), value);
	}
}
