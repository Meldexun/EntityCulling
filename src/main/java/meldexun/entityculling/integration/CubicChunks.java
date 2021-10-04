package meldexun.entityculling.integration;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class CubicChunks {

	public static ClassInheritanceMultiMap<Entity> getEntityList(World world, BlockPos pos) {
		if (!((io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld) world).isCubicWorld()) {
			return world.getChunk(pos).getEntityLists()[pos.getY() >> 4];
		}
		return ((io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld) world).getCubeCache().getCube(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)
				.getEntitySet();
	}

	public static ExtendedBlockStorage getBlockStorage(World world, int chunkX, int chunkY, int chunkZ) {
		if (!((io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld) world).isCubicWorld()) {
			return world.getChunkProvider().provideChunk(chunkX, chunkZ).getBlockStorageArray()[chunkY];
		}
		return ((io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld) world).getCubeCache().getCube(chunkX, chunkY, chunkZ).getStorage();
	}

}
