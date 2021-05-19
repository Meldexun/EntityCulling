package meldexun.entityculling;

import net.minecraftforge.common.config.Config;

@Config(modid = EntityCullingContainer.MOD_ID)
public class EntityCullingConfig {

	@Config.Ignore
	public static boolean betaFeatures = false;

	@Config.RequiresMcRestart
	@Config.Comment("Requires restart. Ram usage (in MB) = (x * 16 * 2) ^ 3 / 4")
	public static int cacheSize = 12;

	public static boolean debug = false;

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

	public static OptifineShaderOptions optifineShaderOptions = new OptifineShaderOptions();

	private EntityCullingConfig() {

	}

	public static class OptifineShaderOptions {

		public boolean entityShadowsCulling = true;
		public boolean entityShadowsCullingLessAggressiveMode = true;
		@Config.RangeDouble(min = 0.0D, max = 64.0D)
		public double entityShadowsCullingLessAggressiveModeDiff = 4.0D;
		public boolean entityShadowsEnabled = true;
		public boolean entityShadowsDistanceLimited = true;
		@Config.RangeDouble(min = 0.0D, max = 64.0D)
		public double entityShadowsMaxDistance = 4.0D;

		public boolean terrainShadowsEnabled = true;
		public boolean terrainShadowsDistanceLimited = true;
		@Config.RangeDouble(min = 0.0D, max = 64.0D)
		public double terrainShadowsMaxHorizontalDistance = 8.0D;
		@Config.RangeDouble(min = 0.0D, max = 64.0D)
		public double terrainShadowsMaxVerticalDistance = 4.0D;

		public boolean tileEntityShadowsCulling = true;
		public boolean tileEntityShadowsCullingLessAggressiveMode = true;
		@Config.RangeDouble(min = 0.0D, max = 64.0D)
		public double tileEntityShadowsCullingLessAggressiveModeDiff = 4.0D;
		public boolean tileEntityShadowsEnabled = true;
		public boolean tileEntityShadowsDistanceLimited = true;
		@Config.RangeDouble(min = 0.0D, max = 64.0D)
		public double tileEntityShadowsMaxDistance = 4.0D;

	}

}
