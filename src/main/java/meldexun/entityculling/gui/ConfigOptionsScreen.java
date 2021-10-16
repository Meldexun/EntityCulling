package meldexun.entityculling.gui;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.TextComponent;

public class ConfigOptionsScreen extends AbstractConfigScreen {

	public ConfigOptionsScreen(Screen lastScreen) {
		super(new TextComponent("options.entity_culling.config.title"), lastScreen);
	}

	@Override
	protected void initOptions(OptionsList list) {
		list.addBig(this.createIntSliderOption(EntityCullingConfig.CLIENT_CONFIG.cacheSize, 1, 32, 1));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.debug));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.enabled));

		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRendering));
		list.addBig(this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingSize, 0.0D, 128.0D, 0.1D));
		// list.addBig(this.createStringOption(EntityCullingConfig.CLIENT_CONFIG.skipHiddenEntityRenderingBlacklist));

		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRendering));
		list.addBig(this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingSize, 0.0D, 128.0D, 0.1D));
		// list.addBig(this.createStringOption(EntityCullingConfig.CLIENT_CONFIG.skipHiddenTileEntityRenderingBlacklist));

		// list.addBig(this.createSubmenuOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions, () -> new
		// ShaderOptionsScreen(this)));
	}

}
