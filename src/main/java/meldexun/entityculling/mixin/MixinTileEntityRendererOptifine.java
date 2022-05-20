package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

import meldexun.entityculling.util.ICullable;
import meldexun.reflectionutil.ReflectionField;
import meldexun.renderlib.renderer.tileentity.TileEntityRenderer;
import meldexun.renderlib.renderer.tileentity.TileEntityRendererOptifine;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntityRendererOptifine.class)
public class MixinTileEntityRendererOptifine extends TileEntityRenderer {

	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");

	@Override
	protected <T extends TileEntity> boolean isOcclusionCulled(final T tileEntity) {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			return ((ICullable) tileEntity).isShadowCulled();
		}

		return super.isOcclusionCulled(tileEntity);
	}

}
