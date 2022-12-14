package meldexun.entityculling.util.culling;

import meldexun.matrixutil.MemoryUtil;
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
		Unsafe unsafe = UnsafeUtil.UNSAFE;
		long address = this.address + id * 28;
		unsafe.putFloat(address, x);
		unsafe.putFloat(address + 4, y);
		unsafe.putFloat(address + 8, z);
		unsafe.putFloat(address + 12, sizeX);
		unsafe.putFloat(address + 16, sizeY);
		unsafe.putFloat(address + 20, sizeZ);
		unsafe.putInt(address + 24, id);
	}

}
