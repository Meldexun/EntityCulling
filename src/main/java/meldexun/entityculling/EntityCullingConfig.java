package meldexun.entityculling;

import net.minecraftforge.common.config.Config;

@Config(modid = EntityCullingContainer.MOD_ID)
public class EntityCullingConfig {

	@Config.Comment("Disable all changes from this mod.")
	public static boolean enabled = true;

	@Config.Comment("Skip rendering of entities that are not visible (hidden behind blocks). Bosses will be rendered normally. This might cause issues where an entity is partly behind a block and thus does not get rendered but it's usually not really noticable.")
	public static boolean skipHiddenEntityRendering = true;
	@Config.Comment("Entities with a width or height greater than this value will always get rendered.")
	@Config.RangeDouble(min = 0.0D, max = 256.0D)
	public static double skipHiddenEntityRenderingSize = 3.0D;
	@Config.Comment("Tile entities which will always be rendered. (Format: 'modid:entity_name')")
	public static String[] skipHiddenEntityRenderingBlacklist = new String[0];

	@Config.Comment("Skip rendering of entities that are not visible (hidden behind blocks). This might cause issues where a tile entity is partly behind a block and thus does not get rendered but it's usually not really noticable.")
	public static boolean skipHiddenTileEntityRendering = true;
	@Config.Comment("Tile entities with a width or height greater than this value will always get rendered.")
	@Config.RangeDouble(min = 0.0D, max = 256.0D)
	public static double skipHiddenTileEntityRenderingSize = 3.0D;
	@Config.Comment("Tile entities which will always be rendered. (Format: 'modid:tile_entity_name')")
	public static String[] skipHiddenTileEntityRenderingBlacklist = new String[0];

	private EntityCullingConfig() {

	}

}
