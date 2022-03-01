package meldexun.entityculling.util;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.raytraceutil.RayTracingCache;
import net.minecraft.util.EnumFacing.Axis;

public class RaytracingEngine {

	public interface PositionPredicate {

		boolean isOpaque(int x, int y, int z);

	}

	private final RayTracingCache cache = new RayTracingCache(EntityCullingConfig.cacheSize);
	private int centerX;
	private int centerY;
	private int centerZ;
	private final PositionPredicate positionPredicate;

	public RaytracingEngine(PositionPredicate positionPredicate) {
		this.positionPredicate = positionPredicate;
	}

	public void setupCache(int x, int y, int z) {
		centerX = x;
		centerY = y;
		centerZ = z;
	}

	public void clearCache() {
		cache.clearCache();
	}

	private boolean isOpaque(int x, int y, int z) {
		return cache.getOrSetCachedValue(x - centerX, y - centerY, z - centerZ, () -> positionPredicate.isOpaque(x, y, z) ? 1 : 2) == 1;
	}

	public boolean raytraceThreshold(double startX, double startY, double startZ, double endX, double endY, double endZ, double threshold) {
		if (threshold <= 0.0D) {
			return raytrace(startX, startY, startZ, endX, endY, endZ);
		}

		double dirX = endX - startX;
		double dirY = endY - startY;
		double dirZ = endZ - startZ;

		if (dirX * dirX + dirY * dirY + dirZ * dirZ <= threshold * threshold) {
			return true;
		}

		int x = MathUtil.floor(startX);
		int y = MathUtil.floor(startY);
		int z = MathUtil.floor(startZ);
		int incX = MathUtil.signum(dirX);
		int incY = MathUtil.signum(dirY);
		int incZ = MathUtil.signum(dirZ);
		double dx = incX == 0 ? Double.MAX_VALUE : (double) incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : (double) incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : (double) incZ / dirZ;
		double percentX = dx * (incX > 0 ? 1.0D - MathUtil.frac(startX) : MathUtil.frac(startX));
		double percentY = dy * (incY > 0 ? 1.0D - MathUtil.frac(startY) : MathUtil.frac(startY));
		double percentZ = dz * (incZ > 0 ? 1.0D - MathUtil.frac(startZ) : MathUtil.frac(startZ));
		Axis axis;

		if (isOpaque(x, y, z)) {
			double d1 = Math.min(Math.min(Math.min(percentX, percentY), percentZ), 1.0D);
			double nextHitX = startX + dirX * d1;
			double nextHitY = startY + dirY * d1;
			double nextHitZ = startZ + dirZ * d1;

			threshold -= MathUtil.dist(startX, startY, startZ, nextHitX, nextHitY, nextHitZ);
			if (threshold <= 0.0D) {
				return false;
			}
		}

		while (percentX <= 1.0D || percentY <= 1.0D || percentZ <= 1.0D) {
			if (percentX < percentY) {
				if (percentX < percentZ) {
					x += incX;
					percentX += dx;
					axis = Axis.X;
				} else {
					z += incZ;
					percentZ += dz;
					axis = Axis.Z;
				}
			} else if (percentY < percentZ) {
				y += incY;
				percentY += dy;
				axis = Axis.Y;
			} else {
				z += incZ;
				percentZ += dz;
				axis = Axis.Z;
			}

			if (isOpaque(x, y, z)) {
				double d = Math.min(axis != Axis.X ? (axis != Axis.Y ? percentZ - dz : percentY - dy) : percentX - dx, 1.0D);
				double hitX = startX + dirX * d;
				double hitY = startY + dirY * d;
				double hitZ = startZ + dirZ * d;

				double d1 = Math.min(Math.min(Math.min(percentX, percentY), percentZ), 1.0D);
				double nextHitX = startX + dirX * d1;
				double nextHitY = startY + dirY * d1;
				double nextHitZ = startZ + dirZ * d1;

				threshold -= MathUtil.dist(hitX, hitY, hitZ, nextHitX, nextHitY, nextHitZ);
				if (threshold <= 0.0D) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean raytrace(double startX, double startY, double startZ, double endX, double endY, double endZ) {
		int x = MathUtil.floor(startX);
		int y = MathUtil.floor(startY);
		int z = MathUtil.floor(startZ);

		if (isOpaque(x, y, z)) {
			return false;
		}

		double dirX = endX - startX;
		double dirY = endY - startY;
		double dirZ = endZ - startZ;
		int incX = MathUtil.signum(dirX);
		int incY = MathUtil.signum(dirY);
		int incZ = MathUtil.signum(dirZ);
		double dx = incX == 0 ? Double.MAX_VALUE : (double) incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : (double) incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : (double) incZ / dirZ;
		double percentX = dx * (incX > 0 ? 1.0D - MathUtil.frac(startX) : MathUtil.frac(startX));
		double percentY = dy * (incY > 0 ? 1.0D - MathUtil.frac(startY) : MathUtil.frac(startY));
		double percentZ = dz * (incZ > 0 ? 1.0D - MathUtil.frac(startZ) : MathUtil.frac(startZ));

		while (percentX <= 1.0D || percentY <= 1.0D || percentZ <= 1.0D) {
			if (percentX < percentY) {
				if (percentX < percentZ) {
					x += incX;
					percentX += dx;
				} else {
					z += incZ;
					percentZ += dz;
				}
			} else if (percentY < percentZ) {
				y += incY;
				percentY += dy;
			} else {
				z += incZ;
				percentZ += dz;
			}

			if (isOpaque(x, y, z)) {
				return false;
			}
		}

		return true;
	}

}
