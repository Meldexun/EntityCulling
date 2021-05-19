package meldexun.entityculling.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import meldexun.entityculling.EntityCullingConfig;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public class ShaderOptionsScreen extends Screen {

	private final Screen lastScreen;
	private OptionsRowList list;

	public ShaderOptionsScreen(Screen lastScreen) {
		super(new TranslationTextComponent("options.shadows.title"));
		this.lastScreen = lastScreen;
	}

	@Override
	protected void init() {
		super.init();

		this.list = new OptionsRowList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		// ----- entity shadow options ----- //
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsEnabled));
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsDistanceLimited));
		this.list.addBig(createSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsMaxDistance, 0.0D, 64.0D, 1.0D));
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCulling));
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveMode));
		this.list.addBig(createSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff, 0.0D, 64.0D, 1.0D));
		
		// ----- tile entity shadow options ----- //
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsEnabled));
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsDistanceLimited));
		this.list.addBig(createSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsMaxDistance, 0.0D, 64.0D, 1.0D));
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCulling));
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode));
		this.list.addBig(createSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff, 0.0D, 64.0D, 1.0D));
		
		// ----- terrain shadow options ----- //
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsEnabled));
		this.list.addBig(createBooleanOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsDistanceLimited));
		this.list.addBig(createSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxHorizontalDistance, 0.0D, 64.0D, 1.0D));
		this.list.addBig(createSliderOption(EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxVerticalDistance, 0.0D, 64.0D, 1.0D));
		this.children.add(this.list);

		/*
		// ----- entity shadow options ----- //
		int i1 = 0;
		this.addButton(new ConfigBooleanButton(this.width / 2 - 155, this.height / 6 + (i1++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsDisabled));
		this.addButton(new ConfigBooleanButton(this.width / 2 - 155, this.height / 6 + (i1++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsDistanceLimited));
		this.addButton(new ConfigSlider(this.width / 2 - 155, this.height / 6 + (i1++ * 22), 128, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsMaxDistance, 0.0D, 1024.0D, 16.0D));
		this.addButton(new ConfigBooleanButton(this.width / 2 - 155, this.height / 6 + (i1++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCulling));
		this.addButton(new ConfigBooleanButton(this.width / 2 - 155, this.height / 6 + (i1++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveMode));
		this.addButton(new ConfigSlider(this.width / 2 - 155, this.height / 6 + (i1++ * 22), 128, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.entityShadowsCullingLessAggressiveModeDiff, 0.0D, 128.0D, 1.0D));

		// ----- tile entity shadow options ----- //
		int i2 = 0;
		this.addButton(new ConfigBooleanButton(this.width / 2 + 5, this.height / 6 + (i2++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsDisabled));
		this.addButton(new ConfigBooleanButton(this.width / 2 + 5, this.height / 6 + (i2++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsDistanceLimited));
		this.addButton(new ConfigSlider(this.width / 2 + 5, this.height / 6 + (i2++ * 22), 128, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsMaxDistance, 0.0D, 1024.0D, 16.0D));
		this.addButton(new ConfigBooleanButton(this.width / 2 + 5, this.height / 6 + (i2++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCulling));
		this.addButton(new ConfigBooleanButton(this.width / 2 + 5, this.height / 6 + (i2++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveMode));
		this.addButton(new ConfigSlider(this.width / 2 + 5, this.height / 6 + (i2++ * 22), 128, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.tileEntityShadowsCullingLessAggressiveModeDiff, 0.0D, 128.0D, 1.0D));

		// ----- terrain shadow options ----- //
		int i3 = 6;
		this.addButton(new ConfigBooleanButton(this.width / 2 - 155, this.height / 6 + (i3++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsDisabled));
		this.addButton(new ConfigBooleanButton(this.width / 2 - 155, this.height / 6 + (i3++ * 22), 150, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsDistanceLimited));
		this.addButton(new ConfigSlider(this.width / 2 - 155, this.height / 6 + (i3++ * 22), 128, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxHorizontalDistance, 0.0D, 1024.0D, 16.0D));
		this.addButton(new ConfigSlider(this.width / 2 - 155, this.height / 6 + (i3++ * 22), 128, 20, EntityCullingConfig.CLIENT_CONFIG.optifineShaderOptions.terrainShadowsMaxVerticalDistance, 0.0D, 1024.0D, 16.0D));
		*/

		this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, DialogTexts.GUI_DONE, button -> {
			for (Widget widget : this.buttons) {
				if (widget instanceof IConfigWidget) {
					((IConfigWidget) widget).updateConfig();
				}
			}
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

	@Override
	public void render(MatrixStack matrixStack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderBackground(matrixStack);
		this.list.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 15, 16777215);
		super.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
	}

	@Override
	public void onClose() {
		super.onClose();
		for (Widget widget : this.buttons) {
			if (widget instanceof IConfigWidget) {
				((IConfigWidget) widget).updateConfig();
			}
		}
		this.minecraft.setScreen(this.lastScreen);
	}

	private static AbstractOption createBooleanOption(ForgeConfigSpec.BooleanValue config) {
		return new AbstractOption(ConfigBooleanButton.getText(config, config.get()).getString()) {
			@Override
			public Widget createButton(GameSettings gameSettings, int x, int y, int width) {
				return new ConfigBooleanButton(x, y, width, 20, config);
			}
		};
	}

	private static AbstractOption createSliderOption(ForgeConfigSpec.DoubleValue config, double min, double max, double stepSize) {
		return new AbstractOption(ConfigSlider.getText(config, config.get()).getString()) {
			@Override
			public Widget createButton(GameSettings gameSettings, int x, int y, int width) {
				return new ConfigSlider(x, y, width, 20, config, min, max, stepSize);
			}
		};
	}

}
