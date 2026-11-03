package rafradek.TF2weapons.message;

import rafradek.TF2weapons.util.TF2HitResult;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import java.util.ArrayList;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TF2ProjectileHandler implements IMessageHandler<TF2Message.PredictionMessage, IMessage> {

	// public static HashMap<Entity, ArrayList<PredictionMessage>> nextShotPos=
	// new HashMap<Entity, ArrayList<PredictionMessage>>();

	@Override
	public IMessage onMessage(final PredictionMessage message, MessageContext ctx) {
		final Player shooter = ctx.getServerHandler().player;
		// ItemStack stack=shooter.getHeldItem(EnumHand.MAIN_HAND);
		((ServerLevel) shooter.level).execute(() -> {
			// shooter.getCapability(TF2weapons.WEAPONS_CAP, null).predictionList.poll();
			message.target = new ArrayList<>();
			if (message.readData != null)
				for (Object[] obj : message.readData) {
					HitResult result;

					if (obj[0] != null) {
						Entity entity = shooter.level.getEntity((int) obj[0]);
						Vec3 hit1 = new Vec3((Byte) obj[6] / 16D + entity.getX(), (Byte) obj[7] / 16D + entity.getY(),
								(Byte) obj[8] / 16D + entity.getZ());
						result = new HitResult(entity, hit1);
						TF2HitResult.setHitInfo(result, new float[] { (Boolean) obj[1] ? 1f : 0f, (Float) obj[2] });
					} else {
						BlockPos pos = new BlockPos((Integer) obj[3], (Integer) obj[4], (Integer) obj[5]);
						Vec3 hit2 = new Vec3((Byte) obj[6] / 16D + pos.getX(), (Byte) obj[7] / 16D + pos.getY(),
								(Byte) obj[8] / 16D + pos.getZ());
						result = new HitResult(hit2, Direction.from3DDataValue((Byte) obj[1]), pos);
						TF2HitResult.setHitInfo(result, new float[] { (Float) obj[2] });
					}
					message.target.add(result);
				}
			Deque<PredictionMessage> deque = shooter.getCapability(TF2weapons.WEAPONS_CAP,
					null).predictionList[(message.state == 1 ? 0 : 2) + message.hand.ordinal()];
			deque.addLast(message);
			message.time = shooter.level.getGameTime();
		});
		return null;
	}

}
