package meldexun.entityculling.util;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class CachedBlockReader implements BlockGetter {

	private Level level;
	private LevelChunk cachedChunk;
	private LevelChunkSection cachedSection;

	public void setupCache(Level level) {
		this.level = level;
		this.cachedChunk = null;
		this.cachedSection = null;
	}

	public void clearCache() {
		this.level = null;
		this.cachedChunk = null;
		this.cachedSection = null;
	}

	private LevelChunk getChunk(int chunkX, int chunkZ) {
		if (this.cachedChunk == null || this.cachedChunk.getPos().x != chunkX || this.cachedChunk.getPos().z != chunkZ) {
			this.cachedChunk = this.level.getChunk(chunkX, chunkZ);
			this.cachedSection = null;
		}
		return this.cachedChunk;
	}

	@Nullable
	private LevelChunkSection getChunkSection(int chunkX, int chunkY, int chunkZ) {
		if (chunkY < 0 || chunkY >= this.level.getMaxBuildHeight() >> 4) {
			return null;
		}
		LevelChunk chunk = this.getChunk(chunkX, chunkZ);
		if (this.cachedSection == null || this.cachedSection.bottomBlockY() >> 4 != chunkY) {
			this.cachedSection = chunk.getSections()[chunkY];
		}
		return this.cachedSection;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (level.isOutsideBuildHeight(pos)) {
			return null;
		}

		LevelChunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

		BlockEntity te = chunk.getBlockEntities().get(pos);
		if (te != null && !te.isRemoved()) {
			return te;
		}

		CompoundTag nbt = chunk.pendingBlockEntities.get(pos);
		if (nbt != null) {
			BlockState state = this.getBlockState(pos);
			BlockEntity te1 = null;
			if ("DUMMY".equals(nbt.getString("id"))) {
				if (state.hasBlockEntity()) {
					te1 = ((EntityBlock) state.getBlock()).newBlockEntity(pos, state);
				}
			} else {
				te1 = BlockEntity.loadStatic(pos, state, nbt);
			}

			if (te1 != null) {
				te1.setLevel(this.level);
				return te1;
			}
		}

		BlockState state = this.getBlockState(pos);
		if (!state.hasBlockEntity()) {
			return null;
		}

		BlockEntity te1 = ((EntityBlock) state.getBlock()).newBlockEntity(pos, state);
		te1.setLevel(level);
		return te1;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		LevelChunkSection section = this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		if (section == null) {
			return Blocks.AIR.defaultBlockState();
		}
		return section.getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		LevelChunkSection section = this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		if (section == null) {
			return Fluids.EMPTY.defaultFluidState();
		}
		return section.getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

	@Override
	public int getHeight() {
		return level.getHeight();
	}

	@Override
	public int getMinBuildHeight() {
		return level.getMinBuildHeight();
	}

}
