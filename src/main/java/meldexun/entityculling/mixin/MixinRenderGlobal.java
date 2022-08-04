package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.BoundingBoxHelper;
import meldexun.entityculling.util.culling.CullingInstance;
import meldexun.reflectionutil.ReflectionField;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.MinecraftForgeClient;

@Mixin(value = RenderGlobal.class)
public class MixinRenderGlobal {

	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");

	/** {@link RenderGlobal#renderEntities(Entity, ICamera, float)} */
	@Inject(method = "renderEntities", at = @At("RETURN"))
	public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo info) {
		if (RenderUtil.isRecursive()) {
			return;
		}
		if (EntityCulling.useOpenGlBasedCulling()
				&& MinecraftForgeClient.getRenderPass() == 1
				&& (!EntityCullingClassTransformer.OPTIFINE_DETECTED || !IS_SHADOW_PASS.getBoolean(null))) {
			CullingInstance cullingInstance = CullingInstance.getInstance();
			cullingInstance.updateResults(RenderUtil.getProjectionModelViewMatrix());
		}
		if (EntityCullingConfig.debugRenderBoxes
				&& MinecraftForgeClient.getRenderPass() == 1
				&& (!EntityCullingClassTransformer.OPTIFINE_DETECTED || !IS_SHADOW_PASS.getBoolean(null))) {
			BoundingBoxHelper.getInstance().drawRenderBoxes(partialTicks);
		}
	}

}
