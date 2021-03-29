package meldexun.entityculling;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface ITileEntityBBCache {

	AxisAlignedBB getPrevAABB();

	void setPrevAABB(AxisAlignedBB aabb);

	BlockPos getPrevPos();

	void setPrevPos(BlockPos pos);

	Block getPrevState();

	void setPrevState(Block state);

	default AxisAlignedBB getCachedAABB() {
		TileEntity tileEntity = (TileEntity) this;
		if (!EntityCullingConfig.enabled) {
			return tileEntity.getRenderBoundingBox();
		}
		if (!tileEntity.hasWorld()) {
			return tileEntity.getRenderBoundingBox();
		}
		AxisAlignedBB prevAABB = this.getPrevAABB();
		BlockPos prevPos = this.getPrevPos();
		Block prevState = this.getPrevState();
		BlockPos pos = tileEntity.getPos();
		Block state = tileEntity.getBlockType();
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
