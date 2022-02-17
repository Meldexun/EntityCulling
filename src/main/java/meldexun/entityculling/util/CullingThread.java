package meldexun.entityculling.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.raytraceutil.RayTracingCache;
import meldexun.raytraceutil.RayTracingEngine;
import meldexun.raytraceutil.RayTracingEngine.MutableRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CullingThread extends Thread {

	private final CachedBlockAccess cachedBlockAccess = new CachedBlockAccess();
	private final MutableBlockPos mutablePos = new MutableBlockPos();
	private final RayTracingEngine engine = new RayTracingEngine((x, y, z) -> {
		this.mutablePos.setPos(x, y, z);
		return this.cachedBlockAccess.getBlockState(this.mutablePos).isOpaqueCube();
	});
	// 0=not cached, 1=blocked, 2=visible
	private final RayTracingCache cache = new RayTracingCache(EntityCullingConfig.cacheSize);
	private final MutableRayTraceResult mutableRayTraceResult = new MutableRayTraceResult();
	private double sleepOverhead = 0.0D;

	/** debug */
	private int counter = 0;
	public long[] time = new long[100];
	private boolean result = false;
	private static List<RaytraceInfo> privateDebugRayList = new ArrayList<>();
	public static List<RaytraceInfo> publicDebugRayList = new ArrayList<>();

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
				privateDebugRayList.clear();

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

				synchronized (CullingThread.class) {
					List<RaytraceInfo> temp = publicDebugRayList;
					publicDebugRayList = privateDebugRayList;
					privateDebugRayList = temp;
				}
			} catch (Throwable e) {
				mc.crashed(new CrashReport("Culling Thread crashed!", e));
			} finally {
				this.cachedBlockAccess.clearCache();
				this.cache.clearCache();
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
		if (EntityCullingConfig.entity.skipHiddenEntityRenderingBlacklistImpl.contains(entity)) {
			return true;
		}

		AxisAlignedBB aabb = ((IBoundingBoxCache) entity).getCachedBoundingBoxUnsafe();
		if (aabb == null) {
			return !((ICullable) entity).isCulled();
		}
		double minX = aabb.minX - 0.5D;
		double minY = aabb.minY - 0.5D;
		double minZ = aabb.minZ - 0.5D;
		double maxX = aabb.maxX + 0.5D;
		double maxY = aabb.maxY + 0.5D;
		double maxZ = aabb.maxZ + 0.5D;
		if (maxX - minX > EntityCullingConfig.entity.skipHiddenEntityRenderingSize
				|| maxY - minY > EntityCullingConfig.entity.skipHiddenEntityRenderingSize
				|| maxZ - minZ > EntityCullingConfig.entity.skipHiddenEntityRenderingSize) {
			return true;
		}
		if (!entity.isInRangeToRender3d(this.x, this.y, this.z)) {
			return true;
		}
		if (!this.frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that entities outside of the fov don't get rendered and thus there is no need to ray trace if they are
			// visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (EntityCullingConfig.debugRenderBoxes) {
			privateDebugRayList.add(new RaytraceInfo((minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, true));
		}
		this.result = this.checkPointUncached(this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D,
				EntityCullingConfig.raytraceThreshold);
		this.result |= this.checkBoxCached(minX, minY, minZ, maxX, maxY, maxZ);
		return this.result;
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
		if (EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingBlacklistImpl.contains(tileEntity)) {
			return true;
		}

		AxisAlignedBB aabb = ((IBoundingBoxCache) tileEntity).getCachedBoundingBoxUnsafe();
		if (aabb == null) {
			return !((ICullable) tileEntity).isCulled();
		}
		if (aabb.maxX - aabb.minX > EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingSize
				|| aabb.maxY - aabb.minY > EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingSize
				|| aabb.maxZ - aabb.minZ > EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingSize) {
			return true;
		}
		if (tileEntity.getDistanceSq(this.x, this.y, this.z) >= tileEntity.getMaxRenderDistanceSquared()) {
			return true;
		}
		if (!this.frustum.isBoxInFrustum(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)) {
			// Assume that tile entities outside of the fov don't get rendered and thus there is no need to ray trace if they are
			// visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (EntityCullingConfig.debugRenderBoxes) {
			privateDebugRayList.add(new RaytraceInfo((aabb.minX + aabb.maxX) * 0.5D, (aabb.minY + aabb.maxY) * 0.5D, (aabb.minZ + aabb.maxZ) * 0.5D, true));
		}
		this.result = this.checkPointUncached(this.camX, this.camY, this.camZ, (aabb.minX + aabb.maxX) * 0.5D, (aabb.minY + aabb.maxY) * 0.5D,
				(aabb.minZ + aabb.maxZ) * 0.5D, EntityCullingConfig.raytraceThreshold);
		this.result |= this.checkBoxCached(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
		return this.result;
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
		}
		return this.checkPointUncached(this.camX, this.camY, this.camZ, entity.posX, entity.posY + entity.height * 0.5D, entity.posZ,
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
		}
		BlockPos pos = tileEntity.getPos();
		return this.checkPointUncached(this.camX, this.camY, this.camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
				EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkBoxCached(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (EntityCullingConfig.cachelessMode) {
			return this.checkBoxUncached(minX, minY, minZ, maxX, maxY, maxZ);
		}

		int startX = MathHelper.floor(minX);
		int startY = MathHelper.floor(minY);
		int startZ = MathHelper.floor(minZ);
		int endX = MathHelper.ceil(maxX);
		int endY = MathHelper.ceil(maxY);
		int endZ = MathHelper.ceil(maxZ);

		if (this.camX >= startX && this.camX <= endX && this.camY >= startY && this.camY <= endY && this.camZ >= startZ && this.camZ <= endZ) {
			this.result = true;
		}
		if (this.camX < startX) {
			IntUtil.forEach(startY, endY, startZ, endZ, (y, z) -> this.checkPointCachedAndDebug(startX, y, z));
		} else if (this.camX > endX) {
			IntUtil.forEach(startY, endY, startZ, endZ, (y, z) -> this.checkPointCachedAndDebug(endX, y, z));
		}
		if (this.camY < startY) {
			IntUtil.forEach(startX, endX, startZ, endZ, (x, z) -> this.checkPointCachedAndDebug(x, startY, z));
		} else if (this.camY > endY) {
			IntUtil.forEach(startX, endX, startZ, endZ, (x, z) -> this.checkPointCachedAndDebug(x, endY, z));
		}
		if (this.camZ < startZ) {
			IntUtil.forEach(startX, endX, startY, endY, (x, y) -> this.checkPointCachedAndDebug(x, y, startZ));
		} else if (this.camZ > endZ) {
			IntUtil.forEach(startX, endX, startY, endY, (x, y) -> this.checkPointCachedAndDebug(x, y, endZ));
		}

		return this.result;
	}

	private void checkPointCachedAndDebug(int x, int y, int z) {
		if (EntityCullingConfig.debugRenderBoxes) {
			privateDebugRayList.add(new RaytraceInfo(x, y, z, !this.result));
		}
		if (!this.result && this.checkPointCached(x, y, z)) {
			this.result = true;
		}
	}

	private boolean checkBoxUncached(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (this.camX >= minX && this.camX <= maxX && this.camY >= minY && this.camY <= maxY && this.camZ >= minZ && this.camZ <= maxZ) {
			this.result = true;
		}

		int stepsX = MathHelper.ceil(maxX - minX);
		int stepsY = MathHelper.ceil(maxY - minY);
		int stepsZ = MathHelper.ceil(maxZ - minZ);

		if (this.camX < minX) {
			IntUtil.forEach(0, stepsY, 0, stepsZ, (iy, iz) -> {
				double y = MathUtil.lerp(minY, maxY, (double) iy / stepsY);
				double z = MathUtil.lerp(minZ, maxZ, (double) iz / stepsZ);
				this.checkPointUncachedAndDebug(minX, y, z);
			});
		} else if (this.camX > maxX) {
			IntUtil.forEach(0, stepsY, 0, stepsZ, (iy, iz) -> {
				double y = MathUtil.lerp(minY, maxY, (double) iy / stepsY);
				double z = MathUtil.lerp(minZ, maxZ, (double) iz / stepsZ);
				this.checkPointUncachedAndDebug(maxX, y, z);
			});
		}

		if (this.camY < minY) {
			IntUtil.forEach(0, stepsX, 0, stepsZ, (ix, iz) -> {
				double x = MathUtil.lerp(minX, maxX, (double) ix / stepsX);
				double z = MathUtil.lerp(minZ, maxZ, (double) iz / stepsZ);
				this.checkPointUncachedAndDebug(x, minY, z);
			});
		} else if (this.camY > maxY) {
			IntUtil.forEach(0, stepsX, 0, stepsZ, (ix, iz) -> {
				double x = MathUtil.lerp(minX, maxX, (double) ix / stepsX);
				double z = MathUtil.lerp(minZ, maxZ, (double) iz / stepsZ);
				this.checkPointUncachedAndDebug(x, maxY, z);
			});
		}

		if (this.camZ < minZ) {
			IntUtil.forEach(0, stepsX, 0, stepsY, (ix, iy) -> {
				double x = MathUtil.lerp(minX, maxX, (double) ix / stepsX);
				double y = MathUtil.lerp(minY, maxY, (double) iy / stepsY);
				this.checkPointUncachedAndDebug(x, y, minZ);
			});
		} else if (this.camZ > maxZ) {
			IntUtil.forEach(0, stepsX, 0, stepsY, (ix, iy) -> {
				double x = MathUtil.lerp(minX, maxX, (double) ix / stepsX);
				double y = MathUtil.lerp(minY, maxY, (double) iy / stepsY);
				this.checkPointUncachedAndDebug(x, y, maxZ);
			});
		}

		return this.result;
	}

	private void checkPointUncachedAndDebug(double x, double y, double z) {
		if (EntityCullingConfig.debugRenderBoxes) {
			privateDebugRayList.add(new RaytraceInfo(x, y, z, !this.result));
		}
		if (!this.result && this.checkPointUncached(this.camX, this.camY, this.camZ, x, y, z, EntityCullingConfig.raytraceThreshold)) {
			this.result = true;
		}
	}

	private boolean checkPointCached(int endX, int endY, int endZ) {
		return this.cache.getOrSetCachedValue(endX - this.camBlockX, endY - this.camBlockY, endZ - this.camBlockZ,
				() -> this.checkPointUncached(this.camX, this.camY, this.camZ, endX, endY, endZ, EntityCullingConfig.raytraceThreshold) ? 2 : 1) == 2;
	}

	private boolean checkPointUncached(double startX, double startY, double startZ, double endX, double endY, double endZ, double maxDiff) {
		return this.engine.rayTraceBlocks(startX, startY, startZ, endX, endY, endZ, true, maxDiff, this.mutableRayTraceResult) == null;
	}

}
