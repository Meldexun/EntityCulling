package meldexun.entityculling.plugin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;

import com.mojang.blaze3d.matrix.MatrixStack;

import meldexun.entityculling.EntityCullingClient;
import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.ICullable;
import meldexun.entityculling.reflection.ReflectionField;
import meldexun.entityculling.reflection.ReflectionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class Hook {

	private static final ReflectionField<Integer> FIELD_COUNT_ENTITIES_RENDERED = new ReflectionField<>(WorldRenderer.class, "field_72749_I", "renderedEntities");
	private static final ReflectionField<Integer> FIELD_DEBUG_FPS = new ReflectionField<>(Minecraft.class, "field_71470_ab", "fps");
	private static final ReflectionMethod<Boolean> METHOD_IS_BOX_IN_FRUSTUM = new ReflectionMethod<>(ClippingHelper.class, "func_228953_a_", "cubeInFrustum", Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);

	private static final ReflectionField<ChunkRender> FIELD_RENDER_CHUNK = new ReflectionField<>("net.minecraft.client.renderer.WorldRenderer$LocalRenderInformationContainer", "field_178036_a", "chunk");

	private static final Random RAND = new Random();

	private static double x;
	private static double y;
	private static double z;

	private Hook() {

	}

	@SuppressWarnings("resource")
	public static boolean shouldRenderEntity(Entity entity) {
		boolean flag = ((ICullable) entity).isVisible();
		if (EntityCullingConfig.CLIENT_CONFIG.debug.get() && !flag) {
			WorldRenderer worldRenderer = Minecraft.getInstance().levelRenderer;
			FIELD_COUNT_ENTITIES_RENDERED.set(worldRenderer, FIELD_COUNT_ENTITIES_RENDERED.get(worldRenderer) - 1);
		}
		return flag;
	}

	public static boolean shouldRenderTileEntity(TileEntity tileEntity) {
		return ((ICullable) tileEntity).isVisible();
	}

	public static void preRenderEntities(ActiveRenderInfo activeRenderInfoIn, MatrixStack matrixStackIn, Matrix4f projectionIn) {
		Minecraft mc = Minecraft.getInstance();
		Vector3d vec = activeRenderInfoIn.getPosition();
		x = vec.x;
		y = vec.y;
		z = vec.z;
		ClippingHelper frustum = new ClippingHelper(matrixStackIn.last().pose(), projectionIn);
		frustum.prepare(x, y, z);

		if (!EntityCullingConfig.CLIENT_CONFIG.debug.get()) {
			EntityCullingClient.CULLING_THREAD.camX = x;
			EntityCullingClient.CULLING_THREAD.camY = y;
			EntityCullingClient.CULLING_THREAD.camZ = z;
		} else {
			EntityCullingClient.CULLING_THREAD.camX = mc.getCameraEntity().getX();
			EntityCullingClient.CULLING_THREAD.camY = mc.getCameraEntity().getEyeY();
			EntityCullingClient.CULLING_THREAD.camZ = mc.getCameraEntity().getZ();
		}
		EntityCullingClient.CULLING_THREAD.matrix = matrixStackIn.last().pose().copy();
		EntityCullingClient.CULLING_THREAD.projection = projectionIn.copy();

		double updateChance = MathHelper.clamp(20.0D / (double) FIELD_DEBUG_FPS.get(null), 1.0e-7D, 0.5D);

		GL11.glDepthMask(false);
		GL11.glColorMask(false, false, false, false);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPushMatrix();
		FloatBuffer floatBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
		matrixStackIn.last().pose().store(floatBuffer);
		GL11.glMultMatrixf(floatBuffer);

		for (Entity entity : mc.level.entitiesForRendering()) {
			AxisAlignedBB aabb = entity.getBoundingBoxForCulling();

			if (!((ICullable) entity).isCulledFast() || !METHOD_IS_BOX_IN_FRUSTUM.invoke(frustum, aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D) || mc.player.distanceToSqr(entity) < 4.0D * 4.0D) {
				((ICullable) entity).setCulledSlow(false);
				((ICullable) entity).setQueryResultDirty(false);
			} else {
				int query = ((ICullable) entity).initQuery();

				if (((ICullable) entity).isQueryResultDirty()) {
					((ICullable) entity).setCulledSlow(GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT) == 0);
				}

				if (RAND.nextDouble() < updateChance) {
					GL15.glBeginQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE, query);

					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX - 0.5D - x, aabb.minY - 0.5D - y, aabb.minZ - 0.5D - z);
					GL11.glScaled(aabb.maxX - aabb.minX + 1.0D, aabb.maxY - aabb.minY + 1.0D, aabb.maxZ - aabb.minZ + 1.0D);
					GL11.glCallList(EntityCullingClient.cubeDisplayList);
					GL11.glPopMatrix();

					GL15.glEndQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE);

					((ICullable) entity).setQueryResultDirty(true);
				} else {
					((ICullable) entity).setQueryResultDirty(false);
				}
			}
		}

		for (TileEntity tileEntity : mc.level.blockEntityList) {
			AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();

			if (!((ICullable) tileEntity).isCulledFast() || !METHOD_IS_BOX_IN_FRUSTUM.invoke(frustum, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ) || mc.player.distanceToSqr(tileEntity.getBlockPos().getX() + 0.5D, tileEntity.getBlockPos().getY() + 0.5D, tileEntity.getBlockPos().getZ() + 0.5D) < 4.0D * 4.0D) {
				((ICullable) tileEntity).setCulledSlow(false);
				((ICullable) tileEntity).setQueryResultDirty(false);
			} else {
				int query = ((ICullable) tileEntity).initQuery();

				if (((ICullable) tileEntity).isQueryResultDirty()) {
					((ICullable) tileEntity).setCulledSlow(GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT) == 0);
				}

				if (RAND.nextDouble() < updateChance) {
					GL15.glBeginQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE, query);

					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX - x, aabb.minY - y, aabb.minZ - z);
					GL11.glScaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
					GL11.glCallList(EntityCullingClient.cubeDisplayList);
					GL11.glPopMatrix();

					GL15.glEndQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE);

					((ICullable) tileEntity).setQueryResultDirty(true);
				} else {
					((ICullable) tileEntity).setQueryResultDirty(false);
				}
			}
		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColorMask(true, true, true, true);
		GL11.glDepthMask(true);
	}

	public static boolean shouldRenderEntityShadow(Entity entity) {
		if (((ICullable) entity).isCulledShadowPass()) {
			return false;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsDistanceLimited.get()) {
			return true;
		}
		double d = EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsMaxDistance.get();
		return entity.distanceToSqr(x, y, z) < d * d;
	}

	public static boolean shouldRenderTileEntityShadow(TileEntity tileEntity) {
		if (((ICullable) tileEntity).isCulledShadowPass()) {
			return false;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsDistanceLimited.get()) {
			return true;
		}
		double d = EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsMaxDistance.get();
		return squareDist(tileEntity.getBlockPos().getX() + 0.5D, tileEntity.getBlockPos().getY() + 0.5D, tileEntity.getBlockPos().getZ() + 0.5D, x, y, z) < d * d;
	}

	private static double squareDist(double x1, double y1, double z1, double x2, double y2, double z2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1);
	}

	public static boolean shouldRenderChunkShadow(Object containerLocalRenderInformation) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}
		if (EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsDisabled.get()) {
			return false;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsDistanceLimited.get()) {
			return true;
		}
		ChunkRender renderChunk = FIELD_RENDER_CHUNK.get(containerLocalRenderInformation);
		BlockPos pos = renderChunk.getOrigin();
		if (Math.abs(pos.getX() + 8.0D - x) > EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxHorizontalDistance.get()) {
			return false;
		}
		if (Math.abs(pos.getY() + 8.0D - y) > EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxVerticalDistance.get()) {
			return false;
		}
		return Math.abs(pos.getZ() + 8.0D - z) <= EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxHorizontalDistance.get();
	}

}
