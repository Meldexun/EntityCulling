package meldexun.entityculling;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface ITileEntityBBCache {

	AxisAlignedBB getPrevAABB();

	void setPrevAABB(AxisAlignedBB aabb);

	BlockPos getPrevPos();

	void setPrevPos(BlockPos pos);

	BlockState getPrevState();

	void setPrevState(BlockState state);

	default AxisAlignedBB getCachedAABB() {
		if (!EntityCullingConfig.CLIENT_CONFIG.enabled.get()) {
			return ((TileEntity) this).getRenderBoundingBox();
		}
		TileEntity tileEntity = (TileEntity) this;
		AxisAlignedBB prevAABB = this.getPrevAABB();
		BlockPos prevPos = this.getPrevPos();
		BlockState prevState = this.getPrevState();
		BlockPos pos = tileEntity.getBlockPos();
		BlockState state = tileEntity.getBlockState();
		if (prevAABB != null && pos.equals(prevPos) && state.equals(prevState)) {
			return prevAABB;
		}
		AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();
		this.setPrevAABB(aabb);
		this.setPrevPos(pos);
		this.setPrevState(state);
		return aabb;
	}

}
