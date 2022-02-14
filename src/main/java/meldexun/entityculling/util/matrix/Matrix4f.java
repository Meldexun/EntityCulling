package meldexun.entityculling.util.matrix;

import java.nio.FloatBuffer;

public class Matrix4f {

	public float f00;
	public float f01;
	public float f02;
	public float f03;
	public float f10;
	public float f11;
	public float f12;
	public float f13;
	public float f20;
	public float f21;
	public float f22;
	public float f23;
	public float f30;
	public float f31;
	public float f32;
	public float f33;

	public Matrix4f() {

	}

	public Matrix4f(float f00, float f01, float f02, float f03,
			float f10, float f11, float f12, float f13,
			float f20, float f21, float f22, float f23,
			float f30, float f31, float f32, float f33) {
		this.f00 = f00;
		this.f01 = f01;
		this.f02 = f02;
		this.f03 = f03;
		this.f10 = f10;
		this.f11 = f11;
		this.f12 = f12;
		this.f13 = f13;
		this.f20 = f20;
		this.f21 = f21;
		this.f22 = f22;
		this.f23 = f23;
		this.f30 = f30;
		this.f31 = f31;
		this.f32 = f32;
		this.f33 = f33;
	}

	public static Matrix4f translateMatrix(float x, float y, float z) {
		Matrix4f matrix = new Matrix4f();
		matrix.f00 = 1.0F;
		matrix.f11 = 1.0F;
		matrix.f22 = 1.0F;
		matrix.f33 = 1.0F;
		matrix.f03 = x;
		matrix.f13 = y;
		matrix.f23 = z;
		return matrix;
	}

	public void multiply(Matrix4f m) {
		float nf00 = this.f00 * m.f00 + this.f01 * m.f10 + this.f02 * m.f20 + this.f03 * m.f30;
		float nf01 = this.f00 * m.f01 + this.f01 * m.f11 + this.f02 * m.f21 + this.f03 * m.f31;
		float nf02 = this.f00 * m.f02 + this.f01 * m.f12 + this.f02 * m.f22 + this.f03 * m.f32;
		float nf03 = this.f00 * m.f03 + this.f01 * m.f13 + this.f02 * m.f23 + this.f03 * m.f33;
		float nf10 = this.f10 * m.f00 + this.f11 * m.f10 + this.f12 * m.f20 + this.f13 * m.f30;
		float nf11 = this.f10 * m.f01 + this.f11 * m.f11 + this.f12 * m.f21 + this.f13 * m.f31;
		float nf12 = this.f10 * m.f02 + this.f11 * m.f12 + this.f12 * m.f22 + this.f13 * m.f32;
		float nf13 = this.f10 * m.f03 + this.f11 * m.f13 + this.f12 * m.f23 + this.f13 * m.f33;
		float nf20 = this.f20 * m.f00 + this.f21 * m.f10 + this.f22 * m.f20 + this.f23 * m.f30;
		float nf21 = this.f20 * m.f01 + this.f21 * m.f11 + this.f22 * m.f21 + this.f23 * m.f31;
		float nf22 = this.f20 * m.f02 + this.f21 * m.f12 + this.f22 * m.f22 + this.f23 * m.f32;
		float nf23 = this.f20 * m.f03 + this.f21 * m.f13 + this.f22 * m.f23 + this.f23 * m.f33;
		float nf30 = this.f30 * m.f00 + this.f31 * m.f10 + this.f32 * m.f20 + this.f33 * m.f30;
		float nf31 = this.f30 * m.f01 + this.f31 * m.f11 + this.f32 * m.f21 + this.f33 * m.f31;
		float nf32 = this.f30 * m.f02 + this.f31 * m.f12 + this.f32 * m.f22 + this.f33 * m.f32;
		float nf33 = this.f30 * m.f03 + this.f31 * m.f13 + this.f32 * m.f23 + this.f33 * m.f33;

		this.f00 = nf00;
		this.f01 = nf01;
		this.f02 = nf02;
		this.f03 = nf03;

		this.f10 = nf10;
		this.f11 = nf11;
		this.f12 = nf12;
		this.f13 = nf13;

		this.f20 = nf20;
		this.f21 = nf21;
		this.f22 = nf22;
		this.f23 = nf23;

		this.f30 = nf30;
		this.f31 = nf31;
		this.f32 = nf32;
		this.f33 = nf33;
	}

	public void store(FloatBuffer buffer) {
		buffer.put(bufferIndex(0, 0), this.f00);
		buffer.put(bufferIndex(0, 1), this.f01);
		buffer.put(bufferIndex(0, 2), this.f02);
		buffer.put(bufferIndex(0, 3), this.f03);
		buffer.put(bufferIndex(1, 0), this.f10);
		buffer.put(bufferIndex(1, 1), this.f11);
		buffer.put(bufferIndex(1, 2), this.f12);
		buffer.put(bufferIndex(1, 3), this.f13);
		buffer.put(bufferIndex(2, 0), this.f20);
		buffer.put(bufferIndex(2, 1), this.f21);
		buffer.put(bufferIndex(2, 2), this.f22);
		buffer.put(bufferIndex(2, 3), this.f23);
		buffer.put(bufferIndex(3, 0), this.f30);
		buffer.put(bufferIndex(3, 1), this.f31);
		buffer.put(bufferIndex(3, 2), this.f32);
		buffer.put(bufferIndex(3, 3), this.f33);
	}

	public void load(FloatBuffer buffer) {
		this.f00 = buffer.get(bufferIndex(0, 0));
		this.f01 = buffer.get(bufferIndex(0, 1));
		this.f02 = buffer.get(bufferIndex(0, 2));
		this.f03 = buffer.get(bufferIndex(0, 3));
		this.f10 = buffer.get(bufferIndex(1, 0));
		this.f11 = buffer.get(bufferIndex(1, 1));
		this.f12 = buffer.get(bufferIndex(1, 2));
		this.f13 = buffer.get(bufferIndex(1, 3));
		this.f20 = buffer.get(bufferIndex(2, 0));
		this.f21 = buffer.get(bufferIndex(2, 1));
		this.f22 = buffer.get(bufferIndex(2, 2));
		this.f23 = buffer.get(bufferIndex(2, 3));
		this.f30 = buffer.get(bufferIndex(3, 0));
		this.f31 = buffer.get(bufferIndex(3, 1));
		this.f32 = buffer.get(bufferIndex(3, 2));
		this.f33 = buffer.get(bufferIndex(3, 3));
	}

	private static int bufferIndex(int x, int y) {
		return (y << 2) | x;
	}

}
