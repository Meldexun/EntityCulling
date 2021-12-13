package meldexun.entityculling.util;

import javax.annotation.Nullable;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.integration.CubicChunks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.Loader;

public class CachedBlockAccess implements IBlockAccess {

	private final MutableBlockPos mutable = new MutableBlockPos();
	private World level;
	private Chunk cachedChunk;
	private ExtendedBlockStorage cachedSection;

	public void setupCached(World level) {
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
		if (this.cachedChunk == null || this.cachedChunk.x != chunkX || this.cachedChunk.z != chunkZ) {
			this.cachedChunk = this.level.getChunk(chunkX, chunkZ);
			this.cachedSection = null;
		}
		return this.cachedChunk;
	}

	@Nullable
	private ExtendedBlockStorage getChunkSection(int chunkX, int chunkY, int chunkZ) {
		if (this.level.isOutsideBuildHeight(mutable.setPos(chunkX << 4, chunkY << 4, chunkZ << 4))) {
			return null;
		}
		if (EntityCulling.isCubicChunksInstalled) {
			this.cachedSection = CubicChunks.getBlockStorage(this.level, chunkX, chunkY, chunkZ);
		} else {
			Chunk chunk = this.getChunk(chunkX, chunkZ);
			if (this.cachedSection == null || this.cachedSection.getYLocation() >> 4 != chunkY) {
				this.cachedSection = chunk.getBlockStorageArray()[chunkY];
			}
		}
		return this.cachedSection;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		ExtendedBlockStorage section = this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		if (section == null) {
			return Blocks.AIR.getDefaultState();
		}
		return section.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public WorldType getWorldType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		throw new UnsupportedOperationException();
	}

}
