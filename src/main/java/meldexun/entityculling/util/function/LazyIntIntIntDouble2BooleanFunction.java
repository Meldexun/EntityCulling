package meldexun.entityculling.util.function;

import java.util.function.BooleanSupplier;

public class LazyIntIntIntDouble2BooleanFunction implements BooleanSupplier {

	private final IntIntIntDoubleToBooleanFunction function;
	private int x;
	private int y;
	private int z;
	private double d;

	public LazyIntIntIntDouble2BooleanFunction(IntIntIntDoubleToBooleanFunction function) {
		this.function = function;
	}

	public void setXYZD(int x, int y, int z, double d) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.d = d;
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

	public void setD(double d) {
		this.d = d;
	}

	@Override
	public boolean getAsBoolean() {
		return function.applyAsBool(x, y, z, d);
	}

}
