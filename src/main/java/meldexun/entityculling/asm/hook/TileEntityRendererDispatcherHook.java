package meldexun.entityculling.asm.hook;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntityRendererDispatcherHook {

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
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		BlockPos blockpos = tileEntity.getPos();
		double x1 = blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX;
		double y1 = blockpos.getY() - TileEntityRendererDispatcher.staticPlayerY;
		double z1 = blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
		TileEntityRendererDispatcher.instance.render(tileEntity, x1, y1, z1, partialTicks, destroyStage, 1.0F);
		return true;
	}

}
