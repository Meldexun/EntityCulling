package meldexun.entityculling;

import org.apache.commons.lang3.tuple.Pair;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.CullingThread;
import meldexun.entityculling.util.IBoundingBoxCache;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(EntityCulling.MOD_ID)
public class EntityCulling {

	public static final String MOD_ID = "entity_culling";
	public static final boolean IS_OPTIFINE_DETECTED;

	static {
		boolean flag = false;
		try {
			Class.forName("net.optifine.Config", false, Thread.currentThread().getContextClassLoader());
			flag = true;
		} catch (ClassNotFoundException e) {
			// ignore
		}
		IS_OPTIFINE_DETECTED = flag;
	}

	public EntityCulling() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		ModLoadingContext.get().registerConfig(Type.CLIENT, EntityCullingConfig.CLIENT_SPEC);
		// ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, lastScreen) -> new
		// ConfigOptionsScreen(lastScreen));
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChanged);
	}

	private void setup(FMLClientSetupEvent event) {
		event.enqueueWork(EntityCullingClient::init);
		MinecraftForge.EVENT_BUS.register(new EntityCullingClient());
	}

	public void onConfigChanged(ModConfigEvent event) {
		CullingThread.updateBlacklists();
		IBoundingBoxCache.updateBlacklist();
	}

}
