package meldexun.entityculling.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public abstract class AbstractConfigScreen extends Screen {

	private final Screen lastScreen;
	private OptionsList list;
	private List<IConfigWidget> configWidgets;

	public AbstractConfigScreen(Component title, Screen lastScreen) {
		super(title);
		this.lastScreen = lastScreen;
	}

	@Override
	protected void init() {
		super.init();

		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.configWidgets = new ArrayList<>();
		this.initOptions(this.list);
		this.children.add(this.list);

		this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> {
			for (IConfigWidget widget : this.configWidgets) {
				widget.updateConfig();
			}
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

	protected abstract void initOptions(OptionsList list);

	@Override
	public void render(PoseStack matrixStack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderBackground(matrixStack);
		this.list.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 15, 16777215);
		super.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
	}

	@Override
	public void onClose() {
		super.onClose();
		for (IConfigWidget widget : this.configWidgets) {
			widget.updateConfig();
		}
		this.minecraft.setScreen(this.lastScreen);
	}

	protected Option createBooleanOption(ForgeConfigSpec.BooleanValue config) {
		return new Option(ConfigBooleanButton.getText(config, config.get()).getString()) {
			@Override
			public AbstractWidget createButton(Options gameSettings, int x, int y, int width) {
				AbstractWidget widget = new ConfigBooleanButton(x, y, width, 20, config);
				AbstractConfigScreen.this.configWidgets.add((IConfigWidget) widget);
				return widget;
			}
		};
	}

	protected Option createDoubleSliderOption(ForgeConfigSpec.DoubleValue config, double min, double max, double stepSize) {
		return new Option(ConfigSlider.getText(config, config.get()).getString()) {
			@Override
			public AbstractWidget createButton(Options gameSettings, int x, int y, int width) {
				AbstractWidget widget = new ConfigSlider(x, y, width, 20, config, min, max, stepSize);
				AbstractConfigScreen.this.configWidgets.add((IConfigWidget) widget);
				return widget;
			}
		};
	}

	protected Option createIntSliderOption(ForgeConfigSpec.IntValue config, int min, int max, int stepSize) {
		return new Option(ConfigSlider.getText(config, config.get()).getString()) {
			@Override
			public AbstractWidget createButton(Options gameSettings, int x, int y, int width) {
				AbstractWidget widget = new ConfigSlider(x, y, width, 20, config, min, max, stepSize);
				AbstractConfigScreen.this.configWidgets.add((IConfigWidget) widget);
				return widget;
			}
		};
	}

	protected Option createDummyOption() {
		return new Option("dummy") {
			@Override
			public AbstractWidget createButton(Options gameSettings, int x, int y, int width) {
				return new AbstractWidget(width, width, width, width, new TextComponent("dummy")) {
					@Override
					public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {

					}

					@Override
					protected void narrate() {

					}

					@Override
					public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
						return false;
					}

					@Override
					public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
						return false;
					}

					@Override
					public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
						return false;
					}
				};
			}
		};
	}

}
