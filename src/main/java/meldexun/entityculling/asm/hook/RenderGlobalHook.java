package meldexun.entityculling.asm.hook;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.renderer.entity.EntityRenderer;
import meldexun.entityculling.renderer.entity.EntityRendererOptifine;
import meldexun.entityculling.renderer.tileentity.TileEntityRenderer;
import meldexun.entityculling.renderer.tileentity.TileEntityRendererOptifine;
import meldexun.reflectionutil.ReflectionField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal.ContainerLocalRenderInformation;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public final class RenderGlobalHook {

	public static EntityRenderer entityRenderer = EntityCullingClassTransformer.OPTIFINE_DETECTED ? new EntityRendererOptifine() : new EntityRenderer();
	public static TileEntityRenderer tileEntityRenderer = EntityCullingClassTransformer.OPTIFINE_DETECTED ? new TileEntityRendererOptifine() : new TileEntityRenderer();
	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
	private static int lastFrameUpdated = -1;
	private static int lastFrameUpdatedShadows = -1;

	public static void setup(double partialTicks, ICamera frustum, int frame) {
		if (EntityCullingClassTransformer.OPTIFINE_DETECTED && IS_SHADOW_PASS.getBoolean(null)) {
			if (EntityCulling.frame <= lastFrameUpdatedShadows) {
				return;
			}
			lastFrameUpdatedShadows = EntityCulling.frame;
		} else {
			if (EntityCulling.frame <= lastFrameUpdated) {
				return;
			}
			lastFrameUpdated = EntityCulling.frame;
		}

		Minecraft mc = Minecraft.getMinecraft();
		Entity viewEntity = mc.getRenderViewEntity();
		double x = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
		double y = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
		double z = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;

		entityRenderer.setup(frustum, x, y, z, partialTicks);
		tileEntityRenderer.setup(frustum, x, y, z, partialTicks);
	}

	public static boolean renderEntities(float partialTicks) {
		if (!EntityCullingConfig.enabled) {
			return false;
		}

		entityRenderer.renderEntities(partialTicks);
		return true;
	}

	public static boolean renderTileEntities(float partialTicks) {
		if (!EntityCullingConfig.enabled) {
			return false;
		}

		tileEntityRenderer.renderTileEntities(partialTicks);
		return true;
	}

	public static boolean shouldRenderChunkShadow(ContainerLocalRenderInformation containerLocalRenderInformation) {
		if (!EntityCullingConfig.enabled) {
			return true;
		}
		if (!EntityCullingConfig.optifineShaderOptions.terrainShadowsEnabled) {
			return false;
		}
		if (!EntityCullingConfig.optifineShaderOptions.terrainShadowsDistanceLimited) {
			return true;
		}
		RenderChunk renderChunk = containerLocalRenderInformation.renderChunk;
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
