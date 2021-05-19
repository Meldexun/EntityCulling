package meldexun.entityculling.gui;

import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigBooleanButton extends AbstractButton implements IConfigWidget {

	private final ForgeConfigSpec.BooleanValue config;
	private boolean value;

	public ConfigBooleanButton(int x, int y, int width, int height, ForgeConfigSpec.BooleanValue config) {
		super(x, y, width, height, getText(config, config.get()));
		this.config = config;
		this.value = config.get();
	}

	public static ITextComponent getText(ForgeConfigSpec.BooleanValue config, boolean value) {
		return new TranslationTextComponent(config.getPath().get(config.getPath().size() - 1), value);
	}

	@Override
	public void onPress() {
		this.value = !this.value;
		this.setMessage(getText(this.config, this.value));
	}

	@Override
	public void updateConfig() {
		this.config.set(this.value);
	}

}
