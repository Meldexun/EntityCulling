package meldexun.entityculling.asm.hook;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class RenderHook {

	public static boolean shouldRender(Render<?> render, Entity entity, ICamera camera, double camX, double camY, double camZ) {
		if (!entity.isInRangeToRender3d(camX, camY, camZ)) {
			return false;
		}

		if (entity.ignoreFrustumCheck) {
			return true;
		}

		AxisAlignedBB aabb = entity.getRenderBoundingBox();

		if (aabb.hasNaN()) {
			if (camera instanceof Frustum) {
				return ((Frustum) camera).isBoxInFrustum(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D,
						entity.posZ + 2.0D);
			} else {
				return camera.isBoundingBoxInFrustum(new AxisAlignedBB(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D,
						entity.posY + 2.0D, entity.posZ + 2.0D));
			}
		}

		if (camera instanceof Frustum) {
			return ((Frustum) camera).isBoxInFrustum(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D,
					aabb.maxZ + 0.5D);
		} else {
			return camera.isBoundingBoxInFrustum(
					new AxisAlignedBB(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D));
		}
	}

}
