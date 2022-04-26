package meldexun.entityculling.util.raytracing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

class RaytracingArrayCache implements IRaytracingCache {

	private final int radiusChunks;
	private final int sizeChunks;
	private final RayTracingCacheChunk[] chunks;
	private final List<RayTracingCacheChunk> dirtyChunks = new ArrayList<>();

	public RaytracingArrayCache(int radiusChunks) {
		this.radiusChunks = radiusChunks;
		this.sizeChunks = this.radiusChunks * 2 + 1;
		this.chunks = new RayTracingCacheChunk[this.sizeChunks * this.sizeChunks * this.sizeChunks];

		for (int i = 0; i < this.chunks.length; i++) {
			this.chunks[i] = new RayTracingCacheChunk();
		}
	}

	@Override
	public boolean getOrSetCachedValue(int x, int y, int z, BooleanSupplier function) {
		RayTracingCacheChunk chunk = this.getChunk(x, y, z);
		if (chunk == null) {
			return function.getAsBoolean();
		}
		return chunk.getOrSetCachedValue(x & 15, y & 15, z & 15, function);
	}

	private RayTracingCacheChunk getChunk(int x, int y, int z) {
		x = (x >> 4) + this.radiusChunks;
		if (x < 0 || x >= this.sizeChunks) {
			return null;
		}
		y = (y >> 4) + this.radiusChunks;
		if (y < 0 || y >= this.sizeChunks) {
			return null;
		}
		z = (z >> 4) + this.radiusChunks;
		if (z < 0 || z >= this.sizeChunks) {
			return null;
		}
		return this.chunks[(z * this.sizeChunks + y) * this.sizeChunks + x];
	}

	@Override
	public void clearCache() {
		for (RayTracingCacheChunk chunk : this.dirtyChunks) {
			chunk.clearChunk();
		}
		this.dirtyChunks.clear();
	}

	private class RayTracingCacheChunk {

		private int[] cache = new int[256];
		private boolean dirty = false;

		/**
		 * @param x chunk relative
		 * @param y chunk relative
		 * @param z chunk relative
		 * @return the cached value
		 */
		public boolean getOrSetCachedValue(int x, int y, int z, BooleanSupplier function) {
			int index = (z << 4) | y;
			int offset = x << 1;
			int cachedSection = this.cache[index];
			int cachedValue = (cachedSection >>> offset) & 3;
			if (cachedValue != 0) {
				return cachedValue == 2;
			}
			cachedValue = function.getAsBoolean() ? 2 : 1;
			this.cache[index] = cachedSection | ((cachedValue & 3) << offset);
			this.markDirty();
			return cachedValue == 2;
		}

		private void markDirty() {
			if (!this.dirty) {
				this.dirty = true;
				RaytracingArrayCache.this.dirtyChunks.add(this);
			}
		}

		/**
		 * Checks if this chunk is dirty and cleans it.
		 */
		public void clearChunk() {
			if (this.dirty) {
				Arrays.fill(this.cache, 0);
				this.dirty = false;
			}
		}

	}

}
