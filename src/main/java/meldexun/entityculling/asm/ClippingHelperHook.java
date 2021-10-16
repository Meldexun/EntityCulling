package meldexun.entityculling.asm;

import com.mojang.math.Vector4f;

public class ClippingHelperHook {

	public static boolean cubeInFrustum(Vector4f[] frustumData, float x0, float y0, float z0, float x1, float y1, float z1) {
		for (Vector4f v : frustumData) {
			if (dist(v.x(), v.y(), v.z(), v.w(), v.x() >= 0.0F ? x1 : x0, v.y() >= 0.0F ? y1 : y0, v.z() >= 0.0F ? z1 : z0) <= 0.0F) {
				return false;
			}
		}
		return true;
	}

	private static float dist(float planeX, float planeY, float planeZ, float planeW, float x, float y, float z) {
		return planeX * x + planeY * y + planeZ * z + planeW;
	}

}
