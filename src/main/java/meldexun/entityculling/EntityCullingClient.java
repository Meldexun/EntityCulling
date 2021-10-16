package meldexun.entityculling;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.mojang.blaze3d.vertex.PoseStack;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.gui.ShaderOptionsScreen;
import meldexun.entityculling.util.CullingThread;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityCullingClient {

	public static final CullingThread CULLING_THREAD = new CullingThread();
	private static final DecimalFormat FORMAT = new DecimalFormat("#.#");
	public static int renderedTileEntities;
	public static int culledTileEntities;

	public static void init() {
		CULLING_THREAD.start();
	}

	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.ALL) {
			return;
		}
		if (!EntityCullingConfig.CLIENT_CONFIG.debug.get()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		Window window = mc.getWindow();

		String s1 = "Time: " + FORMAT.format(Arrays.stream(CULLING_THREAD.time).average().getAsDouble() / 1_000_000.0D) + "ms";
		String s2 = "E: " + mc.levelRenderer.renderedEntities + "/" + mc.levelRenderer.culledEntities + "/" + mc.level.getEntityCount();
		String s3 = "TE: " + renderedTileEntities + "/" + culledTileEntities + "/" + 0;
		// TODO mc.level.blockEntityList.size();

		this.drawOnLeft(event.getMatrixStack(), s1, window.getGuiScaledWidth(), 160);
		this.drawOnLeft(event.getMatrixStack(), s2, window.getGuiScaledWidth(), 170);
		this.drawOnLeft(event.getMatrixStack(), s3, window.getGuiScaledWidth(), 180);
	}

	private void drawOnLeft(PoseStack matrixStack, String string, int x, int y) {
		Minecraft mc = Minecraft.getInstance();
		mc.font.draw(matrixStack, string, x - mc.font.width(string), y, 0xFFFFFFFF);
	}

	@SubscribeEvent
	public void onInitGuiEvent(GuiScreenEvent.InitGuiEvent.Post event) {
		if (!EntityCulling.IS_OPTIFINE_DETECTED) {
			return;
		}
		Screen screen = event.getGui();
		if (!(screen instanceof OptionsScreen)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		event.addWidget(new Button(screen.width / 2 - 155, screen.height / 6 + 120 - 6 + 24, 150, 20,
				new TranslatableComponent("options.entity_culling.shadows.button"), button -> {
					mc.setScreen(new ShaderOptionsScreen(mc.screen));
				}));
	}

}
