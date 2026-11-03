package rafradek.TF2weapons.client;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Client-only construction of the entity a statue displays. */
public class ClientStatueHelper {

	public static LivingEntity createDisplayEntity(Level world, GameProfile profile, CompoundTag data) {
		if (data == null)
			return null;
		if (profile != null) {
			RemotePlayer player = new RemotePlayer((ClientLevel) world, profile, null);
			player.load(data);
			return player;
		}
		return EntityType.create(data, world).filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e)
				.orElse(null);
	}
}
