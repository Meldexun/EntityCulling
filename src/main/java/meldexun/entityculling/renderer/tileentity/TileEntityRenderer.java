package meldexun.entityculling.renderer.tileentity;

import java.util.ArrayDeque;
import java.util.Queue;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.BoundingBoxHelper;
import meldexun.entityculling.util.CameraUtil;
import meldexun.entityculling.util.IBoundingBoxCache;
import meldexun.entityculling.util.ICullable;
import meldexun.entityculling.util.ILoadable;
import meldexun.entityculling.util.ITileEntityRendererCache;
import meldexun.entityculling.util.MutableAABB;
import meldexun.entityculling.util.culling.CullingInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

public class TileEntityRenderer {

	protected final MutableAABB aabb = new MutableAABB();
	protected final Queue<TileEntity> tileEntityListPass0 = new ArrayDeque<>();
	protected final Queue<TileEntity> tileEntityListPass1 = new ArrayDeque<>();
	public int renderedTileEntities;
	public int occludedTileEntities;
	public int totalTileEntities;

	public void setup(ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		this.renderedTileEntities = 0;
		this.occludedTileEntities = 0;
		this.totalTileEntities = 0;
		this.clearTileEntityLists();
		this.fillTileEntityLists(camera, camX, camY, camZ, partialTicks);
	}

	protected void clearTileEntityLists() {
		this.tileEntityListPass0.clear();
		this.tileEntityListPass1.clear();
	}

	protected void fillTileEntityLists(ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.world.loadedTileEntityList.forEach(tileEntity -> this.addToRenderLists(tileEntity, camera, camX, camY, camZ, partialTicks));
	}

	protected <T extends TileEntity> void addToRenderLists(T tileEntity, ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		this.totalTileEntities++;

		if (!((ITileEntityRendererCache) tileEntity).hasRenderer()) {
			return;
		}
		if (!((ILoadable) tileEntity).isChunkLoaded()) {
			return;
		}
		if (!((IBoundingBoxCache) tileEntity).getCachedBoundingBox().isVisible(camera)) {
			((ICullable) tileEntity).setCanBeOcclusionCulled(false);
			return;
		}
		if (tileEntity.getDistanceSq(camX, camY, camZ) >= tileEntity.getMaxRenderDistanceSquared()) {
			((ICullable) tileEntity).setCanBeOcclusionCulled(false);
			return;
		}
		((ICullable) tileEntity).setCanBeOcclusionCulled(true);
		if (this.isOcclusionCulled(tileEntity, partialTicks)) {
			this.occludedTileEntities++;
			return;
		}

		this.renderedTileEntities++;

		if (tileEntity.shouldRenderInPass(0)) {
			this.tileEntityListPass0.add(tileEntity);
		}
		if (tileEntity.shouldRenderInPass(1)) {
			this.tileEntityListPass1.add(tileEntity);
		}
	}

	protected boolean isOcclusionCulled(TileEntity tileEntity, double partialTicks) {
		if (EntityCulling.useOpenGlBasedCulling()) {
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

			// TODO handle shadows
			boolean culled = !CullingInstance.getInstance().isVisible((ICullable) tileEntity);

			aabb.set(((IBoundingBoxCache) tileEntity).getCachedBoundingBox());
			aabb.expand(CameraUtil.getDeltaCamera(), CameraUtil.getDeltaFrameTickTime());
			CullingInstance.getInstance().addBox((ICullable) tileEntity, aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ());

			return culled;
		}

		return ((ICullable) tileEntity).isCulled();
	}

	public void renderTileEntities(float partialTicks) {
		int pass = MinecraftForgeClient.getRenderPass();

		if (pass == 0) {
			this.tileEntityListPass0.forEach(tileEntity -> {
				this.preRenderTileEntity(tileEntity);
				TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
				this.postRenderTileEntity();
			});
		} else if (pass == 1) {
			this.tileEntityListPass1.forEach(tileEntity -> {
				this.preRenderTileEntity(tileEntity);
				TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
				this.postRenderTileEntity();
			});

			this.drawPoints(partialTicks);
		}
	}

	protected void preRenderTileEntity(TileEntity tileEntity) {
		// workaround for stupid mods
		tileEntity.shouldRenderInPass(MinecraftForgeClient.getRenderPass());
	}

	protected void postRenderTileEntity() {

	}

	protected void drawPoints(double partialTicks) {
		// debug
		BoundingBoxHelper.getInstance().drawPoints(partialTicks);
	}

}
