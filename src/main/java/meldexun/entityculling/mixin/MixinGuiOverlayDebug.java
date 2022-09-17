package meldexun.entityculling.mixin;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.renderlib.renderer.EntityRenderManager;
import meldexun.renderlib.renderer.TileEntityRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;

@Mixin(value = GuiOverlayDebug.class, priority = 1100)
public class MixinGuiOverlayDebug {

	@Shadow
	private Minecraft mc;

	@Inject(method = "call", at = @At("RETURN"))
	public void call(CallbackInfoReturnable<List<String>> info) {
		List<String> list = info.getReturnValue();
		search(list, s -> s.startsWith("Entities:"), i -> {
			list.set(i, list.get(i) + ", Culled: " + EntityRenderManager.occludedEntities());
		});
		search(list, s -> s.startsWith("Tile Entities:"), i -> {
			list.set(i, list.get(i) + ", Culled: " + TileEntityRenderManager.occludedTileEntities());
		});
	}

	private static void search(List<String> list, Predicate<String> predicate, IntConsumer action) {
		for (int i = 0; i < list.size(); i++) {
			if (predicate.test(list.get(i))) {
				action.accept(i);
				return;
			}
		}
	}

}
