package meldexun.entityculling;

import java.util.Arrays;

import javax.annotation.Nullable;

public class RayTracingCache {

	private final int radiusBlocks;
	private final int radiusChunks;
	private final int sizeChunks;
	private final RayTracingCacheChunk[][][] chunks;

	public RayTracingCache(int radiusChunks) {
		this.radiusChunks = radiusChunks;
		this.sizeChunks = this.radiusChunks * 2;
		this.radiusBlocks = this.radiusChunks << 4;
		this.chunks = new RayTracingCacheChunk[this.sizeChunks][this.sizeChunks][this.sizeChunks];

		for (int x = 0; x < this.sizeChunks; x++) {
			for (int y = 0; y < this.sizeChunks; y++) {
				for (int z = 0; z < this.sizeChunks; z++) {
					this.chunks[x][y][z] = new RayTracingCacheChunk();
				}
			}
		}
	}

	public int getCachedValue(int x, int y, int z) {
		x += this.radiusBlocks;
		y += this.radiusBlocks;
		z += this.radiusBlocks;
		RayTracingCacheChunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
		if (chunk == null) {
			return -1;
		}
		return chunk.getCachedValue(x & 15, y & 15, z & 15);
	}

	public void setCachedValue(int x, int y, int z, int value) {
		x += this.radiusBlocks;
		y += this.radiusBlocks;
		z += this.radiusBlocks;
		RayTracingCacheChunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
		if (chunk == null) {
			return;
		}
		chunk.setCachedValue(x & 15, y & 15, z & 15, value);
	}

	@Nullable
	public RayTracingCacheChunk getChunk(int chunkX, int chunkY, int chunkZ) {
		if (chunkX < 0 || chunkX > this.sizeChunks) {
			return null;
		}
		if (chunkY < 0 || chunkY > this.sizeChunks) {
			return null;
		}
		if (chunkZ < 0 || chunkZ > this.sizeChunks) {
			return null;
		}
		return this.chunks[chunkX][chunkY][chunkZ];
	}

	public void clearCache() {
		for (int x = 0; x < this.sizeChunks; x++) {
			for (int y = 0; y < this.sizeChunks; y++) {
				for (int z = 0; z < this.sizeChunks; z++) {
					this.chunks[x][y][z].clearChunk();
				}
			}
		}
	}

	public static class RayTracingCacheChunk {

		private static final int CHUNK_SIZE = 16;
		private int[] cache = new int[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE >> 4];
		private boolean dirty = false;

		/**
		 * @param x chunk relative
		 * @param y chunk relative
		 * @param z chunk relative
		 * @return the cached value
		 */
		public int getCachedValue(int x, int y, int z) {
			int index = (z << 4) | y;
			int offset = (x & 3) << 1;
			return (this.cache[index] >> offset) & 3;
		}

		/**
		 * @param x     chunk relative
		 * @param y     chunk relative
		 * @param z     chunk relative
		 * @param value the value which will be cache
		 */
		public void setCachedValue(int x, int y, int z, int value) {
			int index = (z << 4) | y;
			int offset = (x & 3) << 1;
			this.cache[index] = (this.cache[index] & ~(3 << offset)) | (value & 3) << offset;
			this.dirty = true;
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
