package meldexun.entityculling.config;

import meldexun.entityculling.EntityCulling;
import net.minecraftforge.common.config.Config;

@Config(modid = EntityCulling.MOD_ID)
public class EntityCullingConfig {

	@Config.RequiresMcRestart
	@Config.Comment("Requires restart. Ram usage (in Bytes) = ((x * 16 * 2) ^ 3) / 4")
	public static int cacheSize = 12;

	@Config.Comment("Enabling this should give more FPS because (tile-) entities are culled more accuratly. Also this does only not need the cache but the cache still requires memory. This might cause (tile-) entity flickering and lags due to the higher CPU usage.")
	public static boolean cachelessMode = false;

	public static boolean debugCullInfo = false;
	public static boolean debugRenderBoxes = false;

	public static boolean disabledInSpectator = true;

	@Config.Comment("Disable all changes from this mod.")
	public static boolean enabled = true;

	@Config.Comment("If you feel the need to increase this value because of entities being culled falsely then another modder probably messed up his render bounding boxes and he should fix them instead.")
	@Config.RangeDouble(min = 0.0009765625D, max = 1024.0D)
	public static double raytraceThreshold = 1.0D;

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

	@Config.RequiresWorldRestart
	public static String[] tileEntityCachedBoundingBoxBlacklist = { "fairylights" };

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
