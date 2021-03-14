package meldexun.entityculling;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import meldexun.entityculling.RayTracingEngine.MutableRayTraceResult;
import meldexun.entityculling.reflection.ReflectionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class CullingThread extends Thread {

	private static final Set<ResourceLocation> ENTITY_BLACKLIST = new HashSet<>();
	private static final Set<ResourceLocation> TILE_ENTITY_BLACKLIST = new HashSet<>();

	private static final ReflectionMethod<Boolean> METHOD_IS_BOX_IN_FRUSTUM = new ReflectionMethod<>(ClippingHelper.class, "func_228953_a_", "cubeInFrustum", Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);
	private final MutableRayTraceResult mutableRayTraceResult = new MutableRayTraceResult();
	private final RayTracingCache cache = new RayTracingCache(16);
	private double sleepOverhead = 0.0D;
	/** debug */
	public long[] time = new long[10];
	private ClippingHelper frustum;
	private int camBlockX;
	private int camBlockY;
	private int camBlockZ;

	public double camX;
	public double camY;
	public double camZ;
	public Matrix4f matrix = new Matrix4f();
	public Matrix4f projection = new Matrix4f();

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
			ForgeRegistries.TILE_ENTITIES.containsKey(rs);
			TILE_ENTITY_BLACKLIST.add(rs);
		}
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getInstance();

		while (true) {
			long t = System.nanoTime();
			try {
				RayTracingEngine.resetCache();
				this.cache.clearCache();

				if (mc.level != null && mc.getCameraEntity() != null) {
					this.frustum = new ClippingHelper(this.matrix, this.projection);
					this.frustum.prepare(this.camX, this.camY, this.camZ);
					this.camBlockX = MathHelper.floor(this.camX);
					this.camBlockY = MathHelper.floor(this.camY);
					this.camBlockZ = MathHelper.floor(this.camZ);

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

					Iterator<TileEntity> tileEntityIterator = mc.level.blockEntityList.iterator();
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

			if (EntityCullingConfig.CLIENT_CONFIG.debug.get()) {
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
		if (EntityCulling.IS_OPTIFINE_DETECTED) {
			((ICullable) entity).setCulledShadowPass(!this.checkEntityShadowVisibility(entity));
		}
	}

	private void updateTileEntityCullingState(TileEntity tileEntity) {
		((ICullable) tileEntity).setCulledFast(!this.checkTileEntityVisibility(tileEntity));
		if (EntityCulling.IS_OPTIFINE_DETECTED) {
			((ICullable) tileEntity).setCulledShadowPass(!this.checkTileEntityShadowVisibility(tileEntity));
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

		if (entity.getBbWidth() > EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get() || entity.getBbHeight() > EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get()) {
			return true;
		}

		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(entity.getType().getRegistryName())) {
			return true;
		}

		double minX, minY, minZ, maxX, maxY, maxZ;
		{
			AxisAlignedBB aabb = entity.getBoundingBoxForCulling();

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

		if (!Boolean.TRUE.equals(METHOD_IS_BOX_IN_FRUSTUM.invoke(this.frustum, minX, minY, minZ, maxX, maxY, maxZ))) {
			// Assume that entities outside of the fov don't get rendered and thus there is no need to ray trace if they are visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (this.checkVisibility(entity.level, this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, 1.0D)) {
			return true;
		}

		return this.checkBoundingBoxVisibility(entity.level, minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean checkTileEntityVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRendering.get()) {
			return true;
		}

		AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get() || aabb.maxY - aabb.minY > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get() || aabb.maxZ - aabb.minZ > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()) {
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

		if (!Boolean.TRUE.equals(METHOD_IS_BOX_IN_FRUSTUM.invoke(this.frustum, minX, minY, minZ, maxX, maxY, maxZ))) {
			// Assume that tile entities outside of the fov don't get rendered and thus there is no need to ray trace if they are visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		if (this.checkVisibility(tileEntity.getLevel(), this.camX, this.camY, this.camZ, (minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, 1.0D)) {
			return true;
		}

		return this.checkBoundingBoxVisibility(tileEntity.getLevel(), minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean checkEntityShadowVisibility(Entity entity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		if (EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsDisabled.get()) {
			return false;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRendering.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCulling.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveMode.get()) {
			return !((ICullable) entity).isCulledFast();
		}

		// check if entity is boss (isNonBoss in forge mappings)
		if (!entity.canChangeDimensions()) {
			return true;
		}

		if (entity.getBbWidth() >= EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get() || entity.getBbHeight() >= EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get()) {
			return true;
		}

		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(entity.getType().getRegistryName())) {
			return true;
		}

		return this.checkVisibility(entity.level, this.camX, this.camY, this.camZ, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(), EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff.get());
	}

	private boolean checkTileEntityShadowVisibility(TileEntity tileEntity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		if (EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsDisabled.get()) {
			return false;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRendering.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCulling.get()) {
			return true;
		}

		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode.get()) {
			return !((ICullable) tileEntity).isCulledFast();
		}

		AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get() || aabb.maxY - aabb.minY > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get() || aabb.maxZ - aabb.minZ > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()) {
			return true;
		}

		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(tileEntity.getType().getRegistryName())) {
			return true;
		}

		BlockPos pos = tileEntity.getBlockPos();
		return this.checkVisibility(tileEntity.getLevel(), this.camX, this.camY, this.camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff.get());
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
		int cachedValue = this.cache.getCachedValue(endX - this.camBlockX, endY - this.camBlockY, endZ - this.camBlockZ);
		if (cachedValue > 0) {
			return cachedValue >> 1 == 1;
		}

		boolean flag = this.checkVisibility(world, this.camX, this.camY, this.camZ, endX, endY, endZ, 1.0D);

		if (cachedValue == 0) {
			this.cache.setCachedValue(endX - this.camBlockX, endY - this.camBlockY, endZ - this.camBlockZ, flag ? 2 : 1);
		}

		return flag;
	}

	private boolean checkVisibility(World world, double startX, double startY, double startZ, double endX, double endY, double endZ, double maxDiff) {
		MutableRayTraceResult rayTraceResult = RayTracingEngine.rayTraceBlocks(world, startX, startY, startZ, endX, endY, endZ, true, maxDiff, this.mutableRayTraceResult);
		return rayTraceResult == null;
	}

}
