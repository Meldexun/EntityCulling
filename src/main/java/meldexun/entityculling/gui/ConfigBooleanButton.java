package meldexun.entityculling.gui;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigBooleanButton extends AbstractButton implements IConfigWidget {

	private final ForgeConfigSpec.BooleanValue config;
	private boolean value;

	public ConfigBooleanButton(int x, int y, int width, int height, ForgeConfigSpec.BooleanValue config) {
		super(x, y, width, height, getText(config, config.get()));
		this.config = config;
		this.value = config.get();
	}

	public static Component getText(ForgeConfigSpec.BooleanValue config, boolean value) {
		return new TranslatableComponent(config.getPath().get(config.getPath().size() - 1),
				(value ? ChatFormatting.GREEN.toString() : ChatFormatting.RED.toString()) + value);
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

	@Override
	public void resetConfigWidget() {
		this.config.set((Boolean) CONFIG_VALUE_DEFAULT_SUPPLIER.get(this.config).get());
		this.value = this.config.get();
		this.setMessage(getText(this.config, this.value));
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {

	}

}
