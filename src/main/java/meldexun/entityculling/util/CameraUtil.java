package meldexun.entityculling.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class CameraUtil {

	private static double prevFrameTickTime;
	private static double frameTickTime;
	private static double deltaFrameTickTime;
	private static Vec3d prevCamera;
	private static Vec3d camera;
	private static Vec3d deltaCamera;

	public static void update() {
		Minecraft mc = Minecraft.getMinecraft();
		Entity e = mc.getRenderViewEntity();
		if (e == null) {
			return;
		}
		prevFrameTickTime = frameTickTime;
		frameTickTime = mc.getRenderPartialTicks();
		deltaFrameTickTime = frameTickTime - prevFrameTickTime;
		if (deltaFrameTickTime < 0.0D) {
			deltaFrameTickTime += 1.0D;
		}
		double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * frameTickTime;
		double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * frameTickTime;
		double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * frameTickTime;
		prevCamera = camera;
		camera = ActiveRenderInfo.getCameraPosition().add(x, y, z);
		if (prevCamera == null) {
			prevCamera = camera;
		}
		deltaCamera = camera.subtract(prevCamera);
	}

	public static Vec3d getCamera() {
		return camera;
	}

	public static Vec3d getPrevCamera() {
		return prevCamera;
	}

	public static Vec3d getDeltaCamera() {
		return deltaCamera;
	}

	public static double getFrameTickTime() {
		return frameTickTime;
	}

	public static double getPrevFrameTickTime() {
		return prevFrameTickTime;
	}

	public static double getDeltaFrameTickTime() {
		return deltaFrameTickTime;
	}

}
