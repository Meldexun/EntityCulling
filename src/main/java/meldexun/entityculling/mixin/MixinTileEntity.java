package meldexun.entityculling.mixin;

import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.integration.ValkyrienSkies;
import meldexun.entityculling.util.IBoundingBoxCache;
import meldexun.entityculling.util.ITileEntityRendererCache;
import meldexun.entityculling.util.MutableAABB;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(TileEntity.class)
public class MixinTileEntity implements IBoundingBoxCache, ITileEntityRendererCache {

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
	@Unique
	private boolean initialized;
	@Unique
	private TileEntitySpecialRenderer<TileEntity> renderer;
	@Unique
	private boolean rendererInitialized;

	@Unique
	@Override
	public void updateCachedBoundingBox(double partialTicks) {
		if (!initialized
				|| !EntityCullingConfig.tileEntityCachedBoundingBoxEnabled
				|| EntityCullingConfig.tileEntityCachedBoundingBoxBlacklistImpl.get((TileEntity) (Object) this)
				|| EntityCullingConfig.tileEntityCachedBoundingBoxUpdateInterval == 1
				|| RAND.nextInt(EntityCullingConfig.tileEntityCachedBoundingBoxUpdateInterval) == 0) {
			cachedBoundingBox.set(EntityCulling.isValkyrienSkiesInstalled ? ValkyrienSkies.getAABB((TileEntity) (Object) this) : ((TileEntity) (Object) this).getRenderBoundingBox());
			Vec3d v = EntityCullingConfig.tileEntity.tileEntityBoundingBoxGrowthListImpl.get((TileEntity) (Object) this);
			if (v != null) {
				cachedBoundingBox.grow(v);
			}
			if (EntityCulling.useOpenGlBasedCulling() && EntityCullingConfig.tileEntityAABBGrowth) {
				cachedBoundingBox.grow(0.03125D);
			}
			initialized = true;
		}
	}

	@Unique
	@Override
	public MutableAABB getCachedBoundingBox() {
		return this.cachedBoundingBox;
	}

	@SuppressWarnings("unchecked")
	@Unique
	@Override
	@Nullable
	public <T extends TileEntity> TileEntitySpecialRenderer<T> getRenderer() {
		if (!rendererInitialized) {
			renderer = loadRenderer((TileEntity) (Object) this);
			rendererInitialized = true;
		}
		return (TileEntitySpecialRenderer<T>) renderer;
	}

}
