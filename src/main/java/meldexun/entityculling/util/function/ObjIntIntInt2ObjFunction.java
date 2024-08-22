package meldexun.entityculling.util.function;

@FunctionalInterface
public interface ObjIntIntInt2ObjFunction<T, R> {

	R apply(T t, int x, int y, int z);

}
