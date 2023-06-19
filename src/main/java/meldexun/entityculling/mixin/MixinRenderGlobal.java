package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.util.culling.CullingInstance;
import meldexun.renderlib.integration.Optifine;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.MinecraftForgeClient;

@Mixin(value = RenderGlobal.class, priority = 1100)
public class MixinRenderGlobal {

	/** {@link RenderGlobal#renderEntities(Entity, ICamera, float)} */
	@Inject(method = "renderEntities", at = @At("HEAD"))
	public void preEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo info) {
		if (RenderUtil.isRecursive()) {
			return;
		}
		if (EntityCulling.useOpenGlBasedCulling()
				&& MinecraftForgeClient.getRenderPass() == 0
				&& (!Optifine.isOptifineDetected() || !Optifine.isShadowPass())) {
			CullingInstance cullingInstance = CullingInstance.getInstance();
			cullingInstance.updateResults(RenderUtil.getProjectionModelViewMatrix());
		}
	}

}
