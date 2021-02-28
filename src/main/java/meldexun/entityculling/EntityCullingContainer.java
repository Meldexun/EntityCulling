package meldexun.entityculling;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EntityCullingContainer extends DummyModContainer {

	public static final String MOD_ID = "entity_culling";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static final CullingThread CULLING_THREAD = new CullingThread();
	public static boolean debug = false;

	public EntityCullingContainer() {
		super(new ModMetadata());
		ModMetadata meta = this.getMetadata();
		meta.name = "Entity Culling";
		meta.version = "3.0.1";
		meta.modId = MOD_ID;
		meta.authorList = Arrays.asList("Meldexun");
		meta.url = "https://github.com/Meldexun/EntityCulling";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}

	@Subscribe
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
		CullingThread.updateBlacklists();
		MinecraftForge.EVENT_BUS.register(this);

		CULLING_THREAD.start();
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			CullingThread.updateBlacklists();
		}
	}

	@SubscribeEvent
	public void onWorldTickEvent(TickEvent.ClientTickEvent event) {
		if (debug && Minecraft.getMinecraft().world.getTotalWorldTime() % 40 == 0) {
			LOGGER.info("{}", Arrays.stream(CULLING_THREAD.time).sum() / 1_000 / 10);
		}
	}

}
