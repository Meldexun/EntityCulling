package meldexun.entityculling.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;

public class Buffer {

	private final int buffer;
	private final int size;
	private final int flags;
	private final int usage;

	private boolean mapped;
	private ByteBuffer byteBuffer;
	private FloatBuffer floatBuffer;
	private IntBuffer intBuffer;

	public Buffer(int size, int flags, int usage) {
		this.buffer = GL15.glGenBuffers();
		this.size = size;
		this.flags = flags;
		this.usage = usage;

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
		GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, size, flags);
		this.byteBuffer = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0, size, flags, null);
		this.floatBuffer = this.byteBuffer.asFloatBuffer();
		this.intBuffer = this.byteBuffer.asIntBuffer();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void map(int access, long length) {
		/*
		if (!this.mapped) {
			this.mapped = true;
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
			this.byteBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, access, length, this.byteBuffer);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			this.floatBuffer = this.byteBuffer.asFloatBuffer();
			this.intBuffer = this.byteBuffer.asIntBuffer();
		}
		*/
		throw new UnsupportedOperationException();
	}

	public void unmap() {
		/*
		if (this.mapped) {
			this.mapped = false;
			this.floatBuffer = null;
			this.intBuffer = null;
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
			GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		*/
		throw new UnsupportedOperationException();
	}

	public int getBuffer() {
		return buffer;
	}

	public int getSize() {
		return size;
	}

	public int getFlags() {
		return flags;
	}

	public int getUsage() {
		return usage;
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public FloatBuffer getFloatBuffer() {
		return floatBuffer;
	}

	public IntBuffer getIntBuffer() {
		return intBuffer;
	}

}
