package meldexun.entityculling.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.integration.CubicChunks;
import meldexun.raytraceutil.RayTracingCache;
import meldexun.raytraceutil.RayTracingEngine;
import meldexun.raytraceutil.RayTracingEngine.MutableRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.Loader;

public class CullingThread extends Thread {

	private static final Set<Class<? extends Entity>> ENTITY_BLACKLIST = new HashSet<>();
	private static final Set<Class<? extends TileEntity>> TILE_ENTITY_BLACKLIST = new HashSet<>();

	private final MutableBlockPos mutablePos = new MutableBlockPos();
	private boolean blockStorageCached;
	private int cachedX;
	private int cachedY;
	private int cachedZ;
	private ExtendedBlockStorage cachedBlockStorage;
	private final RayTracingEngine engine = new RayTracingEngine((x, y, z) -> {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.world.isOutsideBuildHeight(mutablePos.setPos(x, y, z))) {
			return false;
		}
		if (!blockStorageCached || (x >> 4) != cachedX || (y >> 4) != cachedY || (z >> 4) != cachedZ) {
			blockStorageCached = true;
			cachedX = x >> 4;
			cachedY = y >> 4;
			cachedZ = z >> 4;
			if (!Loader.isModLoaded("cubicChunks")) {
				cachedBlockStorage = mc.world.getChunk(x >> 4, z >> 4).getBlockStorageArray()[y >> 4];
			} else {
				cachedBlockStorage = CubicChunks.getBlockStorage(mc.world, x >> 4, y >> 4, z >> 4);
			}
		}
		return cachedBlockStorage.get(x & 15, y & 15, z & 15).isOpaqueCube();
	});
	// 0=not cached, 1=blocked, 2=visible
	private final RayTracingCache cache = new RayTracingCache(EntityCullingConfig.cacheSize);
	private final MutableRayTraceResult mutableRayTraceResult = new MutableRayTraceResult();
	private double sleepOverhead = 0.0D;

	/** debug */
	private int counter = 0;
	public long[] time = new long[100];
	private boolean result = false;
	private static List<double[]> privateDebugRayList = new ArrayList<>();
	public static List<double[]> publicDebugRayList = new ArrayList<>();

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

	public static void updateBlacklists() {
		ENTITY_BLACKLIST.clear();
		TILE_ENTITY_BLACKLIST.clear();

		for (String s : EntityCullingConfig.skipHiddenEntityRenderingBlacklist) {
			Class<? extends Entity> entityClass = EntityList.getClassFromName(s);
			if (entityClass != null) {
				ENTITY_BLACKLIST.add(entityClass);
			}
		}

		for (String s : EntityCullingConfig.skipHiddenTileEntityRenderingBlacklist) {
			Class<? extends TileEntity> tileEntityClass = TileEntity.REGISTRY.getObject(new ResourceLocation(s));
			if (tileEntityClass != null) {
				TILE_ENTITY_BLACKLIST.add(tileEntityClass);
			}
		}
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();

		while (true) {
			long t = System.nanoTime();
			try {
				this.cache.clearCache();
				privateDebugRayList.clear();

				if (mc.world != null && mc.getRenderViewEntity() != null) {
					this.spectator = mc.player.isSpectator();
					Entity renderViewEntity = mc.getRenderViewEntity();
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

					Iterator<Entity> entityIterator = mc.world.loadedEntityList.iterator();
					while (entityIterator.hasNext()) {
						try {
							Entity entity = entityIterator.next();
							this.updateEntityCullingState(entity);
						} catch (Exception e) {
							// ignore
							break;
						}
					}

					Iterator<TileEntity> tileEntityIterator = mc.world.loadedTileEntityList.iterator();
					while (tileEntityIterator.hasNext()) {
						try {
							TileEntity tileEntity = tileEntityIterator.next();
							this.updateTileEntityCullingState(tileEntity);
						} catch (Exception e) {
							// ignore
							break;
						}
					}
				}

				synchronized (CullingThread.class) {
					List<double[]> temp = publicDebugRayList;
					publicDebugRayList = privateDebugRayList;
					privateDebugRayList = temp;
				}
			} catch (Exception e) {
				// ignore
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
		((ICullable) entity).setCulled(!this.checkEntityVisibility(entity));
		if (EntityCullingClassTransformer.OPTIFINE_DETECTED) {
			((ICullable) entity).setShadowCulled(!this.checkEntityShadowVisibility(entity));
		}
	}

	private void updateTileEntityCullingState(TileEntity tileEntity) {
		((ICullable) tileEntity).setCulled(!this.checkTileEntityVisibility(tileEntity));
		if (EntityCullingClassTransformer.OPTIFINE_DETECTED) {
			((ICullable) tileEntity).setShadowCulled(!this.checkTileEntityShadowVisibility(tileEntity));
		}
	}

	private boolean checkEntityVisibility(Entity entity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (!EntityCullingConfig.skipHiddenEntityRendering) {
			return true;
		}

		if (EntityCullingConfig.disabledInSpectator && this.spectator) {
			return true;
		}

		if (!entity.isNonBoss()) {
			return true;
		}

		if (entity.width > EntityCullingConfig.skipHiddenEntityRenderingSize || entity.height > EntityCullingConfig.skipHiddenEntityRenderingSize) {
			return true;
		}

		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(entity.getClass())) {
			return true;
		}

		double minX, minY, minZ, maxX, maxY, maxZ;
		{
			AxisAlignedBB aabb = entity.getRenderBoundingBox();

			if (aabb.hasNaN()) {
				minX = entity.posX - 2.0D;
				minY = entity.posY - 2.0D;
				minZ = entity.posZ - 2.0D;
				maxX = entity.posX + 2.0D;
				maxY = entity.posY + 2.0D;
				maxZ = entity.posZ + 2.0D;
			} else {
				minX = aabb.minX - 0.5D;
				minY = aabb.minY - 0.5D;
				minZ = aabb.minZ - 0.5D;
				maxX = aabb.maxX + 0.5D;
				maxY = aabb.maxY + 0.5D;
				maxZ = aabb.maxZ + 0.5D;
			}
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
			privateDebugRayList.add(new double[] {
					(minX + maxX) * 0.5D,
					(minY + maxY) * 0.5D,
					(minZ + maxZ) * 0.5D,
					0 });
		}
		this.result = this.checkVisibility(entity.world, this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D,
				EntityCullingConfig.raytraceThreshold);
		this.result |= this.checkBoundingBoxVisibility(entity.world, minX, minY, minZ, maxX, maxY, maxZ);

		return this.result;
	}

	private boolean checkTileEntityVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (!EntityCullingConfig.skipHiddenTileEntityRendering) {
			return true;
		}

		if (EntityCullingConfig.disabledInSpectator && this.spectator) {
			return true;
		}

		AxisAlignedBB aabb = ((IBoundingBoxCache) tileEntity).getOrCacheBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.skipHiddenTileEntityRenderingSize
				|| aabb.maxY - aabb.minY > EntityCullingConfig.skipHiddenTileEntityRenderingSize
				|| aabb.maxZ - aabb.minZ > EntityCullingConfig.skipHiddenTileEntityRenderingSize) {
			return true;
		}

		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(tileEntity.getClass())) {
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

		if (tileEntity.getDistanceSq(this.x, this.y, this.z) >= tileEntity.getMaxRenderDistanceSquared()) {
			return true;
		}

		if (!this.frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that tile entities outside of the fov don't get rendered and thus there is no need to ray trace if they are
			// visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (EntityCullingConfig.debugRenderBoxes) {
			privateDebugRayList.add(new double[] {
					(minX + maxX) * 0.5D,
					(minY + maxY) * 0.5D,
					(minZ + maxZ) * 0.5D,
					0 });
		}
		this.result = this.checkVisibility(tileEntity.getWorld(), this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D,
				(minZ + maxZ) * 0.5D, EntityCullingConfig.raytraceThreshold);
		this.result |= this.checkBoundingBoxVisibility(tileEntity.getWorld(), minX, minY, minZ, maxX, maxY, maxZ);

		return this.result;
	}

	private boolean checkEntityShadowVisibility(Entity entity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.entityShadowsEnabled) {
			return false;
		}

		if (!EntityCullingConfig.skipHiddenEntityRendering) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.entityShadowsCulling) {
			return true;
		}

		if (!((ICullable) entity).isCulled()) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.entityShadowsCullingLessAggressiveMode) {
			return false;
		}

		if (!entity.isNonBoss()) {
			return true;
		}

		if (entity.width >= EntityCullingConfig.skipHiddenEntityRenderingSize || entity.height >= EntityCullingConfig.skipHiddenEntityRenderingSize) {
			return true;
		}

		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(entity.getClass())) {
			return true;
		}

		return this.checkVisibility(entity.world, this.camX, this.camY, this.camZ, entity.posX, entity.posY + entity.height * 0.5D, entity.posZ,
				EntityCullingConfig.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkTileEntityShadowVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsEnabled) {
			return false;
		}

		if (!EntityCullingConfig.skipHiddenTileEntityRendering) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCulling) {
			return true;
		}

		if (!((ICullable) tileEntity).isCulled()) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode) {
			return false;
		}

		AxisAlignedBB aabb = ((IBoundingBoxCache) tileEntity).getOrCacheBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.skipHiddenTileEntityRenderingSize
				|| aabb.maxY - aabb.minY > EntityCullingConfig.skipHiddenTileEntityRenderingSize
				|| aabb.maxZ - aabb.minZ > EntityCullingConfig.skipHiddenTileEntityRenderingSize) {
			return true;
		}

		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(tileEntity.getClass())) {
			return true;
		}

		BlockPos pos = tileEntity.getPos();
		return this.checkVisibility(tileEntity.getWorld(), this.camX, this.camY, this.camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
				EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkBoundingBoxVisibility(World world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (EntityCullingConfig.cachelessMode) {
			return this.checkBoundingBoxVisibility2(world, minX, minY, minZ, maxX, maxY, maxZ);
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
			int x = startX;
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibilityCached(world, x, y, z)) {
						this.result = true;
					}
				}
			}
		} else if (this.camX > endX) {
			int x = endX;
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibilityCached(world, x, y, z)) {
						this.result = true;
					}
				}
			}
		}
		if (this.camY < startY) {
			int y = startY;
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibilityCached(world, x, y, z)) {
						this.result = true;
					}
				}
			}
		} else if (this.camY > endY) {
			int y = endY;
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibilityCached(world, x, y, z)) {
						this.result = true;
					}
				}
			}
		}
		if (this.camZ < startZ) {
			int z = startZ;
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibilityCached(world, x, y, z)) {
						this.result = true;
					}
				}
			}
		} else if (this.camZ > endZ) {
			int z = endZ;
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibilityCached(world, x, y, z)) {
						this.result = true;
					}
				}
			}
		}

		return this.result;
	}

	private boolean checkBoundingBoxVisibility2(World world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (this.camX >= minX && this.camX <= maxX && this.camY >= minY && this.camY <= maxY && this.camZ >= minZ && this.camZ <= maxZ) {
			this.result = true;
		}

		int stepsX = MathHelper.ceil(maxX - minX);
		int stepsY = MathHelper.ceil(maxY - minY);
		int stepsZ = MathHelper.ceil(maxZ - minZ);

		if (this.camX < minX) {
			double x = minX;
			for (int iy = 0; iy <= stepsY; iy++) {
				double y = minY + (maxY - minY) * iy / stepsY;
				for (int iz = 0; iz <= stepsZ; iz++) {
					double z = minZ + (maxZ - minZ) * iz / stepsZ;
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibility(world, this.camX, this.camY, this.camZ, x, y, z, EntityCullingConfig.raytraceThreshold)) {
						this.result = true;
					}
				}
			}
		} else if (this.camX > maxX) {
			double x = maxX;
			for (int iy = 0; iy <= stepsY; iy++) {
				double y = minY + (maxY - minY) * iy / stepsY;
				for (int iz = 0; iz <= stepsZ; iz++) {
					double z = minZ + (maxZ - minZ) * iz / stepsZ;
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibility(world, this.camX, this.camY, this.camZ, x, y, z, EntityCullingConfig.raytraceThreshold)) {
						this.result = true;
					}
				}
			}
		}

		if (this.camY < minY) {
			double y = minY;
			for (int ix = 0; ix <= stepsX; ix++) {
				double x = minX + (maxX - minX) * ix / stepsX;
				for (int iz = 0; iz <= stepsZ; iz++) {
					double z = minZ + (maxZ - minZ) * iz / stepsZ;
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibility(world, this.camX, this.camY, this.camZ, x, y, z, EntityCullingConfig.raytraceThreshold)) {
						this.result = true;
					}
				}
			}
		} else if (this.camY > maxY) {
			double y = maxY;
			for (int ix = 0; ix <= stepsX; ix++) {
				double x = minX + (maxX - minX) * ix / stepsX;
				for (int iz = 0; iz <= stepsZ; iz++) {
					double z = minZ + (maxZ - minZ) * iz / stepsZ;
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibility(world, this.camX, this.camY, this.camZ, x, y, z, EntityCullingConfig.raytraceThreshold)) {
						this.result = true;
					}
				}
			}
		}

		if (this.camZ < minZ) {
			double z = minZ;
			for (int ix = 0; ix <= stepsX; ix++) {
				double x = minX + (maxX - minX) * ix / stepsX;
				for (int iy = 0; iy <= stepsY; iy++) {
					double y = minY + (maxY - minY) * iy / stepsY;
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibility(world, this.camX, this.camY, this.camZ, x, y, z, EntityCullingConfig.raytraceThreshold)) {
						this.result = true;
					}
				}
			}
		} else if (this.camZ > maxZ) {
			double z = maxZ;
			for (int ix = 0; ix <= stepsX; ix++) {
				double x = minX + (maxX - minX) * ix / stepsX;
				for (int iy = 0; iy <= stepsY; iy++) {
					double y = minY + (maxY - minY) * iy / stepsY;
					if (EntityCullingConfig.debugRenderBoxes) {
						privateDebugRayList.add(new double[] {
								x,
								y,
								z,
								this.result ? 1 : 0 });
					}
					if (!this.result && this.checkVisibility(world, this.camX, this.camY, this.camZ, x, y, z, EntityCullingConfig.raytraceThreshold)) {
						this.result = true;
					}
				}
			}
		}

		return this.result;
	}

	private boolean checkVisibilityCached(World world, int endX, int endY, int endZ) {
		boolean test = true;
		if (test) {
			return this.cache.getOrSetCachedValue(endX - this.camBlockX, endY - this.camBlockY, endZ - this.camBlockZ,
					() -> this.checkVisibility(world, this.camX, this.camY, this.camZ, endX, endY, endZ, EntityCullingConfig.raytraceThreshold) ? 2 : 1) == 2;
		}
		int cachedValue = this.cache.getCachedValue(endX - this.camBlockX, endY - this.camBlockY, endZ - this.camBlockZ);
		if (cachedValue > 0) {
			return cachedValue == 2;
		}

		boolean flag = this.checkVisibility(world, this.camX, this.camY, this.camZ, endX, endY, endZ, EntityCullingConfig.raytraceThreshold);

		if (cachedValue != -1) {
			this.cache.setCachedValue(endX - this.camBlockX, endY - this.camBlockY, endZ - this.camBlockZ, flag ? 2 : 1);
		}

		return flag;
	}

	private boolean checkVisibility(World world, double startX, double startY, double startZ, double endX, double endY, double endZ, double maxDiff) {
		MutableRayTraceResult rayTraceResult = engine.rayTraceBlocks(startX, startY, startZ, endX, endY, endZ, true, maxDiff,
				this.mutableRayTraceResult);
		return rayTraceResult == null;
	}

}
