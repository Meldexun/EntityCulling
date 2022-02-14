package meldexun.entityculling.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class CameraUtil {

	private static int lastFrameUpdated = -1;
	private static Vec3d camera;

	public static Vec3d getCamera(int frame) {
		if (frame != lastFrameUpdated) {
			lastFrameUpdated = frame;
			Minecraft mc = Minecraft.getMinecraft();
			Entity e = mc.getRenderViewEntity();
			double d = mc.getRenderPartialTicks();
			double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * d;
			double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * d;
			double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * d;
			camera = ActiveRenderInfo.getCameraPosition().add(x, y, z);
		}
		return camera;
	}

}
