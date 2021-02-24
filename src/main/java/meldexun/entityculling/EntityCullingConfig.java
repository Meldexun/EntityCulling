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

		public final ForgeConfigSpec.BooleanValue enabled;

		public final ForgeConfigSpec.BooleanValue skipHiddenEntityRendering;
		public final ForgeConfigSpec.DoubleValue skipHiddenEntityRenderingSize;
		public final ForgeConfigSpec.DoubleValue skipHiddenEntityRenderingDiff;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> skipHiddenEntityRenderingBlacklist;

		public final ForgeConfigSpec.BooleanValue skipHiddenTileEntityRendering;
		public final ForgeConfigSpec.DoubleValue skipHiddenTileEntityRenderingSize;
		public final ForgeConfigSpec.DoubleValue skipHiddenTileEntityRenderingDiff;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> skipHiddenTileEntityRenderingBlacklist;

		public ClientConfig(ForgeConfigSpec.Builder builder) {
			this.enabled = builder.comment("").define("enabled", true);

			this.skipHiddenEntityRendering = builder.comment("").define("skipHiddenEntityRendering", true);
			this.skipHiddenEntityRenderingSize = builder.comment("").defineInRange("skipHiddenEntityRenderingSize", 3.0D, 0.0D, 128.0D);
			this.skipHiddenEntityRenderingDiff = builder.comment("").defineInRange("skipHiddenEntityRenderingDiff", 16.0D, 0.0D, 128.0D);
			this.skipHiddenEntityRenderingBlacklist = builder.comment("").defineList("skipHiddenEntityRenderingBlacklist", Lists.newArrayList(), o -> true);

			this.skipHiddenTileEntityRendering = builder.comment("").define("skipHiddenTileEntityRendering", true);
			this.skipHiddenTileEntityRenderingSize = builder.comment("").defineInRange("skipHiddenTileEntityRenderingSize", 3.0D, 0.0D, 128.0D);
			this.skipHiddenTileEntityRenderingDiff = builder.comment("").defineInRange("skipHiddenTileEntityRenderingDiff", 16.0D, 0.0D, 128.0D);
			this.skipHiddenTileEntityRenderingBlacklist = builder.comment("").defineList("skipHiddenTileEntityRenderingBlacklist", Lists.newArrayList(), o -> true);
		}

	}

}
