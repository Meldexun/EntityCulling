package net.minecraft.client.renderer.entity;

import meldexun.entityculling.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;

public class ModShadowHelper {

	public static void disableShadows() {
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		setShadowSize(renderManager.entityRenderMap.get(EntityItem.class), ModConfig.disableItemEntityShadows ? 0.0F : 0.15F);
		setShadowSize(renderManager.entityRenderMap.get(EntityXPOrb.class), ModConfig.disableXPOrbEntityShadows ? 0.0F : 0.15F);
	}

	private static void setShadowSize(Render<?> render, float shadowSize) {
		render.shadowSize = shadowSize;
	}

}
