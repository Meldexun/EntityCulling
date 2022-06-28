package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.renderlib.util.MutableAABB;
import net.minecraft.tileentity.TileEntity;

@Mixin(value = TileEntity.class, priority = 1100)
public class MixinTileEntity {

	@Dynamic
	private MutableAABB cachedBoundingBox;

	@Inject(method = "updateCachedBoundingBox", at = @At(value = "INVOKE", target = "Lmeldexun/renderlib/util/MutableAABB;set(Lnet/minecraft/util/math/AxisAlignedBB;)Lmeldexun/renderlib/util/MutableAABB;", shift = Shift.AFTER))
	public void updateCachedBoundingBox(double partialTicks, CallbackInfo info) {
		if (EntityCulling.useOpenGlBasedCulling() && EntityCullingConfig.tileEntityAABBGrowth) {
			cachedBoundingBox.grow(0.03125D);
		}
	}

}
