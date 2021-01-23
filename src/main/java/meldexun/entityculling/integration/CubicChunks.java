package meldexun.entityculling.integration;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CubicChunks {

	public static ClassInheritanceMultiMap<Entity> getEntityList(World world, BlockPos pos) {
		return ((io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld) world).getCubeCache().getCube(pos.getX(), pos.getY(), pos.getZ()).getEntitySet();
	}

}
