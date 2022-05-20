package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

import meldexun.entityculling.util.ICullable;
import meldexun.reflectionutil.ReflectionField;
import meldexun.renderlib.renderer.entity.EntityRenderer;
import meldexun.renderlib.renderer.entity.EntityRendererOptifine;
import net.minecraft.entity.Entity;

@Mixin(EntityRendererOptifine.class)
public class MixinEntityRendererOptifine extends EntityRenderer {

	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");

	@Override
	protected <T extends Entity> boolean isOcclusionCulled(final T entity) {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			return ((ICullable) entity).isShadowCulled();
		}

		return super.isOcclusionCulled(entity);
	}

}
