package meldexun.entityculling;

import meldexun.entityculling.proxy.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = EntityCulling.MOD_ID, name = EntityCulling.NAME, version = EntityCulling.VERSION, acceptedMinecraftVersions = EntityCulling.ACCEPTED_VERSIONS)
public class EntityCulling {

	public static final String MOD_ID = "entity_culling";
	public static final String NAME = "Entity Culling";
	public static final String VERSION = "1.0.1";
	public static final String ACCEPTED_VERSIONS = "[1.12.2]";

	public static final String CLIENT_PROXY_CLASS = "meldexun.entityculling.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "meldexun.entityculling.proxy.ClientProxy";

	@SidedProxy(clientSide = CLIENT_PROXY_CLASS, serverSide = SERVER_PROXY_CLASS)
	public static IProxy proxy;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
	}

	@EventHandler
	public static void init(FMLInitializationEvent event) {
		proxy.init();
	}

	@EventHandler
	public static void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}

}
