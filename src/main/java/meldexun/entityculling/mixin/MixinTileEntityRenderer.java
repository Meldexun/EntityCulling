package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.ICullable;
import meldexun.entityculling.util.ICullable.CullInfo;
import meldexun.entityculling.util.culling.CullingInstance;
import meldexun.reflectionutil.ReflectionField;
import meldexun.renderlib.api.IBoundingBoxCache;
import meldexun.renderlib.renderer.tileentity.TileEntityRenderer;
import meldexun.renderlib.util.MutableAABB;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntityRenderer.class)
public class MixinTileEntityRenderer {

	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");

	@Unique
	protected final MutableAABB aabb = new MutableAABB();

	@Overwrite(remap = false)
	protected <T extends TileEntity> void setCanBeOcclusionCulled(T tileEntity, boolean canBeOcclusionCulled) {
		((ICullable) tileEntity).setCanBeOcclusionCulled(canBeOcclusionCulled);
	}

	@Overwrite(remap = false)
	protected <T extends TileEntity> boolean isOcclusionCulled(T tileEntity) {
		if (RenderUtil.isRecursive()) {
			return false;
		}
		if (EntityCulling.useOpenGlBasedCulling()) {
			if (EntityCullingClassTransformer.OPTIFINE_DETECTED && IS_SHADOW_PASS.getBoolean(null)) {
				return false;
			}
			if (!EntityCullingConfig.enabled) {
				return false;
			}
			if (EntityCullingConfig.disabledInSpectator && Minecraft.getMinecraft().player.isSpectator()) {
				return false;
			}
			if (!EntityCullingConfig.tileEntity.skipHiddenTileEntityRendering) {
				return false;
			}
			if (EntityCullingConfig.tileEntity.skipHiddenTileEntityRenderingBlacklistImpl.get(tileEntity)) {
				return false;
			}

			CullingInstance cullingInstance = CullingInstance.getInstance();
			CullInfo cullInfo = ((ICullable) tileEntity).getCullInfo();
			boolean culled = !cullingInstance.isVisible(cullInfo);

			aabb.set(((IBoundingBoxCache) tileEntity).getCachedBoundingBox());
			// aabb.expand(CameraUtil.getDeltaCamera(), RenderUtil.getPartialTickDelta());
			cullingInstance.addBox(cullInfo, aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ());

			return culled;
		}

		return ((ICullable) tileEntity).isCulled();
	}

}
