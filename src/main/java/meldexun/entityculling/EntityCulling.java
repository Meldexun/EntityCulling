package meldexun.entityculling;

import java.text.DecimalFormat;
import java.util.Arrays;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.CullingThread;
import meldexun.renderlib.renderer.EntityRenderManager;
import meldexun.renderlib.renderer.TileEntityRenderManager;
import meldexun.renderlib.util.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
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

@Mod(modid = EntityCulling.MOD_ID, dependencies = "required-after:renderlib@[1.1.4,)")
public class EntityCulling {

	public static final String MOD_ID = "entityculling";
	private static CullingThread cullingThread;
	private static final DecimalFormat FORMAT = new DecimalFormat("0.0");
	public static boolean isCubicChunksInstalled;
	public static int frame;

	public static boolean useOpenGlBasedCulling() {
		return EntityCullingConfig.openglBasedCulling && GLUtil.CAPS.OpenGL43;
	}

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		EntityCullingConfig.onConfigChanged();
		MinecraftForge.EVENT_BUS.register(this);

		cullingThread = new CullingThread();
		cullingThread.start();
	}

	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isCubicChunksInstalled = Loader.isModLoaded("cubicchunks");
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			EntityCullingConfig.onConfigChanged();
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
		int width = scaled.getScaledWidth();
		int height = scaled.getScaledHeight();
		drawDebug("Time:", FORMAT.format(Arrays.stream(cullingThread.time).average().getAsDouble() / 1_000_000.0D) + "ms", width, height - 80);
		drawDebug("E (R):", Integer.toString(EntityRenderManager.renderedEntities()), width, height - 70);
		drawDebug("E (C):", Integer.toString(EntityRenderManager.occludedEntities()), width, height - 60);
		drawDebug("E (T):", Integer.toString(EntityRenderManager.totalEntities()), width, height - 50);
		drawDebug("TE (R):", Integer.toString(TileEntityRenderManager.renderedTileEntities()), width, height - 40);
		drawDebug("TE (C):", Integer.toString(TileEntityRenderManager.occludedTileEntities()), width, height - 30);
		drawDebug("TE (T):", Integer.toString(TileEntityRenderManager.totalTileEntities()), width, height - 20);
	}

	private void drawDebug(String prefix, String string, int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;
		int stringWidth = font.getStringWidth(string);
		font.drawString(prefix, x - Math.max(stringWidth + font.getStringWidth(prefix), 60), y, 0xFFFFFFFF);
		font.drawString(string, x - stringWidth, y, 0xFFFFFFFF);
	}

}
