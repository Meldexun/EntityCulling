package meldexun.entityculling.renderer.tileentity;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import org.lwjgl.opengl.GL11;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.BoundingBoxHelper;
import meldexun.entityculling.util.IBoundingBoxCache;
import meldexun.entityculling.util.ICullable;
import meldexun.entityculling.util.culling.CullingInstance;
import meldexun.entityculling.util.matrix.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.MinecraftForgeClient;

public class TileEntityRenderer {

	private static final FloatBuffer MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
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
		this.totalTileEntities++;

		if (!tileEntity.shouldRenderInPass(0) && !tileEntity.shouldRenderInPass(1)) {
			return;
		}
		if (TileEntityRendererDispatcher.instance.getRenderer(tileEntity) == null) {
			return;
		}
		if (!camera.isBoundingBoxInFrustum(((IBoundingBoxCache) tileEntity).getCachedBoundingBox())) {
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

			AxisAlignedBB aabb = ((IBoundingBoxCache) tileEntity).getCachedBoundingBox();
			double d = 1.0D / 8.0D;
			CullingInstance.getInstance().addBox((ICullable) tileEntity,
					aabb.minX - d, aabb.minY - d, aabb.minZ - d,
					aabb.maxX + d, aabb.maxY + d, aabb.maxZ + d);

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

	}

	protected void postRenderTileEntity() {

	}

	protected void drawPoints(double partialTicks) {
		if (EntityCulling.useOpenGlBasedCulling()) {
			Minecraft mc = Minecraft.getMinecraft();
			Entity e = mc.getRenderViewEntity();
			double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * partialTicks;
			double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * partialTicks;
			double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partialTicks;
			Matrix4f matrix = getMatrix(GL11.GL_PROJECTION_MATRIX);
			matrix.multiply(getMatrix(GL11.GL_MODELVIEW_MATRIX));
			matrix.multiply(Matrix4f.translateMatrix(-(float) x, -(float) y, -(float) z));
			CullingInstance.getInstance().updateResults(matrix);
		}

		// debug
		BoundingBoxHelper.getInstance().drawPoints(partialTicks);
	}

	private static Matrix4f getMatrix(int matrix) {
		GL11.glGetFloat(matrix, MATRIX_BUFFER);
		Matrix4f m = new Matrix4f();
		m.load(MATRIX_BUFFER);
		return m;
	}

}
