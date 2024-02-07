package meldexun.entityculling.util.raytracing;

import java.util.function.IntSupplier;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.MathUtil;
import meldexun.entityculling.util.function.IntIntInt2BooleanFunction;
import meldexun.entityculling.util.function.LazyIntIntInt2BooleanFunction;
import meldexun.entityculling.util.function.LazyIntIntIntDouble2BooleanFunction;

public class RaytracingEngine {

	private double camX;
	private double camY;
	private double camZ;
	private int camBlockX;
	private int camBlockY;
	private int camBlockZ;
	private final IRaytracingCache resultCache;
	private final IRaytracingCache opacityCache;
	private final LazyIntIntIntDouble2BooleanFunction raytraceUncachedThresholdFunction = new LazyIntIntIntDouble2BooleanFunction(this::raytraceUncachedThreshold);
	private final LazyIntIntInt2BooleanFunction raytraceUncachedFunction = new LazyIntIntInt2BooleanFunction(this::raytraceUncached);
	private final LazyIntIntInt2BooleanFunction isOpaqueFunction;
	private final IntSupplier renderDistSupplier;

	public RaytracingEngine(int cacheSize, IntIntInt2BooleanFunction isOpaqueFunction, IntSupplier renderDistSupplier) {
		this.resultCache = new RaytracingMapCache();
		this.opacityCache = new RaytracingArrayCache(cacheSize);
		this.isOpaqueFunction = new LazyIntIntInt2BooleanFunction(isOpaqueFunction);
		this.renderDistSupplier = renderDistSupplier;
	}

	public void setup(double x, double y, double z) {
		camX = x;
		camY = y;
		camZ = z;
		camBlockX = MathUtil.floor(x);
		camBlockY = MathUtil.floor(y);
		camBlockZ = MathUtil.floor(z);
	}

	public void clearCache() {
		resultCache.clearCache();
		opacityCache.clearCache();
	}

	public boolean raytraceCachedThreshold(int endX, int endY, int endZ, double threshold) {
		if (isTooFarAway(endX, endY, endZ))
			return true;
		raytraceUncachedThresholdFunction.setXYZD(endX, endY, endZ, threshold);
		return resultCache.getOrSetCachedValue(endX - camBlockX, endY - camBlockY, endZ - camBlockZ, raytraceUncachedThresholdFunction);
	}

	public boolean raytraceCached(int endX, int endY, int endZ) {
		if (isTooFarAway(endX, endY, endZ))
			return true;
		raytraceUncachedFunction.setXYZ(endX, endY, endZ);
		return resultCache.getOrSetCachedValue(endX - camBlockX, endY - camBlockY, endZ - camBlockZ, raytraceUncachedFunction);
	}

	public boolean raytraceUncachedThreshold(double endX, double endY, double endZ, double threshold) {
		if (isTooFarAway(endX, endY, endZ))
			return true;
		return raytraceThreshold(camX, camY, camZ, endX, endY, endZ, threshold);
	}

	public boolean raytraceUncached(double endX, double endY, double endZ) {
		if (isTooFarAway(endX, endY, endZ))
			return true;
		return raytrace(camX, camY, camZ, endX, endY, endZ);
	}

	private boolean isTooFarAway(double x, double y, double z) {
		double distSqr = EntityCullingConfig.raytraceDistanceCalculator.distSqr(camX, camY, camZ, x, y, z);
		double maxDistSqr = MathUtil.square(((renderDistSupplier.getAsInt() << 4)
				+ EntityCullingConfig.raytraceDistanceLimitAdder)
				* EntityCullingConfig.raytraceDistanceLimitMultiplier);
		return distSqr > maxDistSqr;
	}

	private boolean raytraceThreshold(double startX, double startY, double startZ, double endX, double endY, double endZ, double threshold) {
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
		double dx = incX == 0 ? Double.MAX_VALUE : incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : incZ / dirZ;
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

	private boolean raytrace(double startX, double startY, double startZ, double endX, double endY, double endZ) {
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
		double dx = incX == 0 ? Double.MAX_VALUE : incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : incZ / dirZ;
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

	private boolean isOpaque(int x, int y, int z) {
		isOpaqueFunction.setXYZ(x, y, z);
		return opacityCache.getOrSetCachedValue(x - camBlockX, y - camBlockY, z - camBlockZ, isOpaqueFunction);
	}

}
