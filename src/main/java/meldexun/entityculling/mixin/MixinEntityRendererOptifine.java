package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

import meldexun.entityculling.util.ICullable;
import meldexun.renderlib.integration.Optifine;
import meldexun.renderlib.renderer.entity.EntityRenderer;
import meldexun.renderlib.renderer.entity.EntityRendererOptifine;
import net.minecraft.entity.Entity;

@Mixin(EntityRendererOptifine.class)
public class MixinEntityRendererOptifine extends EntityRenderer {

	@Override
	protected <T extends Entity> boolean isOcclusionCulled(final T entity) {
		if (Optifine.isShadowPass()) {
			return ((ICullable) entity).isShadowCulled();
		}

		return super.isOcclusionCulled(entity);
	}

}
