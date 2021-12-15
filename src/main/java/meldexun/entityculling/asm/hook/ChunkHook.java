package meldexun.entityculling.asm.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class ChunkHook {

	public static boolean checkAccess(Chunk chunk) {
		return !chunk.getWorld().isRemote || Minecraft.getMinecraft().isCallingFromMinecraftThread();
	}

	public static TileEntity getTileEntity(Chunk chunk, BlockPos pos) {
		TileEntity tileEntity = chunk.getTileEntityMap().get(pos);
		if (tileEntity != null && tileEntity.isInvalid()) {
			return null;
		}
		return tileEntity;
	}

}
