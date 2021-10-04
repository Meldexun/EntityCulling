package meldexun.entityculling.util;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class BoundingBoxHelper {

	public static void drawBox(AxisAlignedBB aabb, double camX, double camY, double camZ) {
		GlStateManager.disableAlpha();
		GlStateManager.disableFog();
		GlStateManager.disableTexture2D();

		GlStateManager.pushMatrix();
		GlStateManager.translate(-camX, -camY, -camZ);
		GlStateManager.glBegin(GL11.GL_LINES);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ);

		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);

		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ);
		GlStateManager.glVertex3f((float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ);
		GlStateManager.glEnd();
		GlStateManager.popMatrix();

		GlStateManager.enableTexture2D();
		GlStateManager.enableFog();
		GlStateManager.enableAlpha();
	}

	public static void drawPoints() {
		if (EntityCullingConfig.debugRenderBoxes) {
			List<double[]> copy;
			synchronized (CullingThread.class) {
				copy = new ArrayList<>(CullingThread.publicDebugRayList);
			}
			Minecraft mc = Minecraft.getMinecraft();
			Entity viewEntity = mc.getRenderViewEntity();
			double partialTicks = mc.getRenderPartialTicks();
			double x = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
			double y = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
			double z = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.disableFog();
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.depthFunc(GL11.GL_ALWAYS);
			GlStateManager.depthMask(true);
			GL11.glPointSize(4.0F);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-x, -y, -z);
			GlStateManager.glBegin(GL11.GL_POINTS);
			for (double[] v : copy) {
				if (v[3] == 1) {
					GlStateManager.color(1, 1, 1);
				} else {
					GlStateManager.color(1, 0, 0);
				}
				GlStateManager.glVertex3f((float) v[0], (float) v[1], (float) v[2]);
			}
			GlStateManager.color(1, 1, 1);
			GlStateManager.glEnd();
			GlStateManager.popMatrix();
			GL11.glPointSize(1.0F);
			GlStateManager.depthMask(false);
			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableFog();
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
		}
	}

}
