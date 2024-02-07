package meldexun.entityculling.util.raytracing;

import java.util.function.BooleanSupplier;

class RaytracingMapCache implements IRaytracingCache {

	private final Int2BoolMap map = new Int2BoolMap();

	@Override
	public boolean getOrSetCachedValue(int x, int y, int z, BooleanSupplier function) {
		int i = index(x, y, z);
		if (i < 0) {
			return function.getAsBoolean();
		}
		return map.computeIfAbsent(i, function);
	}

	private int index(int x, int y, int z) {
		x += 512;
		if (x < 0 || x >= 1024) {
			return -1;
		}
		y += 512;
		if (y < 0 || y >= 1024) {
			return -1;
		}
		z += 512;
		if (z < 0 || z >= 1024) {
			return -1;
		}
		return (z << 20) | (y << 10) | x;
	}

	@Override
	public void clearCache() {
		map.clear();
	}

}
