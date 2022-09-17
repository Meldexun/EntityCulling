package meldexun.entityculling.util.culling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import meldexun.renderlib.util.GLUtil;
import net.minecraft.client.renderer.GlStateManager;

public class GLHelper {

	private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
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

	public static void clearBufferData(int buffer, int data) {
		if (GLUtil.CAPS.OpenGL45) {
			GL45.glClearNamedBufferData(buffer, GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BUFFER.putInt(0, data));
		} else {
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
			GL43.glClearBufferData(GL15.GL_ARRAY_BUFFER, GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BUFFER.putInt(0, data));
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
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
