package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.IEntityRendererCache;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

	@Redirect(method = "isRenderMultipass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;getEntityRenderObject(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/renderer/entity/Render;"))
	public Render<Entity> isRenderMultipass(RenderManager renderManager, Entity entityIn) {
		if (!EntityCullingConfig.enabled) {
			return renderManager.getEntityRenderObject(entityIn);
		}

		return ((IEntityRendererCache) entityIn).getRenderer();
	}

	@Redirect(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;getEntityRenderObject(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/renderer/entity/Render;"))
	public Render<Entity> shouldRender(RenderManager renderManager, Entity entityIn) {
		if (!EntityCullingConfig.enabled) {
			return renderManager.getEntityRenderObject(entityIn);
		}

		return ((IEntityRendererCache) entityIn).getRenderer();
	}

	@Redirect(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;getEntityRenderObject(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/renderer/entity/Render;"))
	public Render<Entity> renderEntity(RenderManager renderManager, Entity entityIn) {
		if (!EntityCullingConfig.enabled) {
			return renderManager.getEntityRenderObject(entityIn);
		}

		return ((IEntityRendererCache) entityIn).getRenderer();
	}

	@Redirect(method = "renderMultipass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;getEntityRenderObject(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/renderer/entity/Render;"))
	public Render<Entity> renderMultipass(RenderManager renderManager, Entity entityIn) {
		if (!EntityCullingConfig.enabled) {
			return renderManager.getEntityRenderObject(entityIn);
		}

		return ((IEntityRendererCache) entityIn).getRenderer();
	}

}
