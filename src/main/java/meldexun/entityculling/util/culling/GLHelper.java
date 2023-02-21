package meldexun.entityculling.util.culling;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import meldexun.matrixutil.MemoryUtil;
import meldexun.matrixutil.UnsafeUtil;
import meldexun.renderlib.util.BufferUtil;
import meldexun.renderlib.util.GLUtil;
import net.minecraft.client.renderer.GlStateManager;

public class GLHelper {

	private static final ByteBuffer BUFFER = BufferUtil.allocate(4);
	private static final long BUFFER_ADDRESS = MemoryUtil.getAddress(BUFFER);
	private static boolean blend;
	private static int blendSrcFactor;
	private static int blendDstFactor;
	private static int blendSrcFactorAlpha;
	private static int blendDstFactorAlpha;
	private static boolean depthTest;
	private static int depthFunc;
	private static boolean depthMask;
	private static boolean cull;
	private static int cullFace;
	private static boolean colorMaskRed;
	private static boolean colorMaskGreen;
	private static boolean colorMaskBlue;
	private static boolean colorMaskAlpha;

	public static void clearBufferSubData(int buffer, long offset, long size, int data) {
		UnsafeUtil.UNSAFE.putInt(BUFFER_ADDRESS, data);
		if (GLUtil.CAPS.OpenGL45) {
			GL45.glClearNamedBufferSubData(buffer, GL30.GL_R32UI, offset, size, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BUFFER);
		} else {
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
			GL43.glClearBufferSubData(GL15.GL_ARRAY_BUFFER, GL30.GL_R32UI, offset, size, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BUFFER);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
	}

	public static void copyBufferSubData(int readBuffer, int writeBuffer, long readOffset, long writeOffset, long size) {
		if (GLUtil.CAPS.OpenGL45) {
			GL45.glCopyNamedBufferSubData(readBuffer, writeBuffer, readOffset, writeOffset, size);
		} else {
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, readBuffer);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, writeBuffer);
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, size);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
		}
	}

	public static void saveShaderGLState() {
		blend = GlStateManager.blendState.blend.currentState;
		blendSrcFactor = GlStateManager.blendState.srcFactor;
		blendDstFactor = GlStateManager.blendState.dstFactor;
		blendSrcFactorAlpha = GlStateManager.blendState.srcFactorAlpha;
		blendDstFactorAlpha = GlStateManager.blendState.dstFactorAlpha;

		depthTest = GlStateManager.depthState.depthTest.currentState;
		depthFunc = GlStateManager.depthState.depthFunc;
		depthMask = GlStateManager.depthState.maskEnabled;

		cull = GlStateManager.cullState.cullFace.currentState;
		cullFace = GlStateManager.cullState.mode;

		colorMaskRed = GlStateManager.colorMaskState.red;
		colorMaskGreen = GlStateManager.colorMaskState.green;
		colorMaskBlue = GlStateManager.colorMaskState.blue;
		colorMaskAlpha = GlStateManager.colorMaskState.alpha;
	}

	public static void restoreShaderGLState() {
		if (blend) {
			GlStateManager.enableBlend();
		} else {
			GlStateManager.disableBlend();
		}
		GlStateManager.tryBlendFuncSeparate(blendSrcFactor, blendDstFactor, blendSrcFactorAlpha, blendDstFactorAlpha);

		if (depthTest) {
			GlStateManager.enableDepth();
		} else {
			GlStateManager.disableDepth();
		}
		GlStateManager.depthFunc(depthFunc);
		GlStateManager.depthMask(depthMask);

		if (cull) {
			GlStateManager.enableCull();
		} else {
			GlStateManager.disableCull();
		}
		GlStateManager.cullFace(cullFace);

		GlStateManager.colorMask(colorMaskRed, colorMaskGreen, colorMaskBlue, colorMaskAlpha);
	}

}
