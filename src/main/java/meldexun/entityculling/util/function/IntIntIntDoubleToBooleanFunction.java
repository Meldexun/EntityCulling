package meldexun.entityculling.util.function;

@FunctionalInterface
public interface IntIntIntDoubleToBooleanFunction {

	boolean applyAsBool(int x, int y, int z, double d);

}
