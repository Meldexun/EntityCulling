package meldexun.entityculling.asm.hook;

import net.minecraft.client.renderer.culling.ClippingHelper;

public class ClippingHelperHook {

	public static boolean isBoxInFrustum(ClippingHelper clippingHelper, double x0, double y0, double z0, double x1, double y1, double z1) {
		for (float[] v : clippingHelper.frustum) {
			if (dist(v[0], v[1], v[2], v[3], v[0] >= 0.0F ? x1 : x0, v[1] >= 0.0F ? y1 : y0, v[2] >= 0.0F ? z1 : z0) <= 0.0F) {
				return false;
			}
		}
		return true;
	}

	private static double dist(float planeX, float planeY, float planeZ, float planeW, double x, double y, double z) {
		return planeX * x + planeY * y + planeZ * z + planeW;
	}

}
