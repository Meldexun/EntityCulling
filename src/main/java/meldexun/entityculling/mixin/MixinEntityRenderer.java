package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.integration.Hats;
import meldexun.entityculling.util.ICullable;
import meldexun.entityculling.util.ICullable.CullInfo;
import meldexun.entityculling.util.culling.CullingInstance;
import meldexun.renderlib.api.IBoundingBoxCache;
import meldexun.renderlib.renderer.entity.EntityRenderer;
import meldexun.renderlib.util.MutableAABB;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

	@Unique
	protected final MutableAABB aabb = new MutableAABB();

	@Overwrite(remap = false)
	protected <T extends Entity> void setCanBeOcclusionCulled(T entity, boolean canBeOcclusionCulled) {
		((ICullable) entity).setCanBeOcclusionCulled(canBeOcclusionCulled);
	}

	@Overwrite(remap = false)
	protected <T extends Entity> boolean isOcclusionCulled(T entity) {
		if (RenderUtil.isRecursive()) {
			return false;
		}
		if (EntityCulling.useOpenGlBasedCulling()) {
			EntityCulling.cpuTimer.start();
			try {
				if (!EntityCullingConfig.enabled) {
					return false;
				}
				if (EntityCullingConfig.disabledInSpectator && Minecraft.getMinecraft().player.isSpectator()) {
					return false;
				}
				if (!EntityCullingConfig.entity.skipHiddenEntityRendering) {
					return false;
				}
				if (EntityCulling.isHatsInstalled && Hats.isHat(entity)) {
					return false;
				}
				if (EntityCullingConfig.entity.alwaysRenderBosses && !entity.isNonBoss()) {
					return false;
				}
				if (EntityCullingConfig.entity.alwaysRenderEntitiesWithName && entity.getAlwaysRenderNameTagForRender()) {
					return false;
				}
				if (EntityCullingConfig.entity.alwaysRenderPlayers && entity instanceof EntityPlayer) {
					return false;
				}
				if (EntityCullingConfig.entity.alwaysRenderViewEntity && entity == Minecraft.getMinecraft().getRenderViewEntity()) {
					return false;
				}
				if (EntityCullingConfig.entity.ignoreEndCrystalsWithBeam && entity instanceof EntityEnderCrystal && ((EntityEnderCrystal) entity).getBeamTarget() != null) {
					return true;
				}
				if (EntityCullingConfig.entity.skipHiddenEntityRenderingBlacklistImpl.get(entity)) {
					return false;
				}

				CullingInstance cullingInstance = CullingInstance.getInstance();
				CullInfo cullInfo = ((ICullable) entity).getCullInfo();
				boolean culled = !cullingInstance.isVisible(cullInfo);

				aabb.set(((IBoundingBoxCache) entity).getCachedBoundingBox());
				aabb.expand(entity.posX - entity.lastTickPosX, entity.posY - entity.lastTickPosY, entity.posZ - entity.lastTickPosZ, RenderUtil.getPartialTickDelta());
				// aabb.expand(CameraUtil.getDeltaCamera(), RenderUtil.getPartialTickDelta());
				cullingInstance.addBox(cullInfo, aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ());

				return culled;
			} finally {
				EntityCulling.cpuTimer.stop();
			}
		}

		return ((ICullable) entity).isCulled();
	}

}
