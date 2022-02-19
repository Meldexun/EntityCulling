package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.IBoundingBoxCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

@Mixin(Render.class)
public class MixinRender {

	@Inject(method = "shouldRender", cancellable = true, at = @At("HEAD"))
	public void shouldRender(Entity entity, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> info) {
		if (!EntityCullingConfig.enabled) {
			return;
		}

		if (!entity.isInRangeToRender3d(camX, camY, camZ)) {
			info.setReturnValue(false);
			return;
		}

		if (entity.ignoreFrustumCheck) {
			info.setReturnValue(true);
			return;
		}

		AxisAlignedBB aabb = ((IBoundingBoxCache) entity).getCachedBoundingBox();

		if (aabb.hasNaN()) {
			info.setReturnValue(isBoxInFrustum(camera, entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D, entity.posZ + 2.0D));
			return;
		}

		info.setReturnValue(isBoxInFrustum(camera, aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D));
	}

	@Unique
	private static boolean isBoxInFrustum(ICamera frustum, double x0, double y0, double z0, double x1, double y1, double z1) {
		if (frustum instanceof Frustum) {
			return ((Frustum) frustum).isBoxInFrustum(x0, y0, z0, x1, y1, z1);
		} else {
			return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(x0, y0, z0, x1, y1, z1));
		}
	}

}
