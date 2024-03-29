package meldexun.entityculling.asm;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer.RenderChunkInfo;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ShadersRenderHook {

	public static boolean shouldRenderChunkShadow(RenderChunkInfo renderInfoContainer) {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return true;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsEnabled.get()) {
			return false;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsDistanceLimited.get()) {
			return true;
		}
		Minecraft mc = Minecraft.getInstance();
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		RenderChunk renderChunk = renderInfoContainer.chunk;
		BlockPos pos = renderChunk.getOrigin();
		if (Math.abs(pos.getX() + 8.0D - camera.x) > EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxHorizontalDistance.get()
				* 16.0D) {
			return false;
		}
		if (Math.abs(pos.getY() + 8.0D - camera.y) > EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxVerticalDistance.get() * 16.0D) {
			return false;
		}
		return Math.abs(pos.getZ() + 8.0D - camera.z) <= EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxHorizontalDistance.get()
				* 16.0D;
	}

}
