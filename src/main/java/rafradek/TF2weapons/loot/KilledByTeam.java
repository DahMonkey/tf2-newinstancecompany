package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;

/** True when killed by an enemy-team player (or their mercenary when "team" is set). */
public class KilledByTeam implements LootItemCondition {

	public final boolean team;

	public KilledByTeam(boolean team) {
		this.team = team;
	}

	@Override
	public boolean test(LootContext context) {
		Entity player = this.team ? context.getParamOrNull(LootContextParams.KILLER_ENTITY)
				: context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
		if (player instanceof OwnableEntity && ((OwnableEntity) player).getOwner() instanceof Player)
			player = ((OwnableEntity) player).getOwner();
		Entity looted = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		return player != null && (player.getTeam() != null || TF2ConfigVars.neutralAttack)
				&& (looted == null || !player.isAlliedTo(looted));
	}

	@Override
	public LootItemConditionType getType() {
		return TF2weapons.KILLED_BY_PLAYER_TEAM;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<KilledByTeam> {
		@Override
		public void serialize(JsonObject json, KilledByTeam value, JsonSerializationContext context) {
			json.addProperty("team", value.team);
		}

		@Override
		public KilledByTeam deserialize(JsonObject json, JsonDeserializationContext context) {
			return new KilledByTeam(GsonHelper.getAsBoolean(json, "team", false));
		}
	}
}
