package meldexun.entityculling.util;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class BoundingBoxHelper {

	private static BoundingBoxHelper instance;
	private final int cubeVertexBuffer;
	private final int cubeIndexBuffer;

	public BoundingBoxHelper() {
		cubeVertexBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cubeVertexBuffer);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, asByteBuffer(new byte[] {
				0, 0, 0,
				0, 0, 1,
				0, 1, 0,
				0, 1, 1,
				1, 0, 0,
				1, 0, 1,
				1, 1, 0,
				1, 1, 1
		}), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		cubeIndexBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, cubeIndexBuffer);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, asByteBuffer(new byte[] {
				7, 3, 5, 1, 0, 3, 2, 7, 6, 5, 4, 0, 6, 2
		}), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public static BoundingBoxHelper getInstance() {
		if (instance == null) {
			instance = new BoundingBoxHelper();
		}
		return instance;
	}

	private static ByteBuffer asByteBuffer(byte[] data) {
		return (ByteBuffer) GLAllocation.createDirectByteBuffer(data.length).put(data).flip();
	}

	public void drawPoints(double partialTicks) {
		if (!EntityCullingConfig.debugRenderBoxes) {
			return;
		}
		/*if (EntityCulling.useOpenGlBasedCulling()) {
			return;
		}*/

		Minecraft mc = Minecraft.getMinecraft();
		Entity ce = mc.getRenderViewEntity();
		double camX = ce.lastTickPosX + (ce.posX - ce.lastTickPosX) * partialTicks;
		double camY = ce.lastTickPosY + (ce.posY - ce.lastTickPosY) * partialTicks;
		double camZ = ce.lastTickPosZ + (ce.posZ - ce.lastTickPosZ) * partialTicks;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		GlStateManager.disableAlpha();
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cubeVertexBuffer);
		GL11.glVertexPointer(3, GL11.GL_BYTE, 0, 0);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, cubeIndexBuffer);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-camX, -camY, -camZ);

		for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
			if (e == ce) {
				continue;
			}

			MutableAABB aabb = ((IBoundingBoxCache) e).getCachedBoundingBox();

			GlStateManager.pushMatrix();
			GlStateManager.translate(aabb.minX(), aabb.minY(), aabb.minZ());
			GlStateManager.scale(aabb.maxX() - aabb.minX(), aabb.maxY() - aabb.minY(), aabb.maxZ() - aabb.minZ());

			GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0);

			GlStateManager.popMatrix();
		}

		for (TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList) {
			MutableAABB aabb = ((IBoundingBoxCache) te).getCachedBoundingBox();

			GlStateManager.pushMatrix();
			GlStateManager.translate(aabb.minX(), aabb.minY(), aabb.minZ());
			GlStateManager.scale(aabb.maxX() - aabb.minX(), aabb.maxY() - aabb.minY(), aabb.maxZ() - aabb.minZ());

			GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0);

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableFog();
		GlStateManager.enableAlpha();
	}

}
