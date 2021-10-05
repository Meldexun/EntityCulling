package meldexun.entityculling.util;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

public class CachedBlockReader implements IBlockReader {

	private World level;
	private Chunk cachedChunk;
	private ChunkSection cachedSection;

	public void setupCache(World level) {
		this.level = level;
		this.cachedChunk = null;
		this.cachedSection = null;
	}

	public void clearCache() {
		this.level = null;
		this.cachedChunk = null;
		this.cachedSection = null;
	}

	private Chunk getChunk(int chunkX, int chunkZ) {
		if (this.cachedChunk == null || this.cachedChunk.getPos().x != chunkX || this.cachedChunk.getPos().z != chunkZ) {
			this.cachedChunk = this.level.getChunk(chunkX, chunkZ);
			this.cachedSection = null;
		}
		return this.cachedChunk;
	}

	@Nullable
	private ChunkSection getChunkSection(int chunkX, int chunkY, int chunkZ) {
		if (chunkY < 0 || chunkY >= this.level.getMaxBuildHeight() >> 4) {
			return null;
		}
		Chunk chunk = this.getChunk(chunkX, chunkZ);
		if (this.cachedSection == null || this.cachedSection.bottomBlockY() >> 4 != chunkY) {
			this.cachedSection = chunk.getSections()[chunkY];
		}
		return this.cachedSection;
	}

	@Override
	@Nullable
	public TileEntity getBlockEntity(BlockPos pos) {
		if (pos.getY() < 0 || pos.getY() >= this.level.getMaxBuildHeight()) {
			return null;
		}

		for (TileEntity te : this.level.pendingBlockEntities) {
			if (te.isRemoved()) {
				continue;
			}
			if (!te.getBlockPos().equals(pos)) {
				continue;
			}
			return te;
		}

		Chunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

		TileEntity te = chunk.getBlockEntities().get(pos);
		if (te != null && !te.isRemoved()) {
			return te;
		}

		CompoundNBT nbt = chunk.pendingBlockEntities.get(pos);
		if (nbt != null) {
			BlockState state = this.getBlockState(pos);
			TileEntity te1 = null;
			if ("DUMMY".equals(nbt.getString("id"))) {
				if (state.hasTileEntity()) {
					te1 = state.createTileEntity(this.level);
				}
			} else {
				te1 = TileEntity.loadStatic(state, nbt);
			}

			if (te1 != null) {
				te1.setLevelAndPosition(this.level, pos);
				return te1;
			}
		}

		BlockState state = this.getBlockState(pos);
		return !state.hasTileEntity() ? null : state.createTileEntity(this.level);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		ChunkSection section = this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		if (section == null) {
			return Blocks.AIR.defaultBlockState();
		}
		return section.getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		ChunkSection section = this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		if (section == null) {
			return Fluids.EMPTY.defaultFluidState();
		}
		return section.getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

}
