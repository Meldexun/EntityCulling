package meldexun.entityculling.plugin;

import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.reflection.ReflectionField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class Hook {

	private static final ReflectionField<RenderChunk> FIELD_RENDER_CHUNK = new ReflectionField<>("net.minecraft.client.renderer.RenderGlobal$ContainerLocalRenderInformation", "field_178036_a", "renderChunk");
	private static EntityCullingRenderer renderer;

	private Hook() {

	}

	public static EntityCullingRenderer getRenderer() {
		if (renderer == null) {
			if (EntityCullingTransformer.IS_OPTIFINE_DETECTED) {
				renderer = new EntityCullingRendererOptifine();
			} else {
				renderer = new EntityCullingRenderer();
			}
		}
		return renderer;
	}

	public static int getLimitFramerate() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.world == null ? MathHelper.clamp(mc.gameSettings.limitFramerate, 30, 240) : mc.gameSettings.limitFramerate;
	}

	public static boolean renderEntities() {
		return getRenderer().renderEntities();
	}

	public static boolean renderTileEntities() {
		return getRenderer().renderTileEntities();
	}

	public static boolean shouldRender(Render<?> render, Entity entity, ICamera camera, double camX, double camY, double camZ) {
		if (!entity.isInRangeToRender3d(camX, camY, camZ)) {
			return false;
		}

		if (entity.ignoreFrustumCheck) {
			return true;
		}

		AxisAlignedBB aabb = entity.getRenderBoundingBox();

		if (aabb.hasNaN()) {
			if (camera instanceof Frustum) {
				return ((Frustum) camera).isBoxInFrustum(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D, entity.posZ + 2.0D);
			} else {
				return camera.isBoundingBoxInFrustum(new AxisAlignedBB(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D, entity.posZ + 2.0D));
			}
		}

		if (camera instanceof Frustum) {
			return ((Frustum) camera).isBoxInFrustum(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D);
		} else {
			return camera.isBoundingBoxInFrustum(new AxisAlignedBB(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D));
		}
	}

	public static boolean render(TileEntity tileEntity, int destroyStage, boolean drawingBatch) {
		if (!EntityCullingConfig.enabled) {
			return false;
		}
		Minecraft mc = Minecraft.getMinecraft();
		float partialTicks = mc.getRenderPartialTicks();
		if (!drawingBatch || !tileEntity.hasFastRenderer()) {
			RenderHelper.enableStandardItemLighting();
			int i = mc.world.getCombinedLight(tileEntity.getPos(), 0);
			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		BlockPos blockpos = tileEntity.getPos();
		double x1 = (double) blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX;
		double y1 = (double) blockpos.getY() - TileEntityRendererDispatcher.staticPlayerY;
		double z1 = (double) blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
		TileEntityRendererDispatcher.instance.render(tileEntity, x1, y1, z1, partialTicks, destroyStage, 1.0F);
		return true;
	}

	public static boolean shouldRenderChunkShadow(Object containerLocalRenderInformation) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}
		if (!EntityCullingConfig.optifineShaderOptions.terrainShadowsEnabled) {
			return false;
		}
		if (!EntityCullingConfig.optifineShaderOptions.terrainShadowsDistanceLimited) {
			return true;
		}
		RenderChunk renderChunk = FIELD_RENDER_CHUNK.get(containerLocalRenderInformation);
		BlockPos pos = renderChunk.getPosition();
		Minecraft mc = Minecraft.getMinecraft();
		Entity entity = mc.getRenderViewEntity();
		float partialTicks = mc.getRenderPartialTicks();
		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		if (Math.abs(pos.getX() + 8.0D - x) > EntityCullingConfig.optifineShaderOptions.terrainShadowsMaxHorizontalDistance * 16.0D) {
			return false;
		}
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks + entity.getEyeHeight();
		if (Math.abs(pos.getY() + 8.0D - y) > EntityCullingConfig.optifineShaderOptions.terrainShadowsMaxVerticalDistance * 16.0D) {
			return false;
		}
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		return Math.abs(pos.getZ() + 8.0D - z) <= EntityCullingConfig.optifineShaderOptions.terrainShadowsMaxHorizontalDistance * 16.0D;
	}

}
