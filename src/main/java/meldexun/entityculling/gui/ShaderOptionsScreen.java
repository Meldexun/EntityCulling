package meldexun.entityculling.gui;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.TextComponent;

public class ShaderOptionsScreen extends AbstractConfigScreen {

	public ShaderOptionsScreen(Screen lastScreen) {
		super(new TextComponent("options.entity_culling.shadows.title"), lastScreen);
	}

	@Override
	protected void initOptions(OptionsList list) {
		// ----- entity shadow options ----- //
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsEnabled));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsDistanceLimited));
		list.addBig(this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsMaxDistance, 0.0D, 64.0D, 1.0D));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCulling));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveMode));
		list.addBig(this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff, 0.0D,
				32.0D, 1.0D));

		list.addBig(this.createDummyOption());

		// ----- tile entity shadow options ----- //
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsEnabled));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsDistanceLimited));
		list.addBig(this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsMaxDistance, 0.0D, 64.0D, 1.0D));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCulling));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode));
		list.addBig(this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff, 0.0D,
				32.0D, 1.0D));

		list.addBig(this.createDummyOption());

		// ----- terrain shadow options ----- //
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsEnabled));
		list.addBig(this.createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsDistanceLimited));
		list.addBig(
				this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxHorizontalDistance, 0.0D, 64.0D, 1.0D));
		list.addBig(
				this.createDoubleSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxVerticalDistance, 0.0D, 64.0D, 1.0D));
	}

}
