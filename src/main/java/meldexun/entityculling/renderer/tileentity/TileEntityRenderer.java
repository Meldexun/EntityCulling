package meldexun.entityculling.renderer.tileentity;

import java.util.ArrayDeque;
import java.util.Queue;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.BoundingBoxHelper;
import meldexun.entityculling.util.IBoundingBoxCache;
import meldexun.entityculling.util.ICullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;

public class TileEntityRenderer {

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

	protected void addToRenderLists(TileEntity tileEntity, ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		if (EntityCullingConfig.debugRenderBoxes) {
			this.drawBox(tileEntity, camX, camY, camZ, partialTicks);
		}

		this.totalTileEntities++;

		if (!tileEntity.shouldRenderInPass(0) && !tileEntity.shouldRenderInPass(1)) {
			return;
		}
		if (!camera.isBoundingBoxInFrustum(((IBoundingBoxCache) tileEntity).getOrCacheBoundingBox())) {
			return;
		}
		if (tileEntity.getDistanceSq(camX, camY, camZ) >= tileEntity.getMaxRenderDistanceSquared()) {
			return;
		}
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

	protected void drawBox(TileEntity tileEntity, double camX, double camY, double camZ, double partialTicks) {
		AxisAlignedBB aabb = ((IBoundingBoxCache) tileEntity).getOrCacheBoundingBox();
		if (aabb.hasNaN()) {
			BlockPos pos = tileEntity.getPos();
			aabb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
		}
		BoundingBoxHelper.drawBox(aabb, camX, camY, camZ);
	}

	protected boolean isOcclusionCulled(TileEntity tileEntity, double partialTicks) {
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

			this.drawPoints();
		}
	}

	protected void preRenderTileEntity(TileEntity tileEntity) {

	}

	protected void postRenderTileEntity() {

	}

	protected void drawPoints() {
		BoundingBoxHelper.drawPoints();
	}

}
