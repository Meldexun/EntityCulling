package meldexun.entityculling.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.mojang.math.Matrix4f;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.raytraceutil.RayTracingCache;
import meldexun.raytraceutil.RayTracingEngine;
import meldexun.raytraceutil.RayTracingEngine.MutableRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class CullingThread extends Thread {

	private static final Set<ResourceLocation> ENTITY_BLACKLIST = new HashSet<>();
	private static final Set<ResourceLocation> TILE_ENTITY_BLACKLIST = new HashSet<>();

	private final CachedBlockReader cachedBlockReader = new CachedBlockReader();
	private final MutableBlockPos mutablePos = new MutableBlockPos();
	private final RayTracingEngine engine = new RayTracingEngine((x, y, z) -> {
		this.mutablePos.set(x, y, z);
		return this.cachedBlockReader.getBlockState(this.mutablePos).isSolidRender(this.cachedBlockReader, this.mutablePos);
	});
	// 0=not cached, 1=blocked, 2=visible
	private final RayTracingCache cache = new RayTracingCache(EntityCullingConfig.CLIENT_CONFIG.cacheSize.get());
	private final MutableRayTraceResult mutableRayTraceResult = new MutableRayTraceResult();

	// input
	public double camX;
	public double camY;
	public double camZ;
	public Matrix4f projection = new Matrix4f();
	public Matrix4f view = new Matrix4f();

	private int camBlockX;
	private int camBlockY;
	private int camBlockZ;
	private Frustum frustum;

	private double sleepOverhead = 0.0D;

	// debug
	private int counter;
	public long[] time = new long[100];

	public CullingThread() {
		super();
		this.setName("Culling Thread");
		this.setDaemon(true);
	}

	public static void updateBlacklists() {
		ENTITY_BLACKLIST.clear();
		TILE_ENTITY_BLACKLIST.clear();

		for (String s : EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingBlacklist.get()) {
			ResourceLocation rs = new ResourceLocation(s);
			ForgeRegistries.ENTITIES.containsKey(rs);
			ENTITY_BLACKLIST.add(rs);
		}

		for (String s : EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingBlacklist.get()) {
			ResourceLocation rs = new ResourceLocation(s);
			ForgeRegistries.BLOCK_ENTITIES.containsKey(rs);
			TILE_ENTITY_BLACKLIST.add(rs);
		}
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getInstance();

		while (true) {
			long t = System.nanoTime();
			try {
				this.cachedBlockReader.setupCache(mc.level);

				if (mc.level != null && mc.getCameraEntity() != null) {
					this.frustum = new Frustum(this.view, this.projection);
					this.frustum.prepare(this.camX, this.camY, this.camZ);
					this.camBlockX = Mth.floor(this.camX);
					this.camBlockY = Mth.floor(this.camY);
					this.camBlockZ = Mth.floor(this.camZ);

					Iterator<Entity> entityIterator = mc.level.entitiesForRendering().iterator();
					while (entityIterator.hasNext()) {
						try {
							Entity entity = entityIterator.next();
							this.updateEntityCullingState(entity);
						} catch (Exception e) {
							// ignore
							break;
						}
					}

					ClientChunkCache chunkSource = mc.level.getChunkSource();
					for (int i = 0; i < chunkSource.storage.chunks.length(); i++) {
						LevelChunk chunk = chunkSource.storage.chunks.get(i);
						if (chunk == null) {
							continue;
						}
						Iterator<BlockEntity> tileEntityIterator = chunk.getBlockEntities().values().iterator();
						while (tileEntityIterator.hasNext()) {
							try {
								BlockEntity tileEntity = tileEntityIterator.next();
								this.updateTileEntityCullingState(tileEntity);
							} catch (Exception e) {
								// ignore
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				// ignore
			} finally {
				this.cachedBlockReader.clearCache();
				this.cache.clearCache();
			}

			t = System.nanoTime() - t;

			this.time[this.counter] = t;
			this.counter = (this.counter + 1) % this.time.length;

			double d = t / 1_000_000.0D + this.sleepOverhead;
			this.sleepOverhead = d % 1.0D;
			long sleepTime = 10 - (long) d;
			if (sleepTime > 0) {
				/*
				 * try {
				 * Thread.sleep(sleepTime);
				 * } catch (InterruptedException e) {
				 * Thread.currentThread().interrupt();
				 * }
				 */
			}
		}
	}

	private void updateEntityCullingState(Entity entity) {
		((ICullable) entity).setCulled(!this.checkEntityVisibility(entity));
		if (EntityCulling.IS_OPTIFINE_DETECTED) {
			((ICullable) entity).setShadowCulled(!this.checkEntityShadowVisibility(entity));
		}
	}

	private void updateTileEntityCullingState(BlockEntity tileEntity) {
		((ICullable) tileEntity).setCulled(!this.checkTileEntityVisibility(tileEntity));
		if (EntityCulling.IS_OPTIFINE_DETECTED) {
			((ICullable) tileEntity).setShadowCulled(!this.checkTileEntityShadowVisibility(tileEntity));
		}
	}

	private boolean checkEntityVisibility(Entity entity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRendering.get()) {
			return true;
		}

		// check if entity is boss (isNonBoss in forge mappings)
		if (!entity.canChangeDimensions()) {
			return true;
		}

		if (entity.getBbWidth() > EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get()
				|| entity.getBbHeight() > EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get()) {
			return true;
		}

		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(entity.getType().getRegistryName())) {
			return true;
		}

		double minX, minY, minZ, maxX, maxY, maxZ;
		{
			AABB aabb = entity.getBoundingBoxForCulling();

			if (aabb.hasNaN()) {
				minX = entity.getX() - 2.0D;
				minY = entity.getY() - 2.0D;
				minZ = entity.getZ() - 2.0D;
				maxX = entity.getX() + 2.0D;
				maxY = entity.getY() + 2.0D;
				maxZ = entity.getZ() + 2.0D;
			} else {
				minX = aabb.minX - 0.5D;
				minY = aabb.minY - 0.5D;
				minZ = aabb.minZ - 0.5D;
				maxX = aabb.maxX + 0.5D;
				maxY = aabb.maxY + 0.5D;
				maxZ = aabb.maxZ + 0.5D;
			}
		}

		if (!entity.shouldRender(this.camX, this.camY, this.camZ)) {
			return true;
		}

		if (!this.frustum.cubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that entities outside of the fov don't get rendered and thus there is no need to ray trace if they are
			// visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (this.checkVisibility(this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, 1.0D)) {
			return true;
		}

		return this.checkBoundingBoxVisibility(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean checkTileEntityVisibility(BlockEntity tileEntity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRendering.get()) {
			return true;
		}

		AABB aabb = ((IBoundingBoxCache) tileEntity).getOrCacheBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()
				|| aabb.maxY - aabb.minY > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()
				|| aabb.maxZ - aabb.minZ > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()) {
			return true;
		}

		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(tileEntity.getType().getRegistryName())) {
			return true;
		}

		double minX, minY, minZ, maxX, maxY, maxZ;
		{
			minX = aabb.minX;
			minY = aabb.minY;
			minZ = aabb.minZ;
			maxX = aabb.maxX;
			maxY = aabb.maxY;
			maxZ = aabb.maxZ;
		}

		BlockPos pos = tileEntity.getBlockPos();
		if (pos.distSqr(this.camX, this.camY, this.camZ, true) >= tileEntity.getViewDistance() * tileEntity.getViewDistance()) {
			return true;
		}

		if (!this.frustum.cubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that tile entities outside of the fov don't get rendered and thus there is no need to ray trace if they are
			// visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (this.checkVisibility(this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, 1.0D)) {
			return true;
		}

		return this.checkBoundingBoxVisibility(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean checkEntityShadowVisibility(Entity entity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsEnabled.get()) {
			return false;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRendering.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCulling.get()) {
			return true;
		}

		if (!((ICullable) entity).isCulled()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveMode.get()) {
			return false;
		}

		// check if entity is boss (isNonBoss in forge mappings)
		if (!entity.canChangeDimensions()) {
			return true;
		}

		if (entity.getBbWidth() >= EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get()
				|| entity.getBbHeight() >= EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get()) {
			return true;
		}

		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(entity.getType().getRegistryName())) {
			return true;
		}

		return this.checkVisibility(this.camX, this.camY, this.camZ, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
				EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff.get());
	}

	private boolean checkTileEntityShadowVisibility(BlockEntity tileEntity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsEnabled.get()) {
			return false;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRendering.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCulling.get()) {
			return true;
		}

		if (!((ICullable) tileEntity).isCulled()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode.get()) {
			return false;
		}

		AABB aabb = ((IBoundingBoxCache) tileEntity).getOrCacheBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()
				|| aabb.maxY - aabb.minY > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()
				|| aabb.maxZ - aabb.minZ > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()) {
			return true;
		}

		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(tileEntity.getType().getRegistryName())) {
			return true;
		}

		BlockPos pos = tileEntity.getBlockPos();
		return this.checkVisibility(this.camX, this.camY, this.camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
				EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff.get());
	}

	private boolean checkBoundingBoxVisibility(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		int startX = Mth.floor(minX);
		int startY = Mth.floor(minY);
		int startZ = Mth.floor(minZ);
		int endX = Mth.ceil(maxX);
		int endY = Mth.ceil(maxY);
		int endZ = Mth.ceil(maxZ);

		if ((this.camX >= startX && this.camX <= endX) && (this.camY >= startY && this.camY >= endY) && (this.camZ >= startZ && this.camZ >= endZ)) {
			return true;
		}

		double threshold = EntityCullingConfig.CLIENT_CONFIG.raytraceThreshold.get();

		if (this.camX < startX) {
			int x = startX;
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(x, y, z, threshold)) {
						return true;
					}
				}
			}
		} else if (this.camX > endX) {
			int x = endX;
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(x, y, z, threshold)) {
						return true;
					}
				}
			}
		}
		if (this.camY < startY) {
			int y = startY;
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(x, y, z, threshold)) {
						return true;
					}
				}
			}
		} else if (this.camY > endY) {
			int y = endY;
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(x, y, z, threshold)) {
						return true;
					}
				}
			}
		}
		if (this.camZ < startZ) {
			int z = startZ;
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					if (this.checkVisibilityCached(x, y, z, threshold)) {
						return true;
					}
				}
			}
		} else if (this.camZ > endZ) {
			int z = endZ;
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					if (this.checkVisibilityCached(x, y, z, threshold)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean checkVisibilityCached(int endX, int endY, int endZ, double threshold) {
		return this.cache.getOrSetCachedValue(endX - this.camBlockX, endY - this.camBlockY, endZ - this.camBlockZ,
				() -> this.checkVisibility(this.camX, this.camY, this.camZ, endX, endY, endZ, threshold) ? 2 : 1) == 2;
	}

	private boolean checkVisibility(double startX, double startY, double startZ, double endX, double endY, double endZ, double threshold) {
		return this.engine.rayTraceBlocks(startX, startY, startZ, endX, endY, endZ, true, threshold, this.mutableRayTraceResult) == null;
	}

}
