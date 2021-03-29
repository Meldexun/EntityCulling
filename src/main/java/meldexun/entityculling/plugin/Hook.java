package meldexun.entityculling.plugin;

import static org.lwjgl.opengl.GL11C.GL_COLOR_WRITEMASK;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_WRITEMASK;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glColorMask;
import static org.lwjgl.opengl.GL11C.glDepthMask;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glGetBoolean;
import static org.lwjgl.opengl.GL11C.glGetBooleanv;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glGetQueryObjecti;
import static org.lwjgl.opengl.GL20C.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import meldexun.entityculling.EntityCullingClient;
import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.GLHelper;
import meldexun.entityculling.ICullable;
import meldexun.entityculling.ITileEntityBBCache;
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

	private static final ByteBuffer COLOR_MASK_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
	private static final FloatBuffer MATRIX_BUFFER = ByteBuffer.allocateDirect(4 * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private static final Random RAND = new Random();

	public static int entitiesRendered;
	public static int entitiesOcclusionCulled;
	public static int tileEntitiesRendered;
	public static int tileEntitiesOcclusionCulled;
	private static double x;
	private static double y;
	private static double z;

	private Hook() {

	}

	public static boolean shouldRenderEntity(Entity entity) {
		if (((ICullable) entity).isVisible()) {
			entitiesRendered++;
			return true;
		} else {
			entitiesOcclusionCulled++;
			return false;
		}
	}

	public static boolean shouldRenderTileEntity(TileEntity tileEntity) {
		if (((ICullable) tileEntity).isVisible()) {
			tileEntitiesRendered++;
			return true;
		} else {
			tileEntitiesOcclusionCulled++;
			return false;
		}
	}

	public static void preRenderEntities(ActiveRenderInfo activeRenderInfoIn, MatrixStack matrixStackIn, Matrix4f projectionIn) {
		entitiesRendered = 0;
		entitiesOcclusionCulled = 0;
		tileEntitiesRendered = 0;
		tileEntitiesOcclusionCulled = 0;

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

		if (!EntityCullingConfig.CLIENT_CONFIG.betaFeatures.get()) {
			return;
		}

		double updateChance = MathHelper.clamp(10.0D / (double) FIELD_DEBUG_FPS.get(null), 1.0e-7D, 0.5D);

		glGetBooleanv(GL_COLOR_WRITEMASK, COLOR_MASK_BUFFER);
		boolean depthMaskEnabled = glGetBoolean(GL_DEPTH_WRITEMASK);
		boolean depthTestEnabled = glGetBoolean(GL_DEPTH_TEST);
		boolean texture2dEnabled = glGetBoolean(GL_TEXTURE_2D);
		glColorMask(false, false, false, false);
		glDepthMask(false);
		glEnable(GL11.GL_DEPTH_TEST);
		glDisable(GL11.GL_TEXTURE_2D);

		GL11.glPushMatrix();
		matrixStackIn.last().pose().store(MATRIX_BUFFER);
		GL11.glMultMatrixf(MATRIX_BUFFER);
		GL11.glTranslated(-x, -y, -z);

		glBindBuffer(GL_ARRAY_BUFFER, EntityCullingClient.vertexBuffer);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EntityCullingClient.indexBuffer);

		for (Entity entity : mc.level.entitiesForRendering()) {
			AxisAlignedBB aabb = entity.getBoundingBoxForCulling();

			if (!((ICullable) entity).isCulledFast() || !METHOD_IS_BOX_IN_FRUSTUM.invoke(frustum, aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D) || mc.player.distanceToSqr(entity) < 4.0D * 4.0D) {
				((ICullable) entity).setCulledSlow(false);
				((ICullable) entity).setQueryResultDirty(false);
			} else {
				int query = ((ICullable) entity).initQuery();

				if (((ICullable) entity).isQueryResultDirty()) {
					((ICullable) entity).setCulledSlow(glGetQueryObjecti(query, GL_QUERY_RESULT) == 0);
				}

				if (RAND.nextDouble() < updateChance) {
					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D);
					GL11.glScaled(aabb.maxX - aabb.minX + 1.0D, aabb.maxY - aabb.minY + 1.0D, aabb.maxZ - aabb.minZ + 1.0D);

					GLHelper.beginQuery(query);
					glDrawElements(GL_TRIANGLE_STRIP, 14, GL_UNSIGNED_BYTE, 0);
					GLHelper.endQuery();

					GL11.glPopMatrix();

					((ICullable) entity).setQueryResultDirty(true);
				} else {
					((ICullable) entity).setQueryResultDirty(false);
				}
			}
		}

		for (TileEntity tileEntity : mc.level.blockEntityList) {
			AxisAlignedBB aabb = ((ITileEntityBBCache) tileEntity).getCachedAABB();

			if (!((ICullable) tileEntity).isCulledFast() || !METHOD_IS_BOX_IN_FRUSTUM.invoke(frustum, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)
					|| mc.player.distanceToSqr(tileEntity.getBlockPos().getX() + 0.5D, tileEntity.getBlockPos().getY() + 0.5D, tileEntity.getBlockPos().getZ() + 0.5D) < 4.0D * 4.0D) {
				((ICullable) tileEntity).setCulledSlow(false);
				((ICullable) tileEntity).setQueryResultDirty(false);
			} else {
				int query = ((ICullable) tileEntity).initQuery();

				if (((ICullable) tileEntity).isQueryResultDirty()) {
					((ICullable) tileEntity).setCulledSlow(glGetQueryObjecti(query, GL_QUERY_RESULT) == 0);
				}

				if (RAND.nextDouble() < updateChance) {
					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX, aabb.minY, aabb.minZ);
					GL11.glScaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);

					GLHelper.beginQuery(query);
					glDrawElements(GL_TRIANGLE_STRIP, 14, GL_UNSIGNED_BYTE, 0);
					GLHelper.endQuery();

					GL11.glPopMatrix();

					((ICullable) tileEntity).setQueryResultDirty(true);
				} else {
					((ICullable) tileEntity).setQueryResultDirty(false);
				}
			}
		}

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		GL11.glPopMatrix();

		if (texture2dEnabled) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		} else {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		if (depthTestEnabled) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}
		glDepthMask(depthMaskEnabled);
		glColorMask(COLOR_MASK_BUFFER.get(0) == 1, COLOR_MASK_BUFFER.get(1) == 1, COLOR_MASK_BUFFER.get(2) == 1, COLOR_MASK_BUFFER.get(3) == 1);
	}

	// TODO call via ASM
	public static void postRenderEntities() {
		Minecraft mc = Minecraft.getInstance();
		FIELD_COUNT_ENTITIES_RENDERED.set(mc.levelRenderer, FIELD_COUNT_ENTITIES_RENDERED.get(mc.levelRenderer) - entitiesOcclusionCulled);
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
