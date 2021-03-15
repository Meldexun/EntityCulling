package meldexun.entityculling;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import meldexun.entityculling.RayTracingEngine.MutableRayTraceResult;
import meldexun.entityculling.plugin.EntityCullingTransformer;
import meldexun.entityculling.reflection.ReflectionField;
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
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;

public class CullingThread extends Thread {

	private static final ReflectionField<RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>>> FIELD_REGISTRY = new ReflectionField<>(TileEntity.class, "field_190562_f", "REGISTRY");
	private static final Set<Class<? extends Entity>> ENTITY_BLACKLIST = new HashSet<>();
	private static final Set<Class<? extends TileEntity>> TILE_ENTITY_BLACKLIST = new HashSet<>();

	private static final ReflectionField<ClippingHelperImpl> FIELD_INSTANCE = new ReflectionField<>(ClippingHelperImpl.class, "field_78563_e", "instance");
	private final MutableRayTraceResult mutableRayTraceResult = new MutableRayTraceResult();
	// 0=not cached, 1=blocked, 2=visible
	private final RayTracingCache cache = new RayTracingCache(EntityCullingConfig.cacheSize);
	private double sleepOverhead = 0.0D;
	/** debug */
	public long[] time = new long[10];
	private Frustum frustum;
	private double camX;
	private double camY;
	private double camZ;
	private int camBlockX;
	private int camBlockY;
	private int camBlockZ;

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

		RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>> tileEntityRegistry = FIELD_REGISTRY.get(null);
		for (String s : EntityCullingConfig.skipHiddenTileEntityRenderingBlacklist) {
			Class<? extends TileEntity> tileEntityClass = tileEntityRegistry.getObject(new ResourceLocation(s));
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
				RayTracingEngine.resetCache();
				this.cache.clearCache();

				if (mc.world != null && mc.getRenderViewEntity() != null) {
					Entity renderViewEntity = mc.getRenderViewEntity();
					float partialTicks = mc.getRenderPartialTicks();
					double x = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
					double y = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
					double z = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
					this.frustum = new Frustum(FIELD_INSTANCE.get(null));
					this.frustum.setPosition(x, y, z);
					if (EntityCullingConfig.debug) {
						this.camX = x;
						this.camY = y + renderViewEntity.getEyeHeight();
						this.camZ = z;
					} else {
						Vec3d cameraPosition = ActiveRenderInfo.getCameraPosition();
						this.camX = x + cameraPosition.x;
						this.camY = y + cameraPosition.y;
						this.camZ = z + cameraPosition.z;
					}
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
			} catch (Exception e) {
				// ignore
			}

			t = System.nanoTime() - t;

			if (EntityCullingConfig.debug) {
				System.arraycopy(this.time, 0, this.time, 1, this.time.length - 1);
				this.time[0] = t;
			}

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
		((ICullable) entity).setCulledFast(!this.checkEntityVisibility(entity));
		if (EntityCullingTransformer.IS_OPTIFINE_DETECTED) {
			((ICullable) entity).setCulledShadowPass(!this.checkEntityShadowVisibility(entity));
		}
	}

	private void updateTileEntityCullingState(TileEntity tileEntity) {
		((ICullable) tileEntity).setCulledFast(!this.checkTileEntityVisibility(tileEntity));
		if (EntityCullingTransformer.IS_OPTIFINE_DETECTED) {
			((ICullable) tileEntity).setCulledShadowPass(!this.checkTileEntityShadowVisibility(tileEntity));
		}
	}

	private boolean checkEntityVisibility(Entity entity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (!EntityCullingConfig.skipHiddenEntityRendering) {
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

		if (!this.frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that entities outside of the fov don't get rendered and thus there is no need to ray trace if they are visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (this.checkVisibility(entity.world, this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, 1.0D)) {
			return true;
		}

		return this.checkBoundingBoxVisibility(entity.world, minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean checkTileEntityVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (!EntityCullingConfig.skipHiddenTileEntityRendering) {
			return true;
		}

		AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.skipHiddenTileEntityRenderingSize || aabb.maxY - aabb.minY > EntityCullingConfig.skipHiddenTileEntityRenderingSize || aabb.maxZ - aabb.minZ > EntityCullingConfig.skipHiddenTileEntityRenderingSize) {
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

		if (!this.frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that tile entities outside of the fov don't get rendered and thus there is no need to ray trace if they are visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (this.checkVisibility(tileEntity.getWorld(), this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, 1.0D)) {
			return true;
		}

		return this.checkBoundingBoxVisibility(tileEntity.getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean checkEntityShadowVisibility(Entity entity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (EntityCullingConfig.optifineShaderOptions.entityShadowsDisabled) {
			return false;
		}

		if (!EntityCullingConfig.skipHiddenEntityRendering) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.entityShadowsCulling) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.entityShadowsCullingLessAggressiveMode) {
			return !((ICullable) entity).isCulledFast();
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

		return this.checkVisibility(entity.world, this.camX, this.camY, this.camZ, entity.posX, entity.posY + entity.height * 0.5D, entity.posZ, EntityCullingConfig.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkTileEntityShadowVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (EntityCullingConfig.optifineShaderOptions.tileEntityShadowsDisabled) {
			return false;
		}

		if (!EntityCullingConfig.skipHiddenTileEntityRendering) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCulling) {
			return true;
		}

		if (!EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode) {
			return !((ICullable) tileEntity).isCulledFast();
		}

		AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.skipHiddenTileEntityRenderingSize || aabb.maxY - aabb.minY > EntityCullingConfig.skipHiddenTileEntityRenderingSize || aabb.maxZ - aabb.minZ > EntityCullingConfig.skipHiddenTileEntityRenderingSize) {
			return true;
		}

		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(tileEntity.getClass())) {
			return true;
		}

		BlockPos pos = tileEntity.getPos();
		return this.checkVisibility(tileEntity.getWorld(), this.camX, this.camY, this.camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, EntityCullingConfig.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff);
	}

	private boolean checkBoundingBoxVisibility(World world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		int startX = MathHelper.floor(minX);
		int startY = MathHelper.floor(minY);
		int startZ = MathHelper.floor(minZ);
		int endX = MathHelper.ceil(maxX);
		int endY = MathHelper.ceil(maxY);
		int endZ = MathHelper.ceil(maxZ);

		if (this.camX < startX) {
			int x = startX;
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(world, x, y, z)) {
						return true;
					}
				}
			}
		} else if (this.camX > endX) {
			int x = endX;
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(world, x, y, z)) {
						return true;
					}
				}
			}
		}
		if (this.camY < startY) {
			int y = startY;
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(world, x, y, z)) {
						return true;
					}
				}
			}
		} else if (this.camY > endY) {
			int y = endY;
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					if (this.checkVisibilityCached(world, x, y, z)) {
						return true;
					}
				}
			}
		}
		if (this.camZ < startZ) {
			int z = startZ;
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					if (this.checkVisibilityCached(world, x, y, z)) {
						return true;
					}
				}
			}
		} else if (this.camZ > endZ) {
			int z = endZ;
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					if (this.checkVisibilityCached(world, x, y, z)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean checkVisibilityCached(World world, int endX, int endY, int endZ) {
		int cacheX = endX - this.camBlockX + this.cache.radiusBlocks;
		int cacheY = endY - this.camBlockY + this.cache.radiusBlocks;
		int cacheZ = endZ - this.camBlockZ + this.cache.radiusBlocks;
		RayTracingCache.RayTracingCacheChunk chunk = this.cache.getChunk(cacheX >> 4, cacheY >> 4, cacheZ >> 4);
		if (chunk != null) {
			int cachedValue = chunk.getCachedValue(cacheX & 15, cacheY & 15, cacheZ & 15);
			if (cachedValue > 0) {
				return cachedValue >> 1 == 1;
			}
		}

		boolean flag = this.checkVisibility(world, this.camX, this.camY, this.camZ, endX, endY, endZ, 1.0D);

		if (chunk != null) {
			chunk.setCachedValue(cacheX & 15, cacheY & 15, cacheZ & 15, flag ? 2 : 1);
		}

		return flag;
	}

	private boolean checkVisibility(World world, double startX, double startY, double startZ, double endX, double endY, double endZ, double maxDiff) {
		MutableRayTraceResult rayTraceResult = RayTracingEngine.rayTraceBlocks(world, startX, startY, startZ, endX, endY, endZ, true, maxDiff, this.mutableRayTraceResult);
		return rayTraceResult == null;
	}

}
