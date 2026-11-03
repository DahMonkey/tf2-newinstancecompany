package rafradek.TF2weapons.util;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Entity hit with the extra data 1.12's RayTraceResult carried: {@code hitInfo} (the mod stores
 * "was a headshot" there) and the side that was hit.
 */
public class TF2HitResult extends EntityHitResult {
	public Object hitInfo;
	public Direction sideHit = Direction.UP;

	public TF2HitResult(Entity entity, Vec3 location) {
		super(entity, location);
	}

	public static boolean isHeadshot(HitResult result) {
		return result instanceof TF2HitResult && Boolean.TRUE.equals(((TF2HitResult) result).hitInfo);
	}

	public static Object getHitInfo(HitResult result) {
		return result instanceof TF2HitResult ? ((TF2HitResult) result).hitInfo : null;
	}

	public static Entity getEntity(HitResult result) {
		return result instanceof EntityHitResult ? ((EntityHitResult) result).getEntity() : null;
	}
}
