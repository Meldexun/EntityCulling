package meldexun.entityculling.mixin;

import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.IBoundingBoxCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

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
	private AxisAlignedBB cachedBoundingBox;

	@Unique
	@Override
	public void updateCachedBoundingBox() {
		if (!EntityCullingConfig.tileEntityCachedBoundingBoxEnabled
				|| EntityCullingConfig.tileEntityCachedBoundingBoxBlacklistImpl.contains((TileEntity) (Object) this)
				|| EntityCullingConfig.tileEntityCachedBoundingBoxUpdateInterval == 1
				|| RAND.nextInt(EntityCullingConfig.tileEntityCachedBoundingBoxUpdateInterval) == 0) {
			cachedBoundingBox = ((TileEntity) (Object) this).getRenderBoundingBox();
		}
	}

	@Unique
	@Override
	public AxisAlignedBB getCachedBoundingBox() {
		if (this.cachedBoundingBox != null) {
			return this.cachedBoundingBox;
		}
		return ((TileEntity) (Object) this).getRenderBoundingBox();
	}

	@Unique
	@Override
	@Nullable
	public AxisAlignedBB getCachedBoundingBoxUnsafe() {
		return this.cachedBoundingBox;
	}

}
