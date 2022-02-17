package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.renderer.culling.ClippingHelper;

@Mixin(ClippingHelper.class)
public class MixinClippingHelper {

	@Shadow
	private float[][] frustum;

	@Inject(method = "isBoxInFrustum", cancellable = true, at = @At("HEAD"))
	public void isBoxInFrustum(double x0, double y0, double z0, double x1, double y1, double z1, CallbackInfoReturnable<Boolean> info) {
		if (!EntityCullingConfig.enabled) {
			return;
		}

		for (float[] v : frustum) {
			if (dist(v[0], v[1], v[2], v[3], v[0] >= 0.0F ? x1 : x0, v[1] >= 0.0F ? y1 : y0, v[2] >= 0.0F ? z1 : z0) <= 0.0F) {
				info.setReturnValue(false);
				return;
			}
		}

		info.setReturnValue(true);
	}

	@Unique
	private static double dist(float planeX, float planeY, float planeZ, float planeW, double x, double y, double z) {
		return planeX * x + planeY * y + planeZ * z + planeW;
	}

}
