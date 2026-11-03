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
		return Boolean.TRUE.equals(getHitInfo(result));
	}

	/** Extra data attached to any hit result (1.12 RayTraceResult.hitInfo). */
	private static final java.util.Map<HitResult, Object> EXTRA = java.util.Collections
			.synchronizedMap(new java.util.WeakHashMap<>());

	public static Object getHitInfo(HitResult result) {
		if (result instanceof TF2HitResult)
			return ((TF2HitResult) result).hitInfo;
		return result == null ? null : EXTRA.get(result);
	}

	public static void setHitInfo(HitResult result, Object info) {
		if (result instanceof TF2HitResult)
			((TF2HitResult) result).hitInfo = info;
		else if (result != null)
			EXTRA.put(result, info);
	}

	public static Entity getEntity(HitResult result) {
		return result instanceof EntityHitResult ? ((EntityHitResult) result).getEntity() : null;
	}
}
