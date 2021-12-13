package meldexun.entityculling.integration;

import net.minecraft.entity.Entity;

public class FairyLights {

	public static boolean isFairyLightEntity(Entity entity) {
		return entity instanceof com.pau101.fairylights.server.entity.EntityFenceFastener;
	}

}
