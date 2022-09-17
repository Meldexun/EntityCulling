package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

import meldexun.entityculling.util.ICullable;
import meldexun.renderlib.integration.Optifine;
import meldexun.renderlib.renderer.tileentity.TileEntityRenderer;
import meldexun.renderlib.renderer.tileentity.TileEntityRendererOptifine;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntityRendererOptifine.class)
public class MixinTileEntityRendererOptifine extends TileEntityRenderer {

	@Override
	protected <T extends TileEntity> boolean isOcclusionCulled(final T tileEntity) {
		if (Optifine.isShadowPass()) {
			return ((ICullable) tileEntity).isShadowCulled();
		}

		return super.isOcclusionCulled(tileEntity);
	}

}
