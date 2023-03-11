package meldexun.entityculling.config;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.util.raytracing.RaytraceDistanceCalculator;
import meldexun.renderlib.util.ResourceLocationMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Config;

@Config(modid = EntityCulling.MOD_ID)
public class EntityCullingConfig {

	@Config.RequiresMcRestart
	@Config.Comment("Ideally should be set to equal the render distance. Ram usage (in Bytes) = 1063 * (2x + 1) ^ 3")
	public static int cacheSize = 12;

	public static boolean debugRenderBoxes = false;

	public static boolean disabledInSpectator = true;

	@Config.RequiresWorldRestart
	@Config.Comment("Disable all changes from this mod (This is not equal to removing the mod!).")
	public static boolean enabled = true;

	@Config.Comment("If you have a weak CPU enabling this option might help reducing the CPU usage.")
	public static boolean enableRaytraceCache = false;

	@Config.Comment("If enabled and OpenGl 4.4 is supported OpenGl based culling is used which is a lot faster and more accurate. If you have a weak GPU you might want to disable this.")
	public static boolean openglBasedCulling = true;

	@Config.Comment("Mode that is used to calculate the distance from camera to a raytrace end point.")
	public static RaytraceDistanceCalculator raytraceDistanceCalculator = RaytraceDistanceCalculator.SPHERICAL;

	@Config.Comment("Used to calculate the raytrace distance limit. Points farther away than the limit are not raytraced. Distance limit = (renderDistance * 16 + adder) * multiplier")
	@Config.RangeDouble(min = 0.0D, max = 1024.0D)
	public static double raytraceDistanceLimitAdder = 16.0D;

	@Config.Comment("Used to calculate the raytrace distance limit. Points farther away than the limit are not raytraced. Distance limit = (renderDistance * 16 + adder) * multiplier")
	@Config.RangeDouble(min = 0.0D, max = 1024.0D)
	public static double raytraceDistanceLimitMultiplier = 1.0D;

	@Config.Comment("If you feel the need to increase this value because of entities being culled falsely then another modder probably messed up their render bounding boxes and you should report the issue to them. Alternatively you can use the (tile-)entityBoundingBoxGrowthList settings to fix bounding boxes on your own.")
	@Config.RangeDouble(min = 0.0D, max = 1024.0D)
	public static double raytraceThreshold = 1.0D;

	@Config.Comment("If enabled tile entity bounding boxes are increased slightly to avoid issues when other mods don't correctly set their bounding boxes (requires opengl based culling). If you still have culling or flickering issues you can use the 'debugRenderBoxes', 'entityBoundingBoxGrowthList' and 'tileEntityBoundingBoxGrowthList' config options to try to fix the bounding box of that entity or tile entity.")
	public static boolean tileEntityAABBGrowth = true;

	public static EntityOptions entity = new EntityOptions();
	public static TileEntityOptions tileEntity = new TileEntityOptions();
	public static OptifineShaderOptions optifineShaderOptions = new OptifineShaderOptions();

	private EntityCullingConfig() {

	}

	public static void onConfigChanged() {
		entity.skipHiddenEntityRenderingBlacklistImpl.load(entity.skipHiddenEntityRenderingBlacklist);
		tileEntity.skipHiddenTileEntityRenderingBlacklistImpl.load(tileEntity.skipHiddenTileEntityRenderingBlacklist);
	}

	public static class EntityOptions {

		public boolean alwaysRenderBosses = true;
		public boolean alwaysRenderEntitiesWithName = true;
		public boolean alwaysRenderPlayers = true;
		public boolean alwaysRenderViewEntity = true;
		@Config.Comment("Skip rendering of entities that are not visible (hidden behind blocks). This might cause issues where an entity is partly behind a block and thus does not get rendered but it's usually not really noticable.")
		public boolean skipHiddenEntityRendering = true;
		@Config.Comment("Entities with a width or height greater than this value will always get rendered.")
		@Config.RangeDouble(min = 0.0D, max = 1024.0D)
		public double skipHiddenEntityRenderingSize = 16.0D;
		@Config.Comment("Tile entities which will always be rendered. (Accepts 'modid' or 'modid:entity_name')")
		public String[] skipHiddenEntityRenderingBlacklist = new String[0];
		@Config.Ignore
		public ResourceLocationMap<Entity, Boolean> skipHiddenEntityRenderingBlacklistImpl = new ResourceLocationMap<>(EntityList::getKey, false, s -> true);

	}

	public static class TileEntityOptions {

		@Config.Comment("Skip rendering of entities that are not visible (hidden behind blocks). This might cause issues where a tile entity is partly behind a block and thus does not get rendered but it's usually not really noticable.")
		public boolean skipHiddenTileEntityRendering = true;
		@Config.Comment("Tile entities with a width or height greater than this value will always get rendered.")
		@Config.RangeDouble(min = 0.0D, max = 1024.0D)
		public double skipHiddenTileEntityRenderingSize = 16.0D;
		@Config.Comment("Tile entities which will always be rendered. (Accepts 'modid' or 'modid:tile_entity_name')")
		public String[] skipHiddenTileEntityRenderingBlacklist = { "enderio:tile_travel_anchor" };
		@Config.Ignore
		public ResourceLocationMap<TileEntity, Boolean> skipHiddenTileEntityRenderingBlacklistImpl = new ResourceLocationMap<>(TileEntity.REGISTRY::getNameForObject, false, s -> true);

	}

	public static class OptifineShaderOptions {

		public boolean entityShadowsCulling = true;
		public boolean entityShadowsCullingLessAggressiveMode = true;
		@Config.RangeDouble(min = 0.0D, max = 1024.0D)
		public double entityShadowsCullingLessAggressiveModeDiff = 4.0D;

		public boolean tileEntityShadowsCulling = true;
		public boolean tileEntityShadowsCullingLessAggressiveMode = true;
		@Config.RangeDouble(min = 0.0D, max = 1024.0D)
		public double tileEntityShadowsCullingLessAggressiveModeDiff = 4.0D;

	}

}
