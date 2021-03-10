package meldexun.entityculling;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class EntityCullingWorldEventListener implements IWorldEventListener {

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		// not needed
	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		// not needed
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		// not needed
	}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
		// not needed
	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {
		// not needed
	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
		// not needed
	}

	@Override
	public void onEntityRemoved(Entity entityIn) {
		((ICullable) entityIn).deleteQuery();
	}

	@Override
	public void onEntityAdded(Entity entityIn) {
		// not needed
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		// not needed
	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		// not needed
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		// not needed
	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {
		// not needed
	}

}
