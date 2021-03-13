package meldexun.entityculling;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(FMLClientSetupEvent event) {
		CullingThread.updateBlacklists();

		EntityCullingClient.init();
		event.enqueueWork(EntityCullingClient::generateCubeDisplayList);
		MinecraftForge.EVENT_BUS.register(new EntityCullingClient());
	}

}
