package meldexun.entityculling;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraftforge.common.ForgeConfigSpec;

public class EntityCullingConfig {

	public static final ClientConfig CLIENT_CONFIG;
	public static final ForgeConfigSpec CLIENT_SPEC;
	static {
		final Pair<ClientConfig, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT_CONFIG = clientSpecPair.getLeft();
		CLIENT_SPEC = clientSpecPair.getRight();
	}

	public static class ClientConfig {

		public final ForgeConfigSpec.IntValue cacheSize;

		public final ForgeConfigSpec.BooleanValue debug;

		public final ForgeConfigSpec.BooleanValue enabled;

		public final ForgeConfigSpec.BooleanValue skipHiddenEntityRendering;
		public final ForgeConfigSpec.DoubleValue skipHiddenEntityRenderingSize;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> skipHiddenEntityRenderingBlacklist;

		public final ForgeConfigSpec.BooleanValue skipHiddenTileEntityRendering;
		public final ForgeConfigSpec.DoubleValue skipHiddenTileEntityRenderingSize;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> skipHiddenTileEntityRenderingBlacklist;

		public final OptifineShaderOptions optifineShaderOptions;

		public ClientConfig(ForgeConfigSpec.Builder builder) {
			this.cacheSize = builder.comment("Requires restart. Ram usage (in MB) = (x * 16 * 2) ^ 3 / 4").defineInRange("cacheSize", 16, 1, 32);

			this.debug = builder.comment("").define("debug", false);

			this.enabled = builder.comment("Disable all changes from this mod.").define("enabled", true);

			this.skipHiddenEntityRendering = builder.comment("Skip rendering of entities that are not visible (hidden behind blocks). Bosses will be rendered normally. This might cause issues where an entity is partly behind a block and thus does not get rendered but it's usually not really noticable.").define("skipHiddenEntityRendering", true);
			this.skipHiddenEntityRenderingSize = builder.comment("Entities with a width or height greater than this value will always get rendered.").defineInRange("skipHiddenEntityRenderingSize", 3.0D, 0.0D, 128.0D);
			this.skipHiddenEntityRenderingBlacklist = builder.comment("Entities which will always be rendered. (Format: 'modid:entity_name')").defineList("skipHiddenEntityRenderingBlacklist", Lists.newArrayList(), o -> true);

			this.skipHiddenTileEntityRendering = builder.comment("Skip rendering of entities that are not visible (hidden behind blocks). This might cause issues where a tile entity is partly behind a block and thus does not get rendered but it's usually not really noticable.").define("skipHiddenTileEntityRendering", true);
			this.skipHiddenTileEntityRenderingSize = builder.comment("Tile entities with a width or height greater than this value will always get rendered.").defineInRange("skipHiddenTileEntityRenderingSize", 3.0D, 0.0D, 128.0D);
			this.skipHiddenTileEntityRenderingBlacklist = builder.comment("Tile entities which will always be rendered. (Format: 'modid:tile_entity_name')").defineList("skipHiddenTileEntityRenderingBlacklist", Lists.newArrayList(), o -> true);

			this.optifineShaderOptions = new OptifineShaderOptions(builder);
		}

		public static class OptifineShaderOptions {

			public final ForgeConfigSpec.BooleanValue entityShadowsCulling;
			public final ForgeConfigSpec.BooleanValue entityShadowsCullingLessAggressiveMode;
			public final ForgeConfigSpec.DoubleValue entityShadowsCullingLessAggressiveModeDiff;
			public final ForgeConfigSpec.BooleanValue entityShadowsDisabled;
			public final ForgeConfigSpec.BooleanValue entityShadowsDistanceLimited;
			public final ForgeConfigSpec.DoubleValue entityShadowsMaxDistance;

			public final ForgeConfigSpec.BooleanValue terrainShadowsDisabled;
			public final ForgeConfigSpec.BooleanValue terrainShadowsDistanceLimited;
			public final ForgeConfigSpec.DoubleValue terrainShadowsMaxHorizontalDistance;
			public final ForgeConfigSpec.DoubleValue terrainShadowsMaxVerticalDistance;

			public final ForgeConfigSpec.BooleanValue tileEntityShadowsCulling;
			public final ForgeConfigSpec.BooleanValue tileEntityShadowsCullingLessAggressiveMode;
			public final ForgeConfigSpec.DoubleValue tileEntityShadowsCullingLessAggressiveModeDiff;
			public final ForgeConfigSpec.BooleanValue tileEntityShadowsDisabled;
			public final ForgeConfigSpec.BooleanValue tileEntityShadowsDistanceLimited;
			public final ForgeConfigSpec.DoubleValue tileEntityShadowsMaxDistance;

			public OptifineShaderOptions(ForgeConfigSpec.Builder builder) {
				builder.push("optifineShaderOptions");

				this.entityShadowsCulling = builder.comment("").define("entityShadowsCulling", true);
				this.entityShadowsCullingLessAggressiveMode = builder.comment("").define("entityShadowsCullingLessAggressiveMode", false);
				this.entityShadowsCullingLessAggressiveModeDiff = builder.comment("").defineInRange("entityShadowsCullingLessAggressiveModeDiff", 4.0D, 0.0D, 128.0D);
				this.entityShadowsDisabled = builder.comment("").define("entityShadowsDisabled", false);
				this.entityShadowsDistanceLimited = builder.comment("").define("entityShadowsDistanceLimited", false);
				this.entityShadowsMaxDistance = builder.comment("").defineInRange("entityShadowsMaxDistance", 64.0D, 0.0D, 1024.0D);

				this.terrainShadowsDisabled = builder.comment("").define("terrainShadowsDisabled", false);
				this.terrainShadowsDistanceLimited = builder.comment("").define("terrainShadowsDistanceLimited", false);
				this.terrainShadowsMaxHorizontalDistance = builder.comment("").defineInRange("terrainShadowsMaxHorizontalDistance", 128.0D, 0.0D, 1024.0D);
				this.terrainShadowsMaxVerticalDistance = builder.comment("").defineInRange("terrainShadowsMaxVerticalDistance", 64.0D, 0.0D, 1024.0D);

				this.tileEntityShadowsCulling = builder.comment("").define("tileEntityShadowsCulling", true);
				this.tileEntityShadowsCullingLessAggressiveMode = builder.comment("").define("tileEntityShadowsCullingLessAggressiveMode", false);
				this.tileEntityShadowsCullingLessAggressiveModeDiff = builder.comment("").defineInRange("tileEntityShadowsCullingLessAggressiveModeDiff", 4.0D, 0.0D, 128.0D);
				this.tileEntityShadowsDisabled = builder.comment("").define("tileEntityShadowsDisabled", false);
				this.tileEntityShadowsDistanceLimited = builder.comment("").define("tileEntityShadowsDistanceLimited", false);
				this.tileEntityShadowsMaxDistance = builder.comment("").defineInRange("tileEntityShadowsMaxDistance", 64.0D, 0.0D, 1024.0D);

				builder.pop();
			}

		}

	}

	private EntityCullingConfig() {

	}

}
