package meldexun.entityculling;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import meldexun.entityculling.plugin.Hook;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityCullingContainer extends DummyModContainer {

	public static final String MOD_ID = "entity_culling";

	public EntityCullingContainer() {
		super(new ModMetadata());
		ModMetadata meta = this.getMetadata();
		meta.name = "Entity Culling";
		meta.version = "2.0.2";
		meta.modId = MOD_ID;
		meta.authorList = Arrays.asList("Meldexun");
		meta.url = "https://github.com/Meldexun/EntityCulling";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}

	@Override
	public String getGuiClassName() {
		return super.getGuiClassName();
	}

	@Subscribe
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			Hook.updateBlacklists();
		}
	}

}
