package meldexun.entityculling.util;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class BoundingBoxHelper {

	private static BoundingBoxHelper instance;
	private final int cubeVertexBuffer;
	private final int cubeIndexBuffer;
	private final int vao;

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
				3, 7, 1, 5, 4, 7, 6, 3, 2, 1, 0, 4, 2, 6
		}), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cubeVertexBuffer);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_BYTE, false, 0, 0);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, cubeIndexBuffer);
		GL30.glBindVertexArray(0);
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
		if (EntityCulling.useOpenGlBasedCulling()) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		Entity ce = mc.getRenderViewEntity();
		double camX = ce.lastTickPosX + (ce.posX - ce.lastTickPosX) * partialTicks;
		double camY = ce.lastTickPosY + (ce.posY - ce.lastTickPosY) * partialTicks;
		double camZ = ce.lastTickPosZ + (ce.posZ - ce.lastTickPosZ) * partialTicks;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

		GL30.glBindVertexArray(this.vao);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-camX, -camY, -camZ);

		for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
			if (e == ce) {
				continue;
			}

			AxisAlignedBB aabb = ((IBoundingBoxCache) e).getCachedBoundingBox();
			double px = -(e.posX - e.lastTickPosX) * (1.0D - partialTicks);
			double py = -(e.posY - e.lastTickPosY) * (1.0D - partialTicks);
			double pz = -(e.posZ - e.lastTickPosZ) * (1.0D - partialTicks);
			double sc = 0.5D;

			GlStateManager.pushMatrix();
			GlStateManager.translate(aabb.minX + px - sc, aabb.minY + py - sc, aabb.minZ + pz - sc);
			GlStateManager.scale(aabb.maxX - aabb.minX + sc * 2.0D, aabb.maxY - aabb.minY + sc * 2.0D, aabb.maxZ - aabb.minZ + sc * 2.0D);

			GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0);

			GlStateManager.popMatrix();
		}

		for (TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList) {
			AxisAlignedBB aabb = ((IBoundingBoxCache) te).getCachedBoundingBox();

			GlStateManager.pushMatrix();
			GlStateManager.translate(aabb.minX, aabb.minY, aabb.minZ);
			GlStateManager.scale(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);

			GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0);

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		GL30.glBindVertexArray(0);

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableFog();
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
	}

}
