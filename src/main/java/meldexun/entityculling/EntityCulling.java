package meldexun.entityculling;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.CullingThread;
import meldexun.renderlib.util.GLUtil;
import meldexun.renderlib.util.timer.CPUTimer;
import meldexun.renderlib.util.timer.ITimer;
import meldexun.renderlib.util.timer.TimerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = EntityCulling.MOD_ID, acceptableRemoteVersions = "*", dependencies = "required-after:renderlib@[1.3.1,)")
public class EntityCulling {

	public static final String MOD_ID = "entityculling";
	private static CullingThread cullingThread;
	public static boolean isCubicChunksInstalled;
	public static boolean isHatsInstalled;
	public static final ITimer gpuTimer = TimerEventHandler.tryCreateGLTimer("GPU (Cull)", 100);
	public static final ITimer cpuTimer = new CPUTimer("CPU (Cull Main)", 100);

	public static boolean useOpenGlBasedCulling() {
		return EntityCullingConfig.openglBasedCulling && GLUtil.CAPS.OpenGL44;
	}

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		EntityCullingConfig.onConfigChanged();
		MinecraftForge.EVENT_BUS.register(this);

		cullingThread = new CullingThread();
		cullingThread.start();

		TimerEventHandler.timers.add(null);
		TimerEventHandler.timers.add(gpuTimer);
		TimerEventHandler.timers.add(cpuTimer);
		TimerEventHandler.timers.add(cullingThread.timer);
	}

	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isCubicChunksInstalled = Loader.isModLoaded("cubicchunks");
		isHatsInstalled = Loader.isModLoaded("hats");
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			EntityCullingConfig.onConfigChanged();
		}
	}

	@SubscribeEvent
	public void onRenderTickEvent(TickEvent.RenderTickEvent event) {
		gpuTimer.update();
		cpuTimer.update();
	}

}
