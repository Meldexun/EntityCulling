package meldexun.entityculling.util.culling;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import meldexun.memoryutil.UnsafeBufferUtil;
import meldexun.memoryutil.UnsafeByteBuffer;
import meldexun.renderlib.util.GLUtil;

public class GLHelper {

	private static final UnsafeByteBuffer BYTE_BUFFER = UnsafeBufferUtil.allocateByte(4);

	public static void clearBufferSubData(int buffer, long offset, long size, int data) {
		BYTE_BUFFER.putInt(0L, data);
		if (GLUtil.CAPS.OpenGL45) {
			GL45.glClearNamedBufferSubData(buffer, GL30.GL_R32UI, offset, size, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BYTE_BUFFER.getBuffer());
		} else {
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
			GL43.glClearBufferSubData(GL15.GL_ARRAY_BUFFER, GL30.GL_R32UI, offset, size, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, BYTE_BUFFER.getBuffer());
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

}
