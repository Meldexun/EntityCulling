package meldexun.entityculling.asm.hook;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.renderer.entity.EntityRenderer;
import meldexun.entityculling.renderer.entity.EntityRendererOptifine;
import meldexun.entityculling.renderer.tileentity.TileEntityRenderer;
import meldexun.entityculling.renderer.tileentity.TileEntityRendererOptifine;
import meldexun.entityculling.util.culling.CullingInstance;
import meldexun.entityculling.util.matrix.Matrix4f;
import meldexun.reflectionutil.ReflectionField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal.ContainerLocalRenderInformation;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;

public final class RenderGlobalHook {

	public static EntityRenderer entityRenderer = EntityCullingClassTransformer.OPTIFINE_DETECTED ? new EntityRendererOptifine() : new EntityRenderer();
	public static TileEntityRenderer tileEntityRenderer = EntityCullingClassTransformer.OPTIFINE_DETECTED ? new TileEntityRendererOptifine() : new TileEntityRenderer();
	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
	private static final FloatBuffer MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
	private static int lastFrameUpdated = -1;
	private static int lastFrameUpdatedShadows = -1;
	public static double cameraEntityX;
	public static double cameraEntityY;
	public static double cameraEntityZ;

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
		cameraEntityX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
		cameraEntityY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
		cameraEntityZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;

		entityRenderer.setup(frustum, cameraEntityX, cameraEntityY, cameraEntityZ, partialTicks);
		tileEntityRenderer.setup(frustum, cameraEntityX, cameraEntityY, cameraEntityZ, partialTicks);
	}

	public static boolean renderEntities(float partialTicks) {
		if (!EntityCullingConfig.enabled) {
			return false;
		}

		if (EntityCulling.useOpenGlBasedCulling()
				&& MinecraftForgeClient.getRenderPass() == 0
				&& (!EntityCullingClassTransformer.OPTIFINE_DETECTED || !IS_SHADOW_PASS.getBoolean(null))) {
			Matrix4f matrix = getMatrix(GL11.GL_PROJECTION_MATRIX);
			matrix.multiply(getMatrix(GL11.GL_MODELVIEW_MATRIX));
			CullingInstance.getInstance().updateResults(matrix);
		}

		entityRenderer.renderEntities(partialTicks);
		return true;
	}

	private static Matrix4f getMatrix(int matrix) {
		GL11.glGetFloat(matrix, MATRIX_BUFFER);
		Matrix4f m = new Matrix4f();
		m.load(MATRIX_BUFFER);
		return m;
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
