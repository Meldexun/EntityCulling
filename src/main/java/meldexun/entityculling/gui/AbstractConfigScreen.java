package meldexun.entityculling.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public abstract class AbstractConfigScreen extends Screen {

	private final Screen lastScreen;
	private OptionsRowList list;
	private List<IConfigWidget> configWidgets;

	public AbstractConfigScreen(ITextComponent title, Screen lastScreen) {
		super(title);
		this.lastScreen = lastScreen;
	}

	@Override
	protected void init() {
		super.init();

		this.list = new OptionsRowList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.configWidgets = new ArrayList<>();
		this.initOptions(this.list);
		this.children.add(this.list);

		this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, DialogTexts.GUI_DONE, button -> {
			for (IConfigWidget widget : this.configWidgets) {
				widget.updateConfig();
			}
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

	protected abstract void initOptions(OptionsRowList list);

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
		for (IConfigWidget widget : this.configWidgets) {
			widget.updateConfig();
		}
		this.minecraft.setScreen(this.lastScreen);
	}

	protected AbstractOption createBooleanOption(ForgeConfigSpec.BooleanValue config) {
		return new AbstractOption(ConfigBooleanButton.getText(config, config.get()).getString()) {
			@Override
			public Widget createButton(GameSettings gameSettings, int x, int y, int width) {
				Widget widget = new ConfigBooleanButton(x, y, width, 20, config);
				AbstractConfigScreen.this.configWidgets.add((IConfigWidget) widget);
				return widget;
			}
		};
	}

	protected AbstractOption createDoubleSliderOption(ForgeConfigSpec.DoubleValue config, double min, double max, double stepSize) {
		return new AbstractOption(ConfigSlider.getText(config, config.get()).getString()) {
			@Override
			public Widget createButton(GameSettings gameSettings, int x, int y, int width) {
				Widget widget = new ConfigSlider(x, y, width, 20, config, min, max, stepSize);
				AbstractConfigScreen.this.configWidgets.add((IConfigWidget) widget);
				return widget;
			}
		};
	}

	protected AbstractOption createIntSliderOption(ForgeConfigSpec.IntValue config, int min, int max, int stepSize) {
		return new AbstractOption(ConfigSlider.getText(config, config.get()).getString()) {
			@Override
			public Widget createButton(GameSettings gameSettings, int x, int y, int width) {
				Widget widget = new ConfigSlider(x, y, width, 20, config, min, max, stepSize);
				AbstractConfigScreen.this.configWidgets.add((IConfigWidget) widget);
				return widget;
			}
		};
	}

	protected AbstractOption createDummyOption() {
		return new AbstractOption("dummy") {
			@Override
			public Widget createButton(GameSettings gameSettings, int x, int y, int width) {
				return new Widget(width, width, width, width, new StringTextComponent("dummy")) {
					@Override
					public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {

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
