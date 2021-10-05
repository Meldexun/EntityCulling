package meldexun.entityculling.asm;

import com.mojang.blaze3d.matrix.MatrixStack;

import meldexun.entityculling.EntityCullingClient;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.ICullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class WorldRendererHook {

	public static boolean shouldRenderEntity(Entity entity) {
		if (((ICullable) entity).isCulled()) {
			Minecraft mc = Minecraft.getInstance();
			mc.levelRenderer.renderedEntities--;
			mc.levelRenderer.culledEntities++;
			return false;
		}

		return true;
	}

	public static boolean shouldRenderTileEntity(TileEntity tileEntity) {
		if (((ICullable) tileEntity).isCulled()) {
			EntityCullingClient.culledTileEntities++;
			return false;
		}

		EntityCullingClient.renderedTileEntities++;
		return true;
	}

	public static void preRenderEntities(ActiveRenderInfo activeRenderInfoIn, MatrixStack matrixStackIn, Matrix4f projectionIn) {
		EntityCullingClient.renderedTileEntities = 0;
		EntityCullingClient.culledTileEntities = 0;

		Vector3d camera = activeRenderInfoIn.getPosition();
		EntityCullingClient.CULLING_THREAD.camX = camera.x;
		EntityCullingClient.CULLING_THREAD.camY = camera.y;
		EntityCullingClient.CULLING_THREAD.camZ = camera.z;
		EntityCullingClient.CULLING_THREAD.view = matrixStackIn.last().pose().copy();
		EntityCullingClient.CULLING_THREAD.projection = projectionIn.copy();
	}

	public static boolean shouldRenderEntityShadow(Entity entity) {
		if (((ICullable) entity).isShadowCulled()) {
			return false;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsDistanceLimited.get()) {
			return true;
		}
		Minecraft mc = Minecraft.getInstance();
		Vector3d camera = mc.gameRenderer.getMainCamera().getPosition();
		double d = EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsMaxDistance.get() * 16.0D;
		return entity.distanceToSqr(camera.x, camera.y, camera.z) < d * d;
	}

	public static boolean shouldRenderTileEntityShadow(TileEntity tileEntity) {
		if (((ICullable) tileEntity).isShadowCulled()) {
			return false;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsDistanceLimited.get()) {
			return true;
		}
		Minecraft mc = Minecraft.getInstance();
		Vector3d camera = mc.gameRenderer.getMainCamera().getPosition();
		double d = EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsMaxDistance.get() * 16.0D;
		return tileEntity.getBlockPos().distSqr(camera.x, camera.y, camera.z, true) < d * d;
	}

}
