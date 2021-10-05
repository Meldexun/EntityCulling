package meldexun.entityculling.util;

import java.util.HashSet;
import java.util.Set;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

public interface IBoundingBoxCache {

	/**
	 * 0 = needs update<br>
	 * 1 = cacheable<br>
	 * 2 = not cacheable
	 */
	int isCacheable();

	/**
	 * 0 = needs update<br>
	 * 1 = cacheable<br>
	 * 2 = not cacheable
	 */
	void setCacheable(int cacheable);

	AxisAlignedBB getCachedBoundingBox();

	void setCachedBoundingBox(AxisAlignedBB aabb);

	default AxisAlignedBB getOrCacheBoundingBox() {
		if (this.isCacheable() == 0) {
			ResourceLocation registryName = TileEntityType.getKey(((TileEntity) this).getType());
			if (BLACKLIST.contains(registryName.getNamespace()) || BLACKLIST.contains(registryName.toString())) {
				this.setCacheable(2);
			} else {
				this.setCachedBoundingBox(((TileEntity) this).getRenderBoundingBox());
				this.setCacheable(1);
			}
		}
		if (this.isCacheable() == 2) {
			return ((TileEntity) this).getRenderBoundingBox();
		}
		return this.getCachedBoundingBox();
	}

	Set<String> BLACKLIST = new HashSet<>();

	static void updateBlacklist() {
		BLACKLIST.clear();
		EntityCullingConfig.CLIENT_CONFIG.tileEntityCachedBoundingBoxBlacklist.get().forEach(BLACKLIST::add);
	}

}
