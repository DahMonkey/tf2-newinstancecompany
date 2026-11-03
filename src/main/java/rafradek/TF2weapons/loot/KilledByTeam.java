package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import rafradek.TF2weapons.TF2ConfigVars;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class KilledByTeam implements LootItemCondition {

	public boolean team;

	public KilledByTeam(boolean team) {
		this.team = team;
	}

	@Override
	public boolean testCondition(Random rand, LootContext context) {

		Entity player = this.team ? context.getKiller() : context.getKillerPlayer();
		if (player instanceof OwnableEntity && ((OwnableEntity) player).getOwner() instanceof Player)
			player = ((OwnableEntity) player).getOwner();
		return player != null && (player.getTeam() != null || TF2ConfigVars.neutralAttack)
				&& !player.isAlliedTo(context.getLootedEntity());
	}

	public static class Serializer extends LootItemCondition.Serializer<KilledByTeam> {
		public Serializer() {
			super(new ResourceLocation("killed_by_player_team"), KilledByTeam.class);
		}

		@Override
		public void serialize(JsonObject json, KilledByTeam value, JsonSerializationContext context) {
			json.addProperty("team", value.team);
		}

		@Override
		public KilledByTeam deserialize(JsonObject json, JsonDeserializationContext context) {
			return new KilledByTeam(GsonHelper.getBoolean(json, "team", false));
		}
	}
}
