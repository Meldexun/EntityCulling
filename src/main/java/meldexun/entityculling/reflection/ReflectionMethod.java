package meldexun.entityculling.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionMethod<T> {

	private final Method method;

	public ReflectionMethod(Class<?> clazz, String obfuscatedName, String deobfuscatedName, Class<?>... parameterTypes) {
		Method m = null;
		try {
			try {
				m = clazz.getDeclaredMethod(obfuscatedName, parameterTypes);
				m.setAccessible(true);
			} catch (NoSuchMethodException e) {
				m = clazz.getDeclaredMethod(deobfuscatedName, parameterTypes);
				m.setAccessible(true);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		this.method = m;
	}

	public ReflectionMethod(String className, String obfuscatedName, String deobfuscatedName, Class<?>... parameterTypes) {
		Method m = null;
		try {
			Class<?> clazz = Class.forName(className);
			try {
				m = clazz.getDeclaredMethod(obfuscatedName, parameterTypes);
				m.setAccessible(true);
			} catch (NoSuchMethodException e) {
				m = clazz.getDeclaredMethod(deobfuscatedName, parameterTypes);
				m.setAccessible(true);
			}
		} catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		this.method = m;
	}

	@SuppressWarnings("unchecked")
	public T invoke(Object obj, Object... args) {
		try {
			return (T) this.method.invoke(obj, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
			e.printStackTrace();
		}
		return null;
	}

}
