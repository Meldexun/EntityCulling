package meldexun.entityculling.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class BoundingBoxHelper {

	private static int cubeVBO = -1;

	public static void drawPoints(double partialTicks) {
		if (!EntityCullingConfig.debugRenderBoxes) {
			return;
		}
		if (EntityCulling.isOpenGL44Supported) {
			return;
		}

		if (cubeVBO == -1) {
			Tessellator tesselator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuffer();
			bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			bufferBuilder.pos(0, 0, 0).endVertex();
			bufferBuilder.pos(0, 0, 1).endVertex();
			bufferBuilder.pos(0, 0, 1).endVertex();
			bufferBuilder.pos(1, 0, 1).endVertex();
			bufferBuilder.pos(1, 0, 1).endVertex();
			bufferBuilder.pos(1, 0, 0).endVertex();
			bufferBuilder.pos(1, 0, 0).endVertex();
			bufferBuilder.pos(0, 0, 0).endVertex();

			bufferBuilder.pos(0, 0, 0).endVertex();
			bufferBuilder.pos(0, 1, 0).endVertex();
			bufferBuilder.pos(0, 0, 1).endVertex();
			bufferBuilder.pos(0, 1, 1).endVertex();
			bufferBuilder.pos(1, 0, 0).endVertex();
			bufferBuilder.pos(1, 1, 0).endVertex();
			bufferBuilder.pos(1, 0, 1).endVertex();
			bufferBuilder.pos(1, 1, 1).endVertex();

			bufferBuilder.pos(0, 1, 0).endVertex();
			bufferBuilder.pos(0, 1, 1).endVertex();
			bufferBuilder.pos(0, 1, 1).endVertex();
			bufferBuilder.pos(1, 1, 1).endVertex();
			bufferBuilder.pos(1, 1, 1).endVertex();
			bufferBuilder.pos(1, 1, 0).endVertex();
			bufferBuilder.pos(1, 1, 0).endVertex();
			bufferBuilder.pos(0, 1, 0).endVertex();
			bufferBuilder.finishDrawing();

			cubeVBO = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cubeVBO);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufferBuilder.getByteBuffer(), GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			bufferBuilder.reset();
		}

		Minecraft mc = Minecraft.getMinecraft();
		Entity ce = mc.getRenderViewEntity();
		double camX = ce.lastTickPosX + (ce.posX - ce.lastTickPosX) * partialTicks;
		double camY = ce.lastTickPosY + (ce.posY - ce.lastTickPosY) * partialTicks;
		double camZ = ce.lastTickPosZ + (ce.posZ - ce.lastTickPosZ) * partialTicks;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-camX, -camY, -camZ);

		{
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cubeVBO);
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0);
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

			for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
				AxisAlignedBB aabb = ((IBoundingBoxCache) e).getCachedBoundingBox();
				double px = -(e.posX - e.lastTickPosX) * (1.0D - partialTicks);
				double py = -(e.posY - e.lastTickPosY) * (1.0D - partialTicks);
				double pz = -(e.posZ - e.lastTickPosZ) * (1.0D - partialTicks);
				double sc = 0.5D;

				GlStateManager.pushMatrix();
				GlStateManager.translate(aabb.minX + px - sc, aabb.minY + py - sc, aabb.minZ + pz - sc);
				GlStateManager.scale(aabb.maxX - aabb.minX + sc, aabb.maxY - aabb.minY + sc, aabb.maxZ - aabb.minZ + sc);

				GL11.glDrawArrays(GL11.GL_LINES, 0, 24);

				GlStateManager.popMatrix();
			}

			for (TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList) {
				AxisAlignedBB aabb = ((IBoundingBoxCache) te).getCachedBoundingBox();

				GlStateManager.pushMatrix();
				GlStateManager.translate(aabb.minX, aabb.minY, aabb.minZ);
				GlStateManager.scale(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);

				GL11.glDrawArrays(GL11.GL_LINES, 0, 24);

				GlStateManager.popMatrix();
			}

			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}

		{
			List<RaytraceInfo> copy;
			synchronized (CullingThread.class) {
				copy = new ArrayList<>(CullingThread.publicDebugRayList);
			}
			GL11.glPointSize(4.0F);

			Tessellator tesselator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuffer();
			ByteBuffer byteBuffer = bufferBuilder.getByteBuffer();
			bufferBuilder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);

			for (int i = 0; i < copy.size(); i++) {
				RaytraceInfo raytraceInfo = copy.get(i);
				byteBuffer.putFloat(i * 16, (float) raytraceInfo.x);
				byteBuffer.putFloat(i * 16 + 4, (float) raytraceInfo.y);
				byteBuffer.putFloat(i * 16 + 8, (float) raytraceInfo.z);
				byteBuffer.put(i * 16 + 12, (byte) 255);
				byteBuffer.put(i * 16 + 13, (byte) 255);
				byteBuffer.put(i * 16 + 14, (byte) 255);
				byteBuffer.put(i * 16 + 15, (byte) 255);
				bufferBuilder.endVertex();
			}

			tesselator.draw();
			bufferBuilder.reset();

			GL11.glPointSize(1.0F);
		}

		GlStateManager.popMatrix();

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableFog();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
	}

}
