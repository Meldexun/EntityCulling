package meldexun.entityculling;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.lwjgl.opengl.GLContext;

import meldexun.entityculling.asm.hook.RenderGlobalHook;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.CullingThread;
import meldexun.entityculling.util.IBoundingBoxCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
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
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@Mod(modid = EntityCulling.MOD_ID)
public class EntityCulling {

	public static final String MOD_ID = "entityculling";
	private static CullingThread cullingThread;
	private static final DecimalFormat FORMAT = new DecimalFormat("#.#");
	public static boolean isCubicChunksInstalled;
	public static boolean isFairyLightsInstalled;
	public static boolean isOpenGL44Supported;

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		isOpenGL44Supported = GLContext.getCapabilities().OpenGL44;

		ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
		EntityCullingConfig.onConfigChanged();

		MinecraftForge.EVENT_BUS.register(this);

		cullingThread = new CullingThread();
		cullingThread.start();
	}

	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isCubicChunksInstalled = Loader.isModLoaded("cubicchunks");
		isFairyLightsInstalled = Loader.isModLoaded("fairylights");
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			EntityCullingConfig.onConfigChanged();
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlayEvent(TickEvent.RenderTickEvent event) {
		if (event.phase == Phase.END) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.world != null) {
			for (Entity e : mc.world.loadedEntityList) {
				((IBoundingBoxCache) e).updateCachedBoundingBox();
			}
			for (TileEntity te : mc.world.loadedTileEntityList) {
				((IBoundingBoxCache) te).updateCachedBoundingBox();
			}
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.ALL) {
			return;
		}
		if (!EntityCullingConfig.debugCullInfo) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaled = new ScaledResolution(mc);
		this.drawOnLeft("Time: " + FORMAT.format(Arrays.stream(cullingThread.time).average().getAsDouble() / 1_000_000.0D) + "ms", scaled.getScaledWidth(),
				160);
		this.drawOnLeft("E: " + RenderGlobalHook.entityRenderer.renderedEntities + "/" + RenderGlobalHook.entityRenderer.occludedEntities + "/"
				+ RenderGlobalHook.entityRenderer.totalEntities, scaled.getScaledWidth(), 170);
		this.drawOnLeft("TE: " + RenderGlobalHook.tileEntityRenderer.renderedTileEntities + "/" + RenderGlobalHook.tileEntityRenderer.occludedTileEntities + "/"
				+ RenderGlobalHook.tileEntityRenderer.totalTileEntities, scaled.getScaledWidth(), 180);
	}

	private void drawOnLeft(String string, int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.fontRenderer.drawString(string, x - mc.fontRenderer.getStringWidth(string), y, 0xFFFFFFFF);
	}

}
