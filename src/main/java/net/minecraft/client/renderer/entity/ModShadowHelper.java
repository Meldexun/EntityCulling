package net.minecraft.client.renderer.entity;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import meldexun.entityculling.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

public class ModShadowHelper {

	private static final Object2FloatMap<Class<? extends Entity>> SHADOW_SIZE_MAP = new Object2FloatOpenHashMap<>();

	public static void disableShadows() {
		Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Minecraft.getMinecraft().getRenderManager().entityRenderMap;
		for (Object2FloatMap.Entry<Class<? extends Entity>> entry : SHADOW_SIZE_MAP.object2FloatEntrySet()) {
			if (entityRenderMap.containsKey(entry.getKey())) {
				entityRenderMap.get(entry.getKey()).shadowSize = entry.getFloatValue();
			}
		}
		SHADOW_SIZE_MAP.clear();
		for (String s : ModConfig.entityShadowBlacklist) {
			Class<? extends Entity> entityClass = EntityList.getClassFromName(s);
			if (entityClass != null && entityRenderMap.containsKey(entityClass)) {
				SHADOW_SIZE_MAP.put(entityClass, entityRenderMap.get(entityClass).shadowSize);
				entityRenderMap.get(entityClass).shadowSize = 0.0F;
			}
		}
	}

}
