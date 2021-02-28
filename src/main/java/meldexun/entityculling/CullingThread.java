package meldexun.entityculling;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import meldexun.entityculling.RayTracingEngine.MutableRayTraceResult;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;

public class CullingThread extends Thread {

	private static final ReflectionField<RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>>> FIELD_REGISTRY = new ReflectionField<>(TileEntity.class, "field_190562_f", "REGISTRY");
	private static final Set<ResourceLocation> ENTITY_BLACKLIST = new HashSet<>();
	private static final Set<ResourceLocation> TILE_ENTITY_BLACKLIST = new HashSet<>();

	private static final ReflectionField<ClippingHelperImpl> FIELD_INSTANCE = new ReflectionField<>(ClippingHelperImpl.class, "field_78563_e", "instance");
	private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
	private final MutableRayTraceResult mutableRayTraceResult1 = new MutableRayTraceResult();
	private final MutableRayTraceResult mutableRayTraceResult2 = new MutableRayTraceResult();
	private double sleepOverhead = 0.0D;
	/** debug */
	public long[] time = new long[10];

	public CullingThread() {
		super();
		this.setName("Culling Thread");
		this.setDaemon(true);
	}

	public static void updateBlacklists() {
		ENTITY_BLACKLIST.clear();
		TILE_ENTITY_BLACKLIST.clear();

		for (String s : EntityCullingConfig.skipHiddenEntityRenderingBlacklist) {
			ENTITY_BLACKLIST.add(new ResourceLocation(s));
		}

		for (String s : EntityCullingConfig.skipHiddenTileEntityRenderingBlacklist) {
			TILE_ENTITY_BLACKLIST.add(new ResourceLocation(s));
		}
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();

		while (true) {
			long t = System.nanoTime();
			try {
				if (mc.world != null && mc.getRenderViewEntity() != null) {
					Entity renderViewEntity = mc.getRenderViewEntity();
					float partialTicks = mc.getRenderPartialTicks();
					double x = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
					double y = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
					double z = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
					Frustum frustum = new Frustum(FIELD_INSTANCE.get(null));
					frustum.setPosition(x, y, z);
					Vec3d cameraPosition = ActiveRenderInfo.getCameraPosition();
					double camX = x + cameraPosition.x;
					double camY = y + cameraPosition.y;
					double camZ = z + cameraPosition.z;

					Iterator<Entity> entityIterator = mc.world.loadedEntityList.iterator();
					while (entityIterator.hasNext()) {
						try {
							Entity entity = entityIterator.next();
							((ICullable) entity).setCulled(!this.checkEntityVisibility(entity, frustum, camX, camY, camZ));
						} catch (Exception e) {
							// ignore
							break;
						}
					}

					Iterator<TileEntity> tileEntityIterator = mc.world.loadedTileEntityList.iterator();
					while (tileEntityIterator.hasNext()) {
						try {
							TileEntity tileEntity = tileEntityIterator.next();
							((ICullable) tileEntity).setCulled(!this.checkTileEntityVisibility(tileEntity, frustum, camX, camY, camZ));
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

			if (EntityCullingContainer.debug) {
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

	private boolean checkEntityVisibility(Entity entity, Frustum frustum, double camX, double camY, double camZ) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}

		if (!EntityCullingConfig.skipHiddenEntityRendering) {
			return true;
		}

		if (!entity.isNonBoss()) {
			return true;
		}

		if (entity.width >= EntityCullingConfig.skipHiddenEntityRenderingSize || entity.height >= EntityCullingConfig.skipHiddenEntityRenderingSize) {
			return true;
		}

		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(EntityList.getKey(entity))) {
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

		if (!frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that entities outside of the fov don't get rendered and thus there is no need to ray trace if they are visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		double dx = (maxX + minX) * 0.5D - camX;
		double dy = (maxY + minY) * 0.5D - camY;
		double dz = (maxZ + minZ) * 0.5D - camZ;
		double dist = dx * dx + dy * dy + dz * dz;

		if (dist <= 32.0D * 32.0D) {
			if (this.checkVisibility(entity.world, camX, camY, camZ, entity.posX, entity.posY + entity.height * 0.5D, entity.posZ, null, EntityCullingConfig.skipHiddenEntityRenderingDiff32)) {
				return true;
			}

			return this.checkBoundingBoxVisibility(entity.world, minX, minY, minZ, maxX, maxY, maxZ, camX, camY, camZ, 2, EntityCullingConfig.skipHiddenEntityRenderingDiff32);
		} else if (dist <= 64.0D * 64.0D) {
			if (this.checkVisibility(entity.world, camX, camY, camZ, entity.posX, entity.posY + entity.height * 0.5D, entity.posZ, null, EntityCullingConfig.skipHiddenEntityRenderingDiff64)) {
				return true;
			}

			return this.checkBoundingBoxVisibility(entity.world, minX, minY, minZ, maxX, maxY, maxZ, camX, camY, camZ, 2, EntityCullingConfig.skipHiddenEntityRenderingDiff64);
		} else if (dist <= 128.0D * 128.0D) {
			return this.checkVisibility(entity.world, camX, camY, camZ, entity.posX, entity.posY + entity.height * 0.5D, entity.posZ, null, EntityCullingConfig.skipHiddenEntityRenderingDiff128);
		} else {
			return !EntityCullingConfig.skipHiddenEntityRenderingHideFarAway;
		}
	}

	private boolean checkTileEntityVisibility(TileEntity tileEntity, Frustum frustum, double camX, double camY, double camZ) {
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

		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(FIELD_REGISTRY.get(null).getNameForObject(tileEntity.getClass()))) {
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

		if (!frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
			// Assume that tile entities outside of the fov don't get rendered and thus there is no need to ray trace if they are visible.
			// But return true because there might be special entities which are always rendered.
			return true;
		}

		double dx = (maxX + minX) * 0.5D - camX;
		double dy = (maxY + minY) * 0.5D - camY;
		double dz = (maxZ + minZ) * 0.5D - camZ;
		double dist = dx * dx + dy * dy + dz * dz;
		BlockPos pos = tileEntity.getPos();

		if (dist <= 32.0D * 32.0D) {
			if (this.checkVisibility(tileEntity.getWorld(), camX, camY, camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, pos, EntityCullingConfig.skipHiddenTileEntityRenderingDiff32)) {
				return true;
			}

			return this.checkBoundingBoxVisibility(tileEntity.getWorld(), minX, minY, minZ, maxX, maxY, maxZ, camX, camY, camZ, 2, EntityCullingConfig.skipHiddenTileEntityRenderingDiff32);
		} else if (dist <= 64.0D * 64.0D) {
			if (this.checkVisibility(tileEntity.getWorld(), camX, camY, camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, pos, EntityCullingConfig.skipHiddenTileEntityRenderingDiff64)) {
				return true;
			}

			return this.checkBoundingBoxVisibility(tileEntity.getWorld(), minX, minY, minZ, maxX, maxY, maxZ, camX, camY, camZ, 2, EntityCullingConfig.skipHiddenTileEntityRenderingDiff64);
		} else if (dist <= 128.0D * 128.0D) {
			return this.checkVisibility(tileEntity.getWorld(), camX, camY, camZ, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, pos, EntityCullingConfig.skipHiddenTileEntityRenderingDiff128);
		} else {
			return !EntityCullingConfig.skipHiddenTileEntityRenderingHideFarAway;
		}
	}

	private boolean checkBoundingBoxVisibility(World world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double camX, double camY, double camZ, int level, double maxDiff) {
		level = Math.max(level, 2);
		double d0 = 1.0D / (level - 1.0D);
		double d1 = (maxX - minX) * d0;
		double d2 = (maxY - minY) * d0;
		double d3 = (maxZ - minZ) * d0;

		for (int i = 0; i < level; i++) {
			for (int j = 0; j < level; j++) {
				for (int k = 0; k < level; k++) {
					double x = minX + i * d1;
					double y = minY + j * d2;
					double z = minZ + k * d3;
					if (this.checkVisibility(world, camX, camY, camZ, x, y, z, this.mutablePos.setPos(x, y, z), maxDiff)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean checkVisibility(World world, double x1, double y1, double z1, double x2, double y2, double z2, @Nullable BlockPos toIgnore, double maxDiff) {
		double maxDiffSquared = maxDiff * maxDiff;
		if (CullingThread.squareDist(x1, y1, z1, x2, y2, z2) <= maxDiffSquared) {
			return true;
		}
		MutableRayTraceResult rayTraceResult1 = RayTracingEngine.rayTraceBlocks(world, x1, y1, z1, x2, y2, z2, toIgnore, this.mutableRayTraceResult1);
		if (rayTraceResult1 == null || CullingThread.squareDist(rayTraceResult1.x, rayTraceResult1.y, rayTraceResult1.z, x2, y2, z2) <= maxDiffSquared) {
			return true;
		}
		MutableRayTraceResult rayTraceResult2 = RayTracingEngine.rayTraceBlocks(world, x2, y2, z2, x1, y1, z1, toIgnore, this.mutableRayTraceResult2);
		if (rayTraceResult2 == null) {
			return true;
		}
		double d = CullingThread.squareDist(rayTraceResult1.x, rayTraceResult1.y, rayTraceResult1.z, rayTraceResult2.x, rayTraceResult2.y, rayTraceResult2.z);
		return d <= maxDiffSquared;
	}

	private static double squareDist(double x1, double y1, double z1, double x2, double y2, double z2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1);
	}

}
