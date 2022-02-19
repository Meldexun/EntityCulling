package meldexun.entityculling.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.IBoundingBoxCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public class MixinEntity implements IBoundingBoxCache {

	@Unique
	private AxisAlignedBB cachedBoundingBox;

	@Unique
	@Override
	public void updateCachedBoundingBox() {
		cachedBoundingBox = ((Entity) (Object) this).getRenderBoundingBox();
		Vec3d v = EntityCullingConfig.entity.entityBoundingBoxGrowthListImpl.get((Entity) (Object) this);
		if (v != null) {
			cachedBoundingBox = new AxisAlignedBB(
					cachedBoundingBox.minX - v.x, cachedBoundingBox.minY - v.z, cachedBoundingBox.minZ - v.x,
					cachedBoundingBox.maxX + v.x, cachedBoundingBox.maxY + v.y, cachedBoundingBox.maxZ + v.x);
		}
	}

	@Unique
	@Override
	public AxisAlignedBB getCachedBoundingBox() {
		if (this.cachedBoundingBox != null) {
			return this.cachedBoundingBox;
		}
		return ((Entity) (Object) this).getRenderBoundingBox();
	}

	@Unique
	@Override
	@Nullable
	public AxisAlignedBB getCachedBoundingBoxUnsafe() {
		return this.cachedBoundingBox;
	}

}
