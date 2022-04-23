package meldexun.entityculling.util;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

public interface IEntityRendererCache {

	default boolean hasRenderer() {
		return getRenderer() != null;
	}

	@Nullable
	<T extends Entity> Render<T> getRenderer();

	@Nullable
	default <T extends Entity> Render<T> loadRenderer(Entity entity) {
		Minecraft mc = Minecraft.getMinecraft();
		RenderManager renderManager = mc.getRenderManager();
		Render<T> renderer = renderManager.getEntityRenderObject(entity);
		return renderer.getClass() == RenderEntity.class ? null : renderer;
	}

}
