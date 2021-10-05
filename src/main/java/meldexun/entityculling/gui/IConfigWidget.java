package meldexun.entityculling.gui;

import java.util.function.Supplier;

import meldexun.reflectionutil.ReflectionField;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public interface IConfigWidget {

	ReflectionField<Supplier<?>> CONFIG_VALUE_DEFAULT_SUPPLIER = new ReflectionField<>(ConfigValue.class, "defaultSupplier", "defaultSupplier");

	void updateConfig();

	void resetConfigWidget();

}
