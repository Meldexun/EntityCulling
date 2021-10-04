package meldexun.entityculling.asm.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

public class MinecraftHook {

	public static int getLimitFramerate() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.world == null ? MathHelper.clamp(mc.gameSettings.limitFramerate, 30, 240) : mc.gameSettings.limitFramerate;
	}

}
