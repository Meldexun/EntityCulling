package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.ITileEntityRendererCache;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

	@Redirect(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	public boolean isBlockLoaded(World world, BlockPos pos, boolean allowEmpty) {
		if (!EntityCullingConfig.enabled) {
			return world.isBlockLoaded(pos, allowEmpty);
		}

		return true;
	}

	@Redirect(method = "render(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;"))
	public TileEntitySpecialRenderer<TileEntity> getRenderer(TileEntityRendererDispatcher tileEntityRenderDispatcher, TileEntity tileEntity) {
		if (!EntityCullingConfig.enabled) {
			return tileEntityRenderDispatcher.getRenderer(tileEntity);
		}

		return ((ITileEntityRendererCache) tileEntity).getRenderer();
	}

}
