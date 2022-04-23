package meldexun.entityculling.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CullingThread extends Thread {

	private final CachedBlockAccess cachedBlockAccess = new CachedBlockAccess();
	private final MutableBlockPos mutablePos = new MutableBlockPos();
	private final RaytracingEngine engine = new RaytracingEngine((x, y, z) -> {
		this.mutablePos.setPos(x, y, z);
		return this.cachedBlockAccess.getBlockState(this.mutablePos).isOpaqueCube();
	});
	private final RaytracingMapCache resultCache = new RaytracingMapCache();
	private double sleepOverhead = 0.0D;

	/** debug */
	private int counter = 0;
	public long[] time = new long[100];

	private boolean spectator;
	private Frustum frustum;
	private double camX;
	private double camY;
	private double camZ;
	private int camBlockX;
	private int camBlockY;
	private int camBlockZ;
	private double x;
	private double y;
	private double z;

	public CullingThread() {
		super();
		this.setName("Culling Thread");
		this.setDaemon(true);
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();

		while (true) {
			long t = System.nanoTime();
			try {
				World world = mc.world;
				EntityPlayer player = mc.player;
				Entity renderViewEntity = mc.getRenderViewEntity();

				if (world != null && player != null && renderViewEntity != null) {
					this.cachedBlockAccess.setupCached(world);
					this.spectator = player.isSpectator();
					float partialTicks = mc.getRenderPartialTicks();
					this.x = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
					this.y = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
					this.z = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
					this.frustum = new Frustum(ClippingHelperImpl.instance);
					this.frustum.setPosition(this.x, this.y, this.z);
					Vec3d cameraPosition = ActiveRenderInfo.getCameraPosition();
					this.camX = this.x + cameraPosition.x;
					this.camY = this.y + cameraPosition.y;
					this.camZ = this.z + cameraPosition.z;
					this.camBlockX = MathHelper.floor(this.camX);
					this.camBlockY = MathHelper.floor(this.camY);
					this.camBlockZ = MathHelper.floor(this.camZ);
					this.engine.setupCache(camBlockX, camBlockY, camBlockZ);
					this.resultCache.setupCache(camBlockX, camBlockY, camBlockZ);

					Iterator<Entity> entityIterator = world.loadedEntityList.iterator();
					while (entityIterator.hasNext()) {
						Entity entity;
						try {
							entity = entityIterator.next();
						} catch (ConcurrentModificationException | NoSuchElementException e) {
							// ignore
							break;
						}
						if (entity == null) {
							continue;
						}
						this.updateEntityCullingState(entity);
					}

					Iterator<TileEntity> tileEntityIterator = world.loadedTileEntityList.iterator();
					while (tileEntityIterator.hasNext()) {
						TileEntity tileEntity;
						try {
							tileEntity = tileEntityIterator.next();
						} catch (ConcurrentModificationException | NoSuchElementException e) {
							// ignore
							break;
						}
						if (tileEntity == null) {
							continue;
						}
						this.updateTileEntityCullingState(tileEntity);
					}
				}
			} catch (Throwable e) {
				mc.crashed(new CrashReport("Culling Thread crashed!", e));
			} finally {
				this.cachedBlockAccess.clearCache();
				this.engine.clearCache();
				this.resultCache.clearCache();
			}

			t = System.nanoTime() - t;

			// debug
			this.time[this.counter] = t;
			this.counter = (this.counter + 1) % this.time.length;

			double d = t / 1_000_000.0D + this.sleepOverhead;
			this.sleepOverhead = d % 1.0D;
			long sleepTime = 10 - (long) d;
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private void updateEntityCullingState(Entity entity) {
		if (!EntityCulling.useOpenGlBasedCulling()) {
			((ICullable) entity).setCulled(!this.checkEntityVisibility(entity));
		}
		if (EntityCullingClassTransformer.OPTIFINE_DETECTED) {
			((ICullable) entity).setShadowCulled(!this.checkEntityShadowVisibility(entity));
		}
	}

	private void updateTileEntityCullingState(TileEntity tileEntity) {
		if (!EntityCulling.useOpenGlBasedCulling()) {
			((ICullable) tileEntity).setCulled(!this.checkTileEntityVisibility(tileEntity));
		}
		if (EntityCullingClassTransformer.OPTIFINE_DETECTED) {
			((ICullable) tileEntity).setShadowCulled(!this.checkTileEntityShadowVisibility(tileEntity));
		}
	}

	private boolean checkEntityVisibility(Entity entity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}
		if (EntityCullingConfig.disabledInSpectator && this.spectator) {
			return true;
		}
		if (!EntityCullingConfig.entity.skipHiddenEntityRendering) {
			return true;
		}
		if (EntityCullingConfig.entity.alwaysRenderBosses && !entity.isNonBoss()) {
			return true;
		}
		if (EntityCullingConfig.entity.alwaysRenderEntitiesWithName && entity.getAlwaysRenderNameTagForRender()) {
			return true;
		}
		if (EntityCullingConfig.entity.alwaysRenderPlayers && entity instanceof EntityPlayer) {
			return true;
		}
		if (EntityCullingConfig.entity.alwaysRenderViewEntity && entity == Minecraft.getMinecraft().getRenderViewEntity()) {
			return true;
		}
		if (EntityCullingConfig.entity.skipHiddenEntityRenderingBlacklistImpl.get(entity)) {
			return true;
		}

		MutableAABB aabb = ((IBoundingBoxCache) entity).getCachedBoundingBox();
		if (aabb.sizeX() > EntityCullingConfig.entity.skipHiddenEntityRenderingSize
				|| aabb.sizeY() > EntityCullingConfig.entity.skipHiddenEntityRenderingSize
				|| aabb.sizeZ() > EntityCullingConfig.entity.skipHiddenEntityRenderingSize) {
			return true;
		}
		if (!entity.isInRangeToRender3d(this.x, this.y, this.z)) {
			return true;
		}
		if (!aabb.isVisible(frustum)) {
			// Assume that entities outside of the fov don't get rendered and thus there is no need to ray trace if they are
			// visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		return this.checkBoxCached(aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ());
	}

	private boolean checkTileEntityVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}
		if (EntityCullingConfig.disabledInSpectator && this.spectator) {
			return true;
		}
		if (!EntityCullingConfig.tileEntity.skipHiddenTileEntityRendering) {
			return true;
		}
		if (EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingBlacklistImpl.get(tileEntity)) {
			return true;
		}

		MutableAABB aabb = ((IBoundingBoxCache) tileEntity).getCachedBoundingBox();
		if (aabb.sizeX() > EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingSize
				|| aabb.sizeY() > EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingSize
				|| aabb.sizeZ() > EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingSize) {
			return true;
		}
		if (!((ICullable) tileEntity).canBeOcclusionCulled()) {
			return true;
		}

		return this.checkBoxCached(aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ());
	}

	private boolean checkEntityShadowVisibility(Entity entity) {
		if (!EntityCullingConfig.optifineShaderOptions.entityShadowsEnabled) {
			return false;
		}
		if (!EntityCullingConfig.optifineShaderOptions.entityShadowsCulling) {
			return true;
		}
		if (!EntityCulling.useOpenGlBasedCulling()) {
			if (!((ICullable) entity).isCulled()) {
				return true;
			}
			if (!EntityCullingConfig.optifineShaderOptions.entityShadowsCullingLessAggressiveMode) {
				return false;
			}
		} else {
			if (!EntityCullingConfig.enabled) {
				return true;
			}
			if (EntityCullingConfig.disabledInSpectator && this.spectator) {
				return true;
			}
			if (!EntityCullingConfig.entity.skipHiddenEntityRendering) {
				return true;
			}
			if (EntityCullingConfig.entity.alwaysRenderBosses && !entity.isNonBoss()) {
				return true;
			}
			if (EntityCullingConfig.entity.alwaysRenderEntitiesWithName && entity.getAlwaysRenderNameTagForRender()) {
				return true;
			}
			if (EntityCullingConfig.entity.alwaysRenderPlayers && entity instanceof EntityPlayer) {
				return true;
			}
			if (EntityCullingConfig.entity.alwaysRenderViewEntity && entity == Minecraft.getMinecraft().getRenderViewEntity()) {
				return true;
			}
			if (EntityCullingConfig.entity.skipHiddenEntityRenderingBlacklistImpl.get(entity)) {
				return true;
			}

			if (!entity.isInRangeToRender3d(this.x, this.y, this.z)) {
				return true;
			}
			MutableAABB aabb = ((IBoundingBoxCache) entity).getCachedBoundingBox();
			if (!aabb.isVisible(frustum)) {
				// Assume that entities outside of the fov don't get rendered and thus there is no need to ray trace if they are
				// visible.
				// But return true because there might be special entities which are always rendered.
				return true;
			}
		}
		return this.checkPointUncached(entity.posX, entity.posY + entity.height * 0.5D, entity.posZ,
				EntityCullingConfig.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkTileEntityShadowVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsEnabled) {
			return false;
		}
		if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCulling) {
			return true;
		}
		if (!EntityCulling.useOpenGlBasedCulling()) {
			if (!((ICullable) tileEntity).isCulled()) {
				return true;
			}
			if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode) {
				return false;
			}
		} else {
			if (!EntityCullingConfig.enabled) {
				return true;
			}
			if (EntityCullingConfig.disabledInSpectator && this.spectator) {
				return true;
			}
			if (!EntityCullingConfig.tileEntity.skipHiddenTileEntityRendering) {
				return true;
			}
			if (EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingBlacklistImpl.get(tileEntity)) {
				return true;
			}

			if (!((ICullable) tileEntity).canBeOcclusionCulled()) {
				return true;
			}
		}
		BlockPos pos = tileEntity.getPos();
		return this.checkPointUncached(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
				EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkBoxCached(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		int startX = MathHelper.floor(minX);
		int startY = MathHelper.floor(minY);
		int startZ = MathHelper.floor(minZ);
		int endX = MathHelper.ceil(maxX);
		int endY = MathHelper.ceil(maxY);
		int endZ = MathHelper.ceil(maxZ);

		if (this.camX >= startX && this.camX <= endX
				&& this.camY >= startY && this.camY <= endY
				&& this.camZ >= startZ && this.camZ <= endZ) {
			return true;
		}
		if (this.camX < startX) {
			if (IntUtil.anyMatch(
					startY, endY,
					startZ, endZ,
					(y, z) -> this.checkPointCached(startX, y, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camX > endX) {
			if (IntUtil.anyMatch(
					startY, endY,
					startZ, endZ,
					(y, z) -> this.checkPointCached(endX, y, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}
		if (this.camY < startY) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					startZ, endZ,
					(x, z) -> this.checkPointCached(x, startY, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camY > endY) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					startZ, endZ,
					(x, z) -> this.checkPointCached(x, endY, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}
		if (this.camZ < startZ) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					this.camY < startY ? startY + 1 : startY, this.camY > endY ? endY - 1 : endY,
					(x, y) -> this.checkPointCached(x, y, startZ, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camZ > endZ) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					this.camY < startY ? startY + 1 : startY, this.camY > endY ? endY - 1 : endY,
					(x, y) -> this.checkPointCached(x, y, endZ, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}

		return false;
	}

	private boolean checkPointCached(int endX, int endY, int endZ, double threshold) {
		return this.resultCache.getOrSetCachedValue(endX, endY, endZ, () -> checkPointUncached(endX, endY, endZ, threshold));
	}

	private boolean checkPointUncached(double endX, double endY, double endZ, double threshold) {
		return this.engine.raytraceThreshold(this.camX, this.camY, this.camZ, endX, endY, endZ, threshold);
	}

}
