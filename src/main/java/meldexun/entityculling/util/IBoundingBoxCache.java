package meldexun.entityculling.util;

import java.util.HashSet;
import java.util.Set;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

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

	AABB getCachedBoundingBox();

	void setCachedBoundingBox(AABB aabb);

	default AABB getOrCacheBoundingBox() {
		if (this.isCacheable() == 0) {
			ResourceLocation registryName = BlockEntityType.getKey(((BlockEntity) this).getType());
			if (BLACKLIST.contains(registryName.getNamespace()) || BLACKLIST.contains(registryName.toString())) {
				this.setCacheable(2);
			} else {
				this.setCachedBoundingBox(((BlockEntity) this).getRenderBoundingBox());
				this.setCacheable(1);
			}
		}
		if (this.isCacheable() == 2) {
			return ((BlockEntity) this).getRenderBoundingBox();
		}
		return this.getCachedBoundingBox();
	}

	Set<String> BLACKLIST = new HashSet<>();

	static void updateBlacklist() {
		BLACKLIST.clear();
		EntityCullingConfig.CLIENT_CONFIG.tileEntityCachedBoundingBoxBlacklist.get().forEach(BLACKLIST::add);
	}

}
