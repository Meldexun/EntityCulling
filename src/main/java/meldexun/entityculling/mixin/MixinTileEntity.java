package meldexun.entityculling.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.IBoundingBoxCache;
import meldexun.entityculling.util.MutableAABB;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(TileEntity.class)
public class MixinTileEntity implements IBoundingBoxCache {

	@SuppressWarnings("serial")
	@Unique
	private static final Random RAND = new Random() {
		private static final long MULTIPLIER = 0x5_DEEC_E66DL;
		private static final long ADDEND = 0xBL;
		private static final long MASK = (1L << 48) - 1;
		private long seed = 0L;

		@Override
		public void setSeed(long seed) {
			this.seed = (seed ^ MULTIPLIER) & MASK;
		}

		@Override
		protected int next(int bits) {
			this.seed = (this.seed * MULTIPLIER + ADDEND) & MASK;
			return (int) (this.seed >>> (48 - bits));
		}
	};

	@Unique
	private final MutableAABB cachedBoundingBox = new MutableAABB();
	private boolean initialized;

	@Unique
	@Override
	public void updateCachedBoundingBox(double partialTicks) {
		if (!initialized
				|| !EntityCullingConfig.tileEntityCachedBoundingBoxEnabled
				|| EntityCullingConfig.tileEntityCachedBoundingBoxBlacklistImpl.get((TileEntity) (Object) this)
				|| EntityCullingConfig.tileEntityCachedBoundingBoxUpdateInterval == 1
				|| RAND.nextInt(EntityCullingConfig.tileEntityCachedBoundingBoxUpdateInterval) == 0) {
			cachedBoundingBox.set(((TileEntity) (Object) this).getRenderBoundingBox());
			Vec3d v = EntityCullingConfig.tileEntity.tileEntityBoundingBoxGrowthListImpl.get((TileEntity) (Object) this);
			if (v != null) {
				cachedBoundingBox.grow(v);
			}
			initialized = true;
		}
	}

	@Unique
	@Override
	public MutableAABB getCachedBoundingBox() {
		return this.cachedBoundingBox;
	}

}
