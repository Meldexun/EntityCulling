package meldexun.entityculling.gui;

import java.text.DecimalFormat;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigSlider extends AbstractSlider implements IConfigWidget {

	private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
	private final ForgeConfigSpec.DoubleValue config;
	private final double min;
	private final double max;
	private final double stepSize;

	public ConfigSlider(int x, int y, int width, int height, ForgeConfigSpec.DoubleValue config, double min, double max, double stepSize) {
		super(x, y, width, height, getText(config, config.get()), (config.get() - min) / (max - min));
		this.config = config;
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
	}

	public static ITextComponent getText(ForgeConfigSpec.DoubleValue config, double value) {
		return new TranslationTextComponent(config.getPath().get(config.getPath().size() - 1), FORMAT.format(value));
	}

	@Override
	protected void updateMessage() {
		double d = this.min + this.stepSize * Math.round(this.value * (this.max - this.min) / this.stepSize);
		this.setMessage(getText(this.config, d));
	}

	@Override
	protected void applyValue() {

	}

	@Override
	public void updateConfig() {
		this.config.set(this.min + this.stepSize * Math.round(this.value * (this.max - this.min) / this.stepSize));
	}

}
