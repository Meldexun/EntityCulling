package meldexun.entityculling.util.culling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import meldexun.renderlib.util.GLUtil;

public class GLHelper {

	private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());

	public static void clearBufferData(int buffer, int data) {
		if (GLUtil.CAPS.OpenGL45) {
			GL45.glClearNamedBufferData(buffer, GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BUFFER.putInt(0, data));
		} else {
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
			GL43.glClearBufferData(GL15.GL_ARRAY_BUFFER, GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BUFFER.putInt(0, data));
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
	}

}
