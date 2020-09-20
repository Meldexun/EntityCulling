package meldexun.entityculling;

import net.minecraftforge.common.config.Config;

@Config(modid = EntityCulling.MOD_ID)
public class ModConfig {

	@Config.Comment("Only render the nearest 'limitEntityRenderingCount' amount of living entities. Bosses will be rendered normally.")
	public static boolean limitEntityRendering = true;
	@Config.Comment("The maximum amount of entities that get rendered.")
	@Config.RangeInt(min = 0, max = 1000)
	public static int limitEntityRenderingCount = 64;
	@Config.Comment("Skip rendering of entities that are not visible (hidden behind blocks). Bosses will be rendered normally. This probably might cause issues where a mob should be render but it won't.")
	public static boolean skipHiddenEntityRendering = true;
	@Config.Comment("It raytraces from the eyes of the player to the eyes of the mob and the other way around. Then it compares the positions that were hit and only renders the entity when the distance between the two points is lower than this setting.")
	@Config.RangeInt(min = 0, max = 100)
	public static int skipHiddenEntityRenderingDiff = 16;
	@Config.Comment("Entities with a width or height equal to or greater than this value will always get rendered.")
	@Config.RangeDouble(min = 0.0D, max = 100.0D)
	public static double maxEntitySizeToSkipRendering = 3.0D;
	@Config.Comment("Entities which will always get rendered. (Format: 'modid:entity_name')")
	public static String[] blacklist = {};

}
