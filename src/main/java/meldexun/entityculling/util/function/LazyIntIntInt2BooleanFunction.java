package meldexun.entityculling.util.function;

import java.util.function.BooleanSupplier;

public class LazyIntIntInt2BooleanFunction implements BooleanSupplier {

	private final IntIntInt2BooleanFunction function;
	private int x;
	private int y;
	private int z;

	public LazyIntIntInt2BooleanFunction(IntIntInt2BooleanFunction function) {
		this.function = function;
	}

	public void setXYZ(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setZ(int z) {
		this.z = z;
	}

	@Override
	public boolean getAsBoolean() {
		return function.applyAsBool(x, y, z);
	}

}
