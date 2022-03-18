package meldexun.entityculling.config;

import meldexun.entityculling.EntityCulling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;

@Config(modid = EntityCulling.MOD_ID)
public class EntityCullingConfig {

	@Config.RequiresMcRestart
	@Config.Comment("Ideally should be set to equal the render distance. Ram usage (in Bytes) = 1063 * (2x + 1) ^ 3")
	public static int cacheSize = 12;

	public static boolean debugCullInfo = false;
	public static boolean debugRenderBoxes = false;

	public static boolean disabledInSpectator = true;

	@Config.RequiresWorldRestart
	@Config.Comment("Disable all changes from this mod.")
	public static boolean enabled = true;

	@Config.Comment("When enabled and OpenGl 4.4 is supported OpenGl based culling is used which is a lot faster and more accurate.")
	public static boolean openglBasedCulling = true;

	@Config.Comment("If you feel the need to increase this value because of entities being culled falsely then another modder probably messed up his render bounding boxes and he should fix them instead.")
	@Config.RangeDouble(min = 0.0009765625D, max = 1024.0D)
	public static double raytraceThreshold = 1.0D;

	@Config.Comment("When enabled tile entity bounding boxes are increased slightly to avoid issues when other mods don't correctly set their bounding boxes (requires opengl based culling). If you still have culling or flickering issues you can use the 'debugRenderBoxes', 'entityBoundingBoxGrowthList' and 'tileEntityBoundingBoxGrowthList' config options to try to fix the bounding box of that entity or tile entity.")
	public static boolean tileEntityAABBGrowth = true;

	@Config.Comment("Most tile entities have static bounding boxes and thus they can be cached. Tile entities whose bounding boxes are likely to change every frame or so should be added to the blacklist. Tile entities whose bounding only change every once in a while should be covered by cache updates (update speed adjustable through tileEntityCachedBoundingBoxUpdateInterval)")
	public static boolean tileEntityCachedBoundingBoxEnabled = true;
	@Config.Comment("Every frame there is a 1 in x chance to update the cached bounding box. Higher = better performance, Lower = tile entities with dynamic bounding boxes get updated faster.")
	@Config.RangeInt(min = 1, max = 1_000_000)
	public static int tileEntityCachedBoundingBoxUpdateInterval = 100;
	@Config.Comment("Tile entities whose bounding boxes won't be cached (Accepts 'modid' or 'modid:tileentity').")
	public static String[] tileEntityCachedBoundingBoxBlacklist = new String[0];
	@Config.Ignore
	public static ResourceLocationMap<TileEntity, Boolean> tileEntityCachedBoundingBoxBlacklistImpl = new ResourceLocationMap<>(TileEntity.REGISTRY::getNameForObject, false, s -> true);

	public static EntityOptions entity = new EntityOptions();
	public static TileEntityOptions tileEntity = new TileEntityOptions();
	public static OptifineShaderOptions optifineShaderOptions = new OptifineShaderOptions();

	private EntityCullingConfig() {

	}

	public static void onConfigChanged() {
		tileEntityCachedBoundingBoxBlacklistImpl.load(tileEntityCachedBoundingBoxBlacklist);
		entity.entityBoundingBoxGrowthListImpl.load(entity.entityBoundingBoxGrowthList);
		tileEntity.tileEntityBoundingBoxGrowthListImpl.load(tileEntity.tileEntityBoundingBoxGrowthList);
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
		@Config.RangeDouble(min = 0.0D, max = 256.0D)
		public double skipHiddenEntityRenderingSize = 16.0D;
		@Config.Comment("Tile entities which will always be rendered. (Accepts 'modid' or 'modid:entity_name')")
		public String[] skipHiddenEntityRenderingBlacklist = new String[0];
		@Config.Ignore
		public ResourceLocationMap<Entity, Boolean> skipHiddenEntityRenderingBlacklistImpl = new ResourceLocationMap<>(EntityList::getKey, false, s -> true);
		@Config.Comment("Allows you to increase the render bounding boxes of entities (or all entities of a mod). Width increases the size on the X and Z axis. Top increases the size in the positive Y direction. Bottom increases the size in the negative Y direction. (Accepts 'modid=width,top,bottom' or 'modid:entity=width,top,bottom').")
		public String[] entityBoundingBoxGrowthList = new String[0];
		@Config.Ignore
		public ResourceLocationMap<Entity, Vec3d> entityBoundingBoxGrowthListImpl = new ResourceLocationMap<>(EntityList::getKey, null, s -> {
			double x = s.length >= 1 ? Double.parseDouble(s[0]) : 0.0D;
			double y = s.length >= 2 ? Double.parseDouble(s[1]) : 0.0D;
			double z = s.length >= 3 ? Double.parseDouble(s[2]) : 0.0D;
			return x != 0.0D || y != 0.0D || z != 0.0D ? new Vec3d(x, y, z) : null;
		});

	}

	public static class TileEntityOptions {

		@Config.Comment("Skip rendering of entities that are not visible (hidden behind blocks). This might cause issues where a tile entity is partly behind a block and thus does not get rendered but it's usually not really noticable.")
		public boolean skipHiddenTileEntityRendering = true;
		@Config.Comment("Tile entities with a width or height greater than this value will always get rendered.")
		@Config.RangeDouble(min = 0.0D, max = 256.0D)
		public double skipHiddenTileEntityRenderingSize = 16.0D;
		@Config.Comment("Tile entities which will always be rendered. (Accepts 'modid' or 'modid:tile_entity_name')")
		public String[] skipHiddenTileEntityRenderingBlacklist = new String[0];
		@Config.Ignore
		public ResourceLocationMap<TileEntity, Boolean> skipHiddenTileEntityRenderingBlacklistImpl = new ResourceLocationMap<>(TileEntity.REGISTRY::getNameForObject, false, s -> true);
		@Config.Comment("Allows you to increase the render bounding boxes of tile entities (or all entities of a mod). Width increases the size on the X and Z axis. Top increases the size in the positive Y direction. Bottom increases the size in the negative Y direction. (Accepts 'modid=width,top,bottom' or 'modid:tileentity=width,top,bottom').")
		public String[] tileEntityBoundingBoxGrowthList = new String[0];
		@Config.Ignore
		public ResourceLocationMap<TileEntity, Vec3d> tileEntityBoundingBoxGrowthListImpl = new ResourceLocationMap<>(TileEntity.REGISTRY::getNameForObject, null, s -> {
			double x = s.length >= 1 ? Double.parseDouble(s[0]) : 0.0D;
			double y = s.length >= 2 ? Double.parseDouble(s[1]) : 0.0D;
			double z = s.length >= 3 ? Double.parseDouble(s[2]) : 0.0D;
			return x != 0.0D || y != 0.0D || z != 0.0D ? new Vec3d(x, y, z) : null;
		});

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
