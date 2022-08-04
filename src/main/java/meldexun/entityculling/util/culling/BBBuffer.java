package meldexun.entityculling.util.culling;

import org.lwjgl.MemoryUtil;

import meldexun.matrixutil.UnsafeUtil;
import meldexun.renderlib.util.GLBuffer;
import meldexun.renderlib.util.GLUtil;
import sun.misc.Unsafe;

public class BBBuffer extends GLBuffer {

	private long address;

	public BBBuffer(long size, int flags, int usage) {
		super(size, flags, usage);
	}

	public BBBuffer(long size, int flags, int usage, boolean persistent, int persistentAccess) {
		super(size, flags, usage, persistent, persistentAccess);
		if (persistent && GLUtil.CAPS.OpenGL44) {
			this.address = MemoryUtil.getAddress(this.getByteBuffer());
		}
	}

	@Override
	public void map(int accessRange, int access) {
		super.map(accessRange, access);
		this.address = MemoryUtil.getAddress(this.getByteBuffer());
	}

	public void put(float x, float y, float z, float sizeX, float sizeY, float sizeZ, int id) {
		Unsafe unsafe = UnsafeUtil.instance();
		long curr;
		unsafe.putFloat(curr = address + id * 28, x);
		unsafe.putFloat(curr += 4, y);
		unsafe.putFloat(curr += 4, z);
		unsafe.putFloat(curr += 4, sizeX);
		unsafe.putFloat(curr += 4, sizeY);
		unsafe.putFloat(curr += 4, sizeZ);
		unsafe.putInt(curr += 4, id);
	}

}
