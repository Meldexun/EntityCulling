package meldexun.entityculling.util.raytracing;

@FunctionalInterface
public interface Int2BoolTriFunction {

	boolean applyAsBool(int x, int y, int z);

}
