package meldexun.entityculling.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.entityculling.asm.hook.RenderGlobalHook;
import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

	@Inject(method = "setupTerrain", at = @At("HEAD"))
	public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo info) {
		if (!EntityCullingConfig.enabled) {
			return;
		}

		RenderGlobalHook.setup(partialTicks, camera, frameCount);
	}

	@Inject(method = "updateTileEntities", cancellable = true, at = @At("HEAD"))
	public void updateTileEntities(Collection<TileEntity> tileEntitiesToRemove, Collection<TileEntity> tileEntitiesToAdd, CallbackInfo info) {
		if (!EntityCullingConfig.enabled) {
			return;
		}

		info.cancel();
	}

}
