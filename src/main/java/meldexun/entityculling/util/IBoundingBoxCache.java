package meldexun.entityculling.util;

public interface IBoundingBoxCache {

	void updateCachedBoundingBox(double partialTicks);

	MutableAABB getCachedBoundingBox();

}
