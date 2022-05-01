package meldexun.entityculling;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.lwjgl.opengl.GLContext;

import meldexun.entityculling.asm.hook.RenderGlobalHook;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.CameraUtil;
import meldexun.entityculling.util.CullingThread;
import meldexun.entityculling.util.IBoundingBoxCache;
import meldexun.entityculling.util.IEntityRendererCache;
import meldexun.entityculling.util.ILoadable;
import meldexun.entityculling.util.ITileEntityRendererCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

@Mod(modid = EntityCulling.MOD_ID)
public class EntityCulling {

	public static final String MOD_ID = "entityculling";
	private static CullingThread cullingThread;
	private static final DecimalFormat FORMAT = new DecimalFormat("0.0");
	public static boolean isCubicChunksInstalled;
	public static boolean isFairyLightsInstalled;
	public static boolean isValkyrienSkiesInstalled;
	private static boolean isOpenGL44Supported;
	public static int frame;

	public static boolean useOpenGlBasedCulling() {
		return isOpenGL44Supported && EntityCullingConfig.openglBasedCulling;
	}

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		isOpenGL44Supported = GLContext.getCapabilities().OpenGL44;

		EntityCullingConfig.onConfigChanged();
		MinecraftForge.EVENT_BUS.register(this);

		cullingThread = new CullingThread();
		cullingThread.start();
	}

	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isCubicChunksInstalled = Loader.isModLoaded("cubicchunks");
		isFairyLightsInstalled = Loader.isModLoaded("fairylights");
		isValkyrienSkiesInstalled = Loader.isModLoaded("valkyrienskies");
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			EntityCullingConfig.onConfigChanged();
		}
	}

	@SubscribeEvent
	public void onRenderTickEvent(RenderTickEvent event) {
		if (event.phase == Phase.END) {
			return;
		}
		frame++;
		CameraUtil.update();
		Minecraft mc = Minecraft.getMinecraft();
		double partialTicks = mc.getRenderPartialTicks();
		if (mc.world != null) {
			for (Entity e : mc.world.loadedEntityList) {
				if (((IEntityRendererCache) e).hasRenderer() && ((ILoadable) e).isChunkLoaded())
					((IBoundingBoxCache) e).updateCachedBoundingBox(partialTicks);
			}
			for (TileEntity te : mc.world.loadedTileEntityList) {
				if (((ITileEntityRendererCache) te).hasRenderer() && ((ILoadable) te).isChunkLoaded())
					((IBoundingBoxCache) te).updateCachedBoundingBox(partialTicks);
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
		int width = scaled.getScaledWidth();
		int height = scaled.getScaledHeight();
		drawDebug("Time:", FORMAT.format(Arrays.stream(cullingThread.time).average().getAsDouble() / 1_000_000.0D) + "ms", width, height - 80);
		drawDebug("E (R):", Integer.toString(RenderGlobalHook.entityRenderer.renderedEntities), width, height - 70);
		drawDebug("E (C):", Integer.toString(RenderGlobalHook.entityRenderer.occludedEntities), width, height - 60);
		drawDebug("E (T):", Integer.toString(RenderGlobalHook.entityRenderer.totalEntities), width, height - 50);
		drawDebug("TE (R):", Integer.toString(RenderGlobalHook.tileEntityRenderer.renderedTileEntities), width, height - 40);
		drawDebug("TE (C):", Integer.toString(RenderGlobalHook.tileEntityRenderer.occludedTileEntities), width, height - 30);
		drawDebug("TE (T):", Integer.toString(RenderGlobalHook.tileEntityRenderer.totalTileEntities), width, height - 20);
	}

	private void drawDebug(String prefix, String string, int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;
		int stringWidth = font.getStringWidth(string);
		font.drawString(prefix, x - Math.max(stringWidth + font.getStringWidth(prefix), 60), y, 0xFFFFFFFF);
		font.drawString(string, x - stringWidth, y, 0xFFFFFFFF);
	}

}
