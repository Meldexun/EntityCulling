package meldexun.entityculling.util;

import java.util.function.BooleanSupplier;

public class RaytracingMapCache {

	private final Int2BoolMap map = new Int2BoolMap();
	public int centerX;
	public int centerY;
	public int centerZ;

	public void setupCache(int centerX, int centerY, int centerZ) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
	}

	public boolean getOrSetCachedValue(int x, int y, int z, BooleanSupplier function) {
		int i = index(x, y, z);
		if (i < 0) {
			return function.getAsBoolean();
		}
		return map.computeIfAbsent(i, k -> function.getAsBoolean());
	}

	public boolean getCachedValue(int x, int y, int z, boolean defaultValue) {
		int i = index(x, y, z);
		if (i < 0) {
			return defaultValue;
		}
		return map.get(i);
	}

	public void setCachedValue(int x, int y, int z, boolean value) {
		int i = index(x, y, z);
		if (i < 0) {
			return;
		}
		map.put(i, value);
	}

	private int index(int x, int y, int z) {
		x = x - centerX + 512;
		y = y - centerY + 512;
		z = z - centerZ + 512;
		if (x < 0 || x >= 1024 || y < 0 || y >= 1024 || z < 0 || z >= 1024) {
			return -1;
		}
		return (z << 20) | (y << 10) | x;
	}

	public void clearCache() {
		map.clear();
	}

}
