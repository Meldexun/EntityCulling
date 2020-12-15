package meldexun.entityculling.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionConstructor<T> {

	private final Constructor<T> constructor;

	public ReflectionConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		Constructor<T> c = null;
		try {
			c = clazz.getDeclaredConstructor(parameterTypes);
			c.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		this.constructor = c;
	}

	@SuppressWarnings("unchecked")
	public ReflectionConstructor(String className, Class<?>... parameterTypes) {
		Constructor<T> c = null;
		try {
			Class<T> clazz = (Class<T>) Class.forName(className);
			c = clazz.getDeclaredConstructor(parameterTypes);
			c.setAccessible(true);
		} catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		this.constructor = c;
	}

	public T newInstance(Object... initargs) {
		try {
			return (T) this.constructor.newInstance(initargs);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

}
