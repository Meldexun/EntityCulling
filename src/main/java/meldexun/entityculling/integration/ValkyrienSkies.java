package meldexun.entityculling.integration;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class ValkyrienSkies {

	public static AxisAlignedBB getAABB(TileEntity tileEntity) {
		return org.valkyrienskies.mod.common.util.ValkyrienUtils.getAABBInGlobal(tileEntity.getRenderBoundingBox(), tileEntity.getWorld(), tileEntity.getPos());
	}

}
