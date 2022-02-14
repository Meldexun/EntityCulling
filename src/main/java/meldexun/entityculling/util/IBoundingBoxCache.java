package meldexun.entityculling.util;

import javax.annotation.Nullable;

import net.minecraft.util.math.AxisAlignedBB;

public interface IBoundingBoxCache {

	void updateCachedBoundingBox();

	AxisAlignedBB getCachedBoundingBox();

	@Nullable
	AxisAlignedBB getCachedBoundingBoxUnsafe();

}
