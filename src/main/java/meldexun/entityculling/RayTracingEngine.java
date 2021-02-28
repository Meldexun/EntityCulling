package meldexun.entityculling;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Copy of vanilla ray trace algorithm which uses {@link MutableRayTraceResult}, {@link MutableBlockPos}, {@link MutableVec3d} to save memory.
 */
public final class RayTracingEngine {

	private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();
	private static final MutableVec3d MUTABLE_VEC1 = new MutableVec3d();
	private static final MutableVec3d MUTABLE_VEC2 = new MutableVec3d();

	private RayTracingEngine() {

	}

	/** Copied from {@link World#rayTraceBlocks(Vec3d, Vec3d, boolean, boolean, boolean)} */
	@Nullable
	public static MutableRayTraceResult rayTraceBlocks(World world, double startX, double startY, double startZ, double endX, double endY, double endZ, @Nullable BlockPos toIgnore, MutableRayTraceResult returnValue) {
		if (!Double.isNaN(startX) && !Double.isNaN(startY) && !Double.isNaN(startZ)) {
			if (!Double.isNaN(endX) && !Double.isNaN(endY) && !Double.isNaN(endZ)) {
				int blockStartX = MathHelper.floor(startX);
				int blockStartY = MathHelper.floor(startY);
				int blockStartZ = MathHelper.floor(startZ);
				IBlockState state;
				Block block;

				MUTABLE_POS.setPos(blockStartX, blockStartY, blockStartZ);
				if (toIgnore == null || !MUTABLE_POS.equals(toIgnore)) {
					state = world.getBlockState(MUTABLE_POS);
					block = state.getBlock();

					if (state.isOpaqueCube() && state.getCollisionBoundingBox(world, MUTABLE_POS) != Block.NULL_AABB && block.canCollideCheck(state, false)) {
						MutableRayTraceResult rayTraceResult = rayTrace(world, state, MUTABLE_POS, startX, startY, startZ, endX, endY, endZ, returnValue);

						if (rayTraceResult != null) {
							return rayTraceResult;
						}
					}
				}

				int i = 256;
				double x = startX;
				double y = startY;
				double z = startZ;
				int blockEndX = MathHelper.floor(endX);
				int blockEndY = MathHelper.floor(endY);
				int blockEndZ = MathHelper.floor(endZ);

				while (i-- >= 0) {
					if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
						return null;
					}

					if (blockStartX == blockEndX && blockStartY == blockEndY && blockStartZ == blockEndZ) {
						return null;
					}

					boolean flagX = true;
					boolean flagY = true;
					boolean flagZ = true;
					double dx = 999.0D;
					double dy = 999.0D;
					double dz = 999.0D;

					if (blockEndX > blockStartX) {
						dx = (double) blockStartX + 1.0D;
					} else if (blockEndX < blockStartX) {
						dx = (double) blockStartX + 0.0D;
					} else {
						flagX = false;
					}

					if (blockEndY > blockStartY) {
						dy = (double) blockStartY + 1.0D;
					} else if (blockEndY < blockStartY) {
						dy = (double) blockStartY + 0.0D;
					} else {
						flagY = false;
					}

					if (blockEndZ > blockStartZ) {
						dz = (double) blockStartZ + 1.0D;
					} else if (blockEndZ < blockStartZ) {
						dz = (double) blockStartZ + 0.0D;
					} else {
						flagZ = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					double d6 = endX - x;
					double d7 = endY - y;
					double d8 = endZ - z;

					if (flagX) {
						d3 = (dx - x) / d6;
					}

					if (flagY) {
						d4 = (dy - y) / d7;
					}

					if (flagZ) {
						d5 = (dz - z) / d8;
					}

					if (d3 == -0.0D) {
						d3 = -1.0E-4D;
					}

					if (d4 == -0.0D) {
						d4 = -1.0E-4D;
					}

					if (d5 == -0.0D) {
						d5 = -1.0E-4D;
					}

					EnumFacing enumfacing;

					if (d3 < d4 && d3 < d5) {
						enumfacing = blockEndX > blockStartX ? EnumFacing.WEST : EnumFacing.EAST;
						x = dx;
						y = y + d7 * d3;
						z = z + d8 * d3;
					} else if (d4 < d5) {
						enumfacing = blockEndY > blockStartY ? EnumFacing.DOWN : EnumFacing.UP;
						x = x + d6 * d4;
						y = dy;
						z = z + d8 * d4;
					} else {
						enumfacing = blockEndZ > blockStartZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
						x = x + d6 * d5;
						y = y + d7 * d5;
						z = dz;
					}

					blockStartX = MathHelper.floor(x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
					blockStartY = MathHelper.floor(y) - (enumfacing == EnumFacing.UP ? 1 : 0);
					blockStartZ = MathHelper.floor(z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);

					MUTABLE_POS.setPos(blockStartX, blockStartY, blockStartZ);
					if (toIgnore == null || !MUTABLE_POS.equals(toIgnore)) {
						state = world.getBlockState(MUTABLE_POS);
						block = state.getBlock();

						if (state.isOpaqueCube() && state.getCollisionBoundingBox(world, MUTABLE_POS) != Block.NULL_AABB && block.canCollideCheck(state, false)) {
							MutableRayTraceResult rayTraceResult = rayTrace(world, state, MUTABLE_POS, x, y, z, endX, endY, endZ, returnValue);

							if (rayTraceResult != null) {
								return rayTraceResult;
							}
						}
					}
				}

				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/** Copied from {@link Block#collisionRayTrace(IBlockState, World, BlockPos, Vec3d, Vec3d)} and {@link Block#rayTrace(BlockPos, Vec3d, Vec3d, AxisAlignedBB)} */
	@Nullable
	private static MutableRayTraceResult rayTrace(World world, IBlockState state, BlockPos pos, double x1, double y1, double z1, double x2, double y2, double z2, MutableRayTraceResult returnValue) {
		AxisAlignedBB aabb = state.getCollisionBoundingBox(world, pos);
		MutableRayTraceResult mutableRayTraceResult = calculateIntercept(aabb, x1 - pos.getX(), y1 - pos.getY(), z1 - pos.getZ(), x2 - pos.getX(), y2 - pos.getY(), z2 - pos.getZ(), returnValue);
		return mutableRayTraceResult == null ? null : mutableRayTraceResult.set(mutableRayTraceResult.x + pos.getX(), mutableRayTraceResult.y + pos.getY(), mutableRayTraceResult.z + pos.getZ(), mutableRayTraceResult.facing);
	}

	/** Copied from {@link AxisAlignedBB#calculateIntercept(Vec3d, Vec3d)} */
	@Nullable
	private static MutableRayTraceResult calculateIntercept(AxisAlignedBB aabb, double x1, double y1, double z1, double x2, double y2, double z2, MutableRayTraceResult returnValue) {
		MutableVec3d vec1 = collideWithXPlane(aabb, aabb.minX, x1, y1, z1, x2, y2, z2, MUTABLE_VEC1);
		EnumFacing facing = EnumFacing.WEST;
		MutableVec3d vec2;

		vec2 = collideWithXPlane(aabb, aabb.maxX, x1, y1, z1, x2, y2, z2, MUTABLE_VEC2);
		if (vec2 != null && isClosest(x1, y1, z1, vec1, vec2)) {
			vec1 = MUTABLE_VEC1.set(vec2);
			facing = EnumFacing.EAST;
		}

		vec2 = collideWithYPlane(aabb, aabb.minY, x1, y1, z1, x2, y2, z2, MUTABLE_VEC2);
		if (vec2 != null && isClosest(x1, y1, z1, vec1, vec2)) {
			vec1 = MUTABLE_VEC1.set(vec2);
			facing = EnumFacing.DOWN;
		}

		vec2 = collideWithYPlane(aabb, aabb.maxY, x1, y1, z1, x2, y2, z2, MUTABLE_VEC2);
		if (vec2 != null && isClosest(x1, y1, z1, vec1, vec2)) {
			vec1 = MUTABLE_VEC1.set(vec2);
			facing = EnumFacing.UP;
		}

		vec2 = collideWithZPlane(aabb, aabb.minZ, x1, y1, z1, x2, y2, z2, MUTABLE_VEC2);
		if (vec2 != null && isClosest(x1, y1, z1, vec1, vec2)) {
			vec1 = MUTABLE_VEC1.set(vec2);
			facing = EnumFacing.NORTH;
		}

		vec2 = collideWithZPlane(aabb, aabb.maxZ, x1, y1, z1, x2, y2, z2, MUTABLE_VEC2);
		if (vec2 != null && isClosest(x1, y1, z1, vec1, vec2)) {
			vec1 = MUTABLE_VEC1.set(vec2);
			facing = EnumFacing.SOUTH;
		}

		return vec1 == null ? null : returnValue.set(vec1.x, vec1.y, vec1.z, facing);
	}

	/** Copied from {@link AxisAlignedBB#isClosest(Vec3d, Vec3d, Vec3d)} */
	private static boolean isClosest(double x, double y, double z, @Nullable MutableVec3d vec1, MutableVec3d vec2) {
		return vec1 == null || vec2.squareDist(x, y, z) < vec1.squareDist(x, y, z);
	}

	/** Copied from {@link AxisAlignedBB#collideWithXPlane(double, Vec3d, Vec3d)} */
	@Nullable
	private static MutableVec3d collideWithXPlane(AxisAlignedBB aabb, double x, double x1, double y1, double z1, double x2, double y2, double z2, MutableVec3d returnVec) {
		MutableVec3d vec = getIntermediateWithXValue(x, x1, y1, z1, x2, y2, z2, returnVec);
		return vec != null && intersectsWithYZ(aabb, vec) ? vec : null;
	}

	/** Copied from {@link AxisAlignedBB#collideWithYPlane(double, Vec3d, Vec3d)} */
	@Nullable
	private static MutableVec3d collideWithYPlane(AxisAlignedBB aabb, double y, double x1, double y1, double z1, double x2, double y2, double z2, MutableVec3d returnVec) {
		MutableVec3d vec = getIntermediateWithYValue(y, x1, y1, z1, x2, y2, z2, returnVec);
		return vec != null && intersectsWithXZ(aabb, vec) ? vec : null;
	}

	/** Copied from {@link AxisAlignedBB#collideWithZPlane(double, Vec3d, Vec3d)} */
	@Nullable
	private static MutableVec3d collideWithZPlane(AxisAlignedBB aabb, double z, double x1, double y1, double z1, double x2, double y2, double z2, MutableVec3d returnVec) {
		MutableVec3d vec = getIntermediateWithZValue(z, x1, y1, z1, x2, y2, z2, returnVec);
		return vec != null && intersectsWithXY(aabb, vec) ? vec : null;
	}

	/** Copied from {@link Vec3d#getIntermediateWithXValue(Vec3d, double)} */
	@Nullable
	private static MutableVec3d getIntermediateWithXValue(double x, double x1, double y1, double z1, double x2, double y2, double z2, MutableVec3d returnVec) {
		double d0 = x1 - x2;
		double d1 = y1 - y2;
		double d2 = z1 - z2;

		if (d0 * d0 < 1.0000000116860974E-7D) {
			return null;
		} else {
			double d3 = (x - x1) / d0;
			return d3 >= 0.0D && d3 <= 1.0D ? returnVec.set(x1 + d0 * d3, y1 + d1 * d3, z1 + d2 * d3) : null;
		}
	}

	/** Copied from {@link Vec3d#getIntermediateWithYValue(Vec3d, double)} */
	@Nullable
	private static MutableVec3d getIntermediateWithYValue(double y, double x1, double y1, double z1, double x2, double y2, double z2, MutableVec3d returnVec) {
		double d0 = x1 - x2;
		double d1 = y1 - y2;
		double d2 = z1 - z2;

		if (d1 * d1 < 1.0000000116860974E-7D) {
			return null;
		} else {
			double d3 = (y - y1) / d1;
			return d3 >= 0.0D && d3 <= 1.0D ? returnVec.set(x1 + d0 * d3, y1 + d1 * d3, z1 + d2 * d3) : null;
		}
	}

	/** Copied from {@link Vec3d#getIntermediateWithZValue(Vec3d, double)} */
	@Nullable
	private static MutableVec3d getIntermediateWithZValue(double z, double x1, double y1, double z1, double x2, double y2, double z2, MutableVec3d returnVec) {
		double d0 = x1 - x2;
		double d1 = y1 - y2;
		double d2 = z1 - z2;

		if (d2 * d2 < 1.0000000116860974E-7D) {
			return null;
		} else {
			double d3 = (z - z1) / d2;
			return d3 >= 0.0D && d3 <= 1.0D ? returnVec.set(x1 + d0 * d3, y1 + d1 * d3, z1 + d2 * d3) : null;
		}
	}

	/** Copied from {@link AxisAlignedBB#intersectsWithYZ(Vec3d)} */
	private static boolean intersectsWithYZ(AxisAlignedBB aabb, MutableVec3d vec) {
		return vec.y >= aabb.minY && vec.y <= aabb.maxY && vec.z >= aabb.minZ && vec.z <= aabb.maxZ;
	}

	/** Copied from {@link AxisAlignedBB#intersectsWithXZ(Vec3d)} */
	private static boolean intersectsWithXZ(AxisAlignedBB aabb, MutableVec3d vec) {
		return vec.x >= aabb.minX && vec.x <= aabb.maxX && vec.z >= aabb.minZ && vec.z <= aabb.maxZ;
	}

	/** Copied from {@link AxisAlignedBB#intersectsWithXY(Vec3d)} */
	private static boolean intersectsWithXY(AxisAlignedBB aabb, MutableVec3d vec) {
		return vec.x >= aabb.minX && vec.x <= aabb.maxX && vec.y >= aabb.minY && vec.y <= aabb.maxY;
	}

	public static class MutableRayTraceResult {

		public double x = 0.0D;
		public double y = 0.0D;
		public double z = 0.0D;
		public EnumFacing facing = EnumFacing.NORTH;

		public MutableRayTraceResult() {

		}

		public MutableRayTraceResult(double x, double y, double z, EnumFacing facing) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.facing = facing;
		}

		public MutableRayTraceResult set(double x, double y, double z, EnumFacing facing) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.facing = facing;
			return this;
		}

	}

	public static class MutableVec3d {

		public double x = 0.0D;
		public double y = 0.0D;
		public double z = 0.0D;

		public MutableVec3d() {

		}

		public MutableVec3d(MutableVec3d vec) {
			this(vec.x, vec.y, vec.z);
		}

		public MutableVec3d(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public MutableVec3d set(MutableVec3d vec) {
			return this.set(vec.x, vec.y, vec.z);
		}

		public MutableVec3d set(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public MutableVec3d add(MutableVec3d vec) {
			return this.add(vec.x, vec.y, vec.z);
		}

		public MutableVec3d add(double x, double y, double z) {
			this.x += x;
			this.y += y;
			this.z += z;
			return this;
		}

		public MutableVec3d subtract(MutableVec3d vec) {
			return this.subtract(vec.x, vec.y, vec.z);
		}

		public MutableVec3d subtract(double x, double y, double z) {
			this.x -= x;
			this.y -= y;
			this.z -= z;
			return this;
		}

		public double squareDist(MutableVec3d vec) {
			return this.squareDist(vec.x, vec.y, vec.z);
		}

		public double squareDist(double x, double y, double z) {
			double d1 = x - this.x;
			double d2 = y - this.y;
			double d3 = z - this.z;
			return d1 * d1 + d2 * d2 + d3 * d3;
		}

	}

}
