package meldexun.entityculling.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.util.IBoundingBoxCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

@Mixin(Entity.class)
public class MixinEntity implements IBoundingBoxCache {

	@Unique
	private AxisAlignedBB cachedBoundingBox;

	@Unique
	@Override
	public void updateCachedBoundingBox() {
		cachedBoundingBox = ((Entity) (Object) this).getRenderBoundingBox();
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
