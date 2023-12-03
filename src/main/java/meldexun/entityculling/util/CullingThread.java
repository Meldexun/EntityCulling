package meldexun.entityculling.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.LongStream;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.integration.Hats;
import meldexun.entityculling.util.raytracing.RaytracingEngine;
import meldexun.renderlib.api.IBoundingBoxCache;
import meldexun.renderlib.api.IEntityRendererCache;
import meldexun.renderlib.api.ILoadable;
import meldexun.renderlib.api.ITileEntityRendererCache;
import meldexun.renderlib.config.RenderLibConfig;
import meldexun.renderlib.integration.Optifine;
import meldexun.renderlib.util.MutableAABB;
import meldexun.renderlib.util.timer.CPUTimer;
import meldexun.renderlib.util.timer.ITimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
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
	private final RaytracingEngine engine = new RaytracingEngine(EntityCullingConfig.cacheSize, (x, y, z) -> {
		return this.cachedBlockAccess.getBlockState(this.mutablePos.setPos(x, y, z)).isOpaqueCube();
	}, () -> Minecraft.getMinecraft().gameSettings.renderDistanceChunks);
	private double sleepOverhead = 0.0D;

	public final ITimer timer = new ITimer() {

		private final ITimer timer = new CPUTimer("CPU (Cull Async)", 100);
		private volatile String avgString = "0.0ms";
		private volatile String minString = "0.0ms";
		private volatile String maxString = "0.0ms";

		@Override
		public void update() {
			if (RenderLibConfig.showFrameTimes) {
				this.avgString = this.timer.avgString();
				this.minString = this.timer.minString();
				this.maxString = this.timer.maxString();
			}
			this.timer.update();
		}

		@Override
		public void stop() {
			this.timer.stop();
		}

		@Override
		public void start() {
			this.timer.start();
		}

		@Override
		public LongStream results() {
			return this.timer.results();
		}

		@Override
		public String getName() {
			return this.timer.getName();
		}

		@Override
		public String avgString() {
			return this.avgString;
		}

		@Override
		public String minString() {
			return this.minString;
		}

		@Override
		public String maxString() {
			return this.maxString;
		}

	};

	private boolean spectator;
	private double camX;
	private double camY;
	private double camZ;
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
			this.timer.update();
			this.timer.start();

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
					Vec3d cameraPosition = ActiveRenderInfo.getCameraPosition();
					this.camX = this.x + cameraPosition.x;
					this.camY = this.y + cameraPosition.y;
					this.camZ = this.z + cameraPosition.z;
					this.engine.setup(camX, camY, camZ);

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
			}
	
			this.timer.stop();

			double d = (System.nanoTime() - t) / 1_000_000.0D + this.sleepOverhead;
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
		if (Optifine.isOptifineDetected()) {
			((ICullable) entity).setShadowCulled(!this.checkEntityShadowVisibility(entity));
		}
	}

	private void updateTileEntityCullingState(TileEntity tileEntity) {
		if (!EntityCulling.useOpenGlBasedCulling()) {
			((ICullable) tileEntity).setCulled(!this.checkTileEntityVisibility(tileEntity));
		}
		if (Optifine.isOptifineDetected()) {
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
		if (EntityCulling.isHatsInstalled && Hats.isHat(entity)) {
			return false;
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

		if (!((IEntityRendererCache) entity).hasRenderer()) {
			return true;
		}
		if (!((ILoadable) entity).isChunkLoaded()) {
			return true;
		}
		MutableAABB aabb = ((IBoundingBoxCache) entity).getCachedBoundingBox();
		if (aabb.sizeX() > EntityCullingConfig.entity.skipHiddenEntityRenderingSize
				|| aabb.sizeY() > EntityCullingConfig.entity.skipHiddenEntityRenderingSize
				|| aabb.sizeZ() > EntityCullingConfig.entity.skipHiddenEntityRenderingSize) {
			return true;
		}
		if (!((ICullable) entity).canBeOcclusionCulled()) {
			return true;
		}

		return this.checkBox(aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ());
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

		if (!((ITileEntityRendererCache) tileEntity).hasRenderer()) {
			return true;
		}
		if (!((ILoadable) tileEntity).isChunkLoaded()) {
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

		return this.checkBox(aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ());
	}

	private boolean checkEntityShadowVisibility(Entity entity) {
		//if (!EntityCullingConfig.optifineShaderOptions.entityShadowsEnabled) {
		//	return false;
		//}
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

			if (!((IEntityRendererCache) entity).hasRenderer()) {
				return true;
			}
			if (!((ILoadable) entity).isChunkLoaded()) {
				return true;
			}
			if (!((ICullable) entity).canBeOcclusionCulled()) {
				return true;
			}
		}
		return engine.raytraceUncachedThreshold(entity.posX, entity.posY + entity.height * 0.5D, entity.posZ,
				EntityCullingConfig.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkTileEntityShadowVisibility(TileEntity tileEntity) {
		//if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsEnabled) {
		//	return false;
		//}
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

			if (!((ITileEntityRendererCache) tileEntity).hasRenderer()) {
				return true;
			}
			if (!((ILoadable) tileEntity).isChunkLoaded()) {
				return true;
			}
			if (!((ICullable) tileEntity).canBeOcclusionCulled()) {
				return true;
			}
		}
		BlockPos pos = tileEntity.getPos();
		return engine.raytraceUncachedThreshold(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
				EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (EntityCullingConfig.enableRaytraceCache) {
			return checkBoxCached(minX, minY, minZ, maxX, maxY, maxZ);
		} else {
			return checkBoxUncached(minX, minY, minZ, maxX, maxY, maxZ);
		}
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
					(y, z) -> engine.raytraceCachedThreshold(startX, y, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camX > endX) {
			if (IntUtil.anyMatch(
					startY, endY,
					startZ, endZ,
					(y, z) -> engine.raytraceCachedThreshold(endX, y, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}
		if (this.camY < startY) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					startZ, endZ,
					(x, z) -> engine.raytraceCachedThreshold(x, startY, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camY > endY) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					startZ, endZ,
					(x, z) -> engine.raytraceCachedThreshold(x, endY, z, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}
		if (this.camZ < startZ) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					this.camY < startY ? startY + 1 : startY, this.camY > endY ? endY - 1 : endY,
					(x, y) -> engine.raytraceCachedThreshold(x, y, startZ, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camZ > endZ) {
			if (IntUtil.anyMatch(
					this.camX < startX ? startX + 1 : startX, this.camX > endX ? endX - 1 : endX,
					this.camY < startY ? startY + 1 : startY, this.camY > endY ? endY - 1 : endY,
					(x, y) -> engine.raytraceCachedThreshold(x, y, endZ, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}

		return false;
	}

	private boolean checkBoxUncached(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (this.camX >= minX && this.camX <= maxX
				&& this.camY >= minY && this.camY <= maxY
				&& this.camZ >= minZ && this.camZ <= maxZ) {
			return true;
		}

		double deltaX = maxX - minX;
		double deltaY = maxY - minY;
		double deltaZ = maxZ - minZ;
		int stepsX = MathHelper.ceil(deltaX);
		int stepsY = MathHelper.ceil(deltaY);
		int stepsZ = MathHelper.ceil(deltaZ);
		double dx = deltaX / stepsX;
		double dy = deltaY / stepsY;
		double dz = deltaZ / stepsZ;

		if (this.camX < minX) {
			if (IntUtil.anyMatch(
					0, stepsY,
					0, stepsZ,
					(y, z) -> engine.raytraceUncachedThreshold(minX, minY + y * dy, minZ + z * dz, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camX > maxX) {
			if (IntUtil.anyMatch(
					0, stepsY,
					0, stepsZ,
					(y, z) -> engine.raytraceUncachedThreshold(maxX, minY + y * dy, minZ + z * dz, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}
		if (this.camY < minY) {
			if (IntUtil.anyMatch(
					this.camX < minX ? 1 : 0, this.camX > maxX ? stepsX - 1 : stepsX,
					0, stepsZ,
					(x, z) -> engine.raytraceUncachedThreshold(minX + x * dx, minY, minZ + z * dz, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camY > maxY) {
			if (IntUtil.anyMatch(
					this.camX < minX ? 1 : 0, this.camX > maxX ? stepsX - 1 : stepsX,
					0, stepsZ,
					(x, z) -> engine.raytraceUncachedThreshold(minX + x * dx, maxY, minZ + z * dz, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}
		if (this.camZ < minZ) {
			if (IntUtil.anyMatch(
					this.camX < minX ? 1 : 0, this.camX > maxX ? stepsX - 1 : stepsX,
					this.camY < minY ? 1 : 0, this.camY > maxX ? stepsY - 1 : stepsY,
					(x, y) -> engine.raytraceUncachedThreshold(minX + x * dx, minY + y * dy, minZ, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		} else if (this.camZ > maxZ) {
			if (IntUtil.anyMatch(
					this.camX < minX ? 1 : 0, this.camX > maxX ? stepsX - 1 : stepsX,
					this.camY < minY ? 1 : 0, this.camY > maxX ? stepsY - 1 : stepsY,
					(x, y) -> engine.raytraceUncachedThreshold(minX + x * dx, minY + y * dy, maxZ, EntityCullingConfig.raytraceThreshold))) {
				return true;
			}
		}

		return false;
	}

}
