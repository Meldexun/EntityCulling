package meldexun.entityculling.plugin;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import meldexun.entityculling.EntityCullingConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class Hook {

	private static final Set<ResourceLocation> ENTITY_BLACKLIST = new HashSet<>();
	private static final Set<ResourceLocation> TILE_ENTITY_BLACKLIST = new HashSet<>();

	private Hook() {

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

	public static boolean shouldRenderEntity(Entity entity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		Minecraft mc = Minecraft.getInstance();
		ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();

		return checkEntityVisibility(entity, activeRenderInfo.getProjectedView());
	}

	public static boolean shouldRenderTileEntity(TileEntity tileEntity) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}

		Minecraft mc = Minecraft.getInstance();
		ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();

		return checkTileEntityVisibility(tileEntity, activeRenderInfo.getProjectedView());
	}

	private static boolean checkEntityVisibility(Entity entity, Vector3d camVec) {
		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRendering.get()) {
			return true;
		}
		if (!entity.isNonBoss()) {
			return true;
		}
		if (entity.getWidth() >= EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get() || entity.getHeight() >= EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize.get()) {
			return true;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingBlacklist.get().isEmpty() && EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingBlacklist.get().stream().anyMatch(entity.getType().getRegistryName().toString()::equals)) {
			return true;
		}
		double maxDiffSquared = EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingDiff.get() * EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingDiff.get();
		Minecraft mc = Minecraft.getInstance();
		Vector3d end = entity.getEyePosition(mc.getRenderPartialTicks());
		if (camVec.squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result1 = rayTraceBlocks(mc.world, new RayTraceContext(camVec, end, BlockMode.COLLIDER, FluidMode.NONE, mc.renderViewEntity), null);
		if (result1 == null || result1.getHitVec().squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result2 = rayTraceBlocks(mc.world, new RayTraceContext(end, camVec, BlockMode.COLLIDER, FluidMode.NONE, mc.renderViewEntity), null);
		if (result2 == null) {
			return true;
		}
		return result1.getHitVec().squareDistanceTo(result2.getHitVec()) <= maxDiffSquared;
	}

	private static boolean checkTileEntityVisibility(TileEntity tileEntity, Vector3d camVec) {
		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRendering.get()) {
			return true;
		}
		AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get() || aabb.maxY - aabb.minY > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get() || aabb.maxZ - aabb.minZ > EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize.get()) {
			return true;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingBlacklist.get().isEmpty() && EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingBlacklist.get().stream().anyMatch(tileEntity.getType().getRegistryName().toString()::equals)) {
			return true;
		}
		double maxDiffSquared = EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingDiff.get() * EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingDiff.get();
		Minecraft mc = Minecraft.getInstance();
		BlockPos pos = tileEntity.getPos();
		Vector3d end = new Vector3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
		if (camVec.squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result1 = rayTraceBlocks(mc.world, new RayTraceContext(camVec, end, BlockMode.COLLIDER, FluidMode.NONE, mc.renderViewEntity), pos);
		if (result1 == null || result1.getHitVec().squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result2 = rayTraceBlocks(mc.world, new RayTraceContext(end, camVec, BlockMode.COLLIDER, FluidMode.NONE, mc.renderViewEntity), pos);
		if (result2 == null) {
			return true;
		}
		return result1.getHitVec().squareDistanceTo(result2.getHitVec()) <= maxDiffSquared;
	}

	private static BlockRayTraceResult rayTraceBlocks(World world, RayTraceContext context, @Nullable BlockPos toIgnore) {
		return IBlockReader.doRayTrace(context, (c, p) -> {
			if (toIgnore != null && p.equals(toIgnore)) {
				return null;
			}
			BlockState blockstate = world.getBlockState(p);
			if (!blockstate.isOpaqueCube(world, p)) {
				return null;
			}
			FluidState fluidstate = world.getFluidState(p);
			Vector3d vector3d = c.getStartVec();
			Vector3d vector3d1 = c.getEndVec();
			VoxelShape voxelshape = c.getBlockShape(blockstate, world, p);
			BlockRayTraceResult blockraytraceresult = world.rayTraceBlocks(vector3d, vector3d1, p, voxelshape, blockstate);
			VoxelShape voxelshape1 = c.getFluidShape(fluidstate, world, p);
			BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(vector3d, vector3d1, p);
			double d0 = blockraytraceresult == null ? Double.MAX_VALUE : c.getStartVec().squareDistanceTo(blockraytraceresult.getHitVec());
			double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : c.getStartVec().squareDistanceTo(blockraytraceresult1.getHitVec());
			return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
		}, (c) -> {
			Vector3d vector3d = c.getStartVec().subtract(c.getEndVec());
			return BlockRayTraceResult.createMiss(c.getEndVec(), Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z), new BlockPos(c.getEndVec()));
		});
	}

}
