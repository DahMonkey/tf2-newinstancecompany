package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.message.TF2Message.BuildingConfigMessage;
import java.util.HashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class TF2BuildingConfigHandler implements IMessageHandler<TF2Message.BuildingConfigMessage, IMessage> {

	public static HashMap<Entity, float[]> shotInfo = new HashMap<>();

	@Override
	public IMessage onMessage(final BuildingConfigMessage message, final MessageContext ctx) {

		TF2weapons.server.addScheduledTask(() -> {
			Entity ent = ctx.getServerHandler().player.level.getEntity(message.entityid);
			if (ent != null && ent instanceof EntityBuilding
					&& ((EntityBuilding) ent).getOwner() == ctx.getServerHandler().player) {
				if (message.id == 127) {
					((EntityBuilding) ent).grab();
					return;
				}

				if (ent instanceof EntityTeleporter) {
					if (message.id == 0)
						((EntityTeleporter) ent)
								.setID(Mth.clamp(message.value, 0, EntityTeleporter.TP_PER_PLAYER - 1));
					else if (message.id == 1)
						((EntityTeleporter) ent).setExit(message.value == 1);
				} else if (ent instanceof EntitySentry)
					if (message.id == 0)
						((EntitySentry) ent).setTargetInfo(message.value);
			}
		});

		return null;
	}

}
