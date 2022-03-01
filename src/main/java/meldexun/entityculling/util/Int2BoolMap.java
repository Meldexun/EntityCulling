package meldexun.entityculling.util;

import java.util.function.IntPredicate;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;

@SuppressWarnings("serial")
public class Int2BoolMap extends Int2BooleanOpenHashMap {

	public Int2BoolMap() {
		super();
	}

	public Int2BoolMap(int expected, float f) {
		super(expected, f);
	}

	public boolean computeIfAbsent(int k, IntPredicate mappingFunc) {
		int pos = find(k);
		if (pos < 0) {
			boolean v = mappingFunc.test(k);
			insert(-pos - 1, k, v);
			return v;
		}
		return value[pos];
	}

	// @formatter:off
	private int find(final int k) {
		if (k == 0) return containsNullKey ? n : -(n + 1);
		int curr;
		final int[] key = this.key;
		int pos;
		// The starting point.
		if ((curr = key[pos = HashCommon.mix(k) & mask]) == 0) return -(pos + 1);
		if (k == curr) return pos;
		// There's always an unused entry.
		while (true) {
			if ((curr = key[pos = pos + 1 & mask]) == 0) return -(pos + 1);
			if (k == curr) return pos;
		}
	}

	private void insert(final int pos, final int k, final boolean v) {
		if (pos == n) containsNullKey = true;
		key[pos] = k;
		value[pos] = v;
		if (size++ >= maxFill) rehash(HashCommon.arraySize(size + 1, f));
	}
	// @formatter:on

}
