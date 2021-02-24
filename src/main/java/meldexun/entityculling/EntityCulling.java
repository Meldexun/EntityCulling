package meldexun.entityculling;

import meldexun.entityculling.plugin.Hook;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EntityCulling.MOD_ID)
public class EntityCulling {

	public static final String MOD_ID = "entity_culling";

	public EntityCulling() {
		ModLoadingContext.get().registerConfig(Type.CLIENT, EntityCullingConfig.CLIENT_SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {
		Hook.updateBlacklists();
	}

}
