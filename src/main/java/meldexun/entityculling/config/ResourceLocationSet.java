package meldexun.entityculling.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.util.ResourceLocation;

public class ResourceLocationSet<T> {

	private interface IStringSet {

		static IStringSet alwaysTrue() {
			return new IStringSet() {

				@Override
				public boolean contains(String s) {
					return true;
				}

				@Override
				public void add(String s) {

				}

			};
		}

		static IStringSet ofHashSet() {
			return ofSet(new HashSet<>());
		}

		static IStringSet ofSet(Set<String> set) {
			return new IStringSet() {

				@Override
				public boolean contains(String s) {
					return set.contains(s);
				}

				@Override
				public void add(String s) {
					set.add(s);
				}

			};
		}

		void add(String s);

		boolean contains(String s);

	}

	private final Map<String, IStringSet> namespace2pathsMap = new HashMap<>();
	private final Object2BooleanMap<Class<? extends T>> class2containedMap = new Object2BooleanOpenHashMap<>();
	private final Function<Class<? extends T>, ResourceLocation> resourceLocationFunc;

	public ResourceLocationSet(Function<Class<? extends T>, ResourceLocation> func) {
		this.resourceLocationFunc = func;
	}

	@SuppressWarnings("unchecked")
	public boolean contains(T t) {
		if (namespace2pathsMap.isEmpty()) {
			return false;
		}
		return class2containedMap.computeIfAbsent((Class<? extends T>) t.getClass(), k -> {
			ResourceLocation resourceLocation = resourceLocationFunc.apply(k);
			IStringSet set = namespace2pathsMap.get(resourceLocation.getNamespace());
			return set != null && set.contains(resourceLocation.getPath());
		});
	}

	public void load(String[] data) {
		this.namespace2pathsMap.clear();
		this.class2containedMap.clear();
		for (String s : data) {
			int i = s.indexOf(':');
			if (i == -1) {
				namespace2pathsMap.put(s, IStringSet.alwaysTrue());
			} else {
				namespace2pathsMap.computeIfAbsent(s.substring(0, i), k -> IStringSet.ofHashSet()).add(s.substring(i + 1));
			}
		}
	}

}
