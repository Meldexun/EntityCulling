package meldexun.entityculling.util.raytracing;

import java.util.function.BooleanSupplier;

interface IRaytracingCache {

	boolean getOrSetCachedValue(int x, int y, int z, BooleanSupplier function);

	void clearCache();

}
