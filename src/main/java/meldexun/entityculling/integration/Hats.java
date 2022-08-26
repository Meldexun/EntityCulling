package meldexun.entityculling.integration;

import net.minecraft.entity.Entity;

public class Hats {

	public static boolean isHat(Entity entity) {
		return entity instanceof me.ichun.mods.hats.common.entity.EntityHat;
	}

}
