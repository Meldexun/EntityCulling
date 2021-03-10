package meldexun.entityculling;

import meldexun.entityculling.integration.CubicChunks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.Loader;

@SuppressWarnings("unused")
public final class RayTracingEngine {

	private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();

	private static int cachedChunkX = 0;
	private static int cachedChunkY = 0;
	private static int cachedChunkZ = 0;
	private static ExtendedBlockStorage cachedChunk = null;
	private static boolean isChunkCached = false;

	private RayTracingEngine() {

	}

	private static boolean isOpaqueBlock(World world, int x, int y, int z) {
		if (world.isOutsideBuildHeight(MUTABLE_POS.setPos(x, y, z))) {
			return false;
		}
		if (!isChunkCached || x >> 4 != cachedChunkX || y >> 4 != cachedChunkY || z >> 4 != cachedChunkZ) {
			cachedChunkX = x >> 4;
			cachedChunkY = y >> 4;
			cachedChunkZ = z >> 4;
			if (!Loader.isModLoaded("cubicchunks")) {
				cachedChunk = world.getChunkProvider().provideChunk(cachedChunkX, cachedChunkZ).getBlockStorageArray()[cachedChunkY];
			} else {
				cachedChunk = CubicChunks.getBlockStorage(world, cachedChunkX, cachedChunkY, cachedChunkZ);
			}
			isChunkCached = true;
		}
		if (cachedChunk == null) {
			return false;
		}
		IBlockState state = cachedChunk.get(x & 15, y & 15, z & 15);
		return state.isOpaqueCube();
	}

	public static void resetCache() {
		cachedChunkX = 0;
		cachedChunkY = 0;
		cachedChunkZ = 0;
		cachedChunk = null;
		isChunkCached = false;
	}

	public static MutableRayTraceResult rayTraceBlocks(World world, double startX, double startY, double startZ, double endX, double endY, double endZ, boolean ignoreStart, double maxIgnore, MutableRayTraceResult returnValue) {
		// double x1 = lerp(-1.0E-7D, startX, endX);
		// double y1 = lerp(-1.0E-7D, startY, endY);
		// double z1 = lerp(-1.0E-7D, startZ, endZ);
		// double x2 = lerp(-1.0E-7D, endX, startX);
		// double y2 = lerp(-1.0E-7D, endY, startY);
		// double z2 = lerp(-1.0E-7D, endZ, startZ);
		double x1 = startX;
		double y1 = startY;
		double z1 = startZ;
		double x2 = endX;
		double y2 = endY;
		double z2 = endZ;
		double dirX = x2 - x1;
		double dirY = y2 - y1;
		double dirZ = z2 - z1;

		if (maxIgnore <= 0.0D) {
			return returnValue.set(x1, y1, z1, EnumFacing.getFacingFromVector((float) dirX, (float) dirY, (float) dirZ).getOpposite());
		}

		if (dirX * dirX + dirY * dirY + dirZ * dirZ < maxIgnore * maxIgnore) {
			return null;
		}

		int incX = signum(dirX);
		int incY = signum(dirY);
		int incZ = signum(dirZ);
		double dx = incX == 0 ? Double.MAX_VALUE : (double) incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : (double) incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : (double) incZ / dirZ;
		double percentX = dx * (incX > 0 ? 1.0D - frac(x1) : frac(x1));
		double percentY = dy * (incY > 0 ? 1.0D - frac(y1) : frac(y1));
		double percentZ = dz * (incZ > 0 ? 1.0D - frac(z1) : frac(z1));
		EnumFacing facingX = incX > 0 ? EnumFacing.WEST : EnumFacing.EAST;
		EnumFacing facingY = incY > 0 ? EnumFacing.DOWN : EnumFacing.UP;
		EnumFacing facingZ = incZ > 0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
		int x = floor(x1);
		int y = floor(y1);
		int z = floor(z1);
		EnumFacing facing;

		boolean hasHitBlock = false;
		boolean hasHitBlockPreviously = false;
		double firstHitX = x1;
		double firstHitY = y1;
		double firstHitZ = z1;
		EnumFacing firstHitFacing = EnumFacing.NORTH;
		double lastHitX = x1;
		double lastHitY = y1;
		double lastHitZ = z1;

		if (!ignoreStart && isOpaqueBlock(world, x, y, z)) {
			hasHitBlock = true;
			hasHitBlockPreviously = true;
			firstHitFacing = EnumFacing.getFacingFromVector((float) dirX, (float) dirY, (float) dirZ).getOpposite();
		}

		while (percentX <= 1.0D || percentY <= 1.0D || percentZ <= 1.0D) {
			if (percentX < percentY) {
				if (percentX < percentZ) {
					x += incX;
					percentX += dx;
					facing = facingX;
				} else {
					z += incZ;
					percentZ += dz;
					facing = facingZ;
				}
			} else if (percentY < percentZ) {
				y += incY;
				percentY += dy;
				facing = facingY;
			} else {
				z += incZ;
				percentZ += dz;
				facing = facingZ;
			}

			boolean hitOpaqueBlock = isOpaqueBlock(world, x, y, z);
			if (hasHitBlockPreviously || hitOpaqueBlock) {
				double d;
				if (facing.getAxis() == Axis.X) {
					d = percentX - dx;
				} else if (facing.getAxis() == Axis.Y) {
					d = percentY - dy;
				} else {
					d = percentZ - dz;
				}
				double d1 = x1 + dirX * d;
				double d2 = y1 + dirY * d;
				double d3 = z1 + dirZ * d;

				if (!hasHitBlock) {
					if (hitOpaqueBlock) {
						hasHitBlock = true;
						hasHitBlockPreviously = true;
						firstHitFacing = facing;
						firstHitX = d1;
						firstHitY = d2;
						firstHitZ = d3;
						lastHitX = d1;
						lastHitY = d2;
						lastHitZ = d3;
					}
				} else {
					if (hasHitBlockPreviously) {
						maxIgnore -= Math.sqrt(squareDist(lastHitX, lastHitY, lastHitZ, d1, d2, d3));
						if (maxIgnore <= 0.0D) {
							return returnValue.set(firstHitX, firstHitY, firstHitZ, firstHitFacing);
						}
					}
					if (hitOpaqueBlock) {
						lastHitX = x1 + dirX * d;
						lastHitY = y1 + dirY * d;
						lastHitZ = z1 + dirZ * d;
						hasHitBlockPreviously = true;
					} else {
						hasHitBlockPreviously = false;
					}
				}
			}
		}

		return null;
	}

	private static double lerp(double pct, double start, double end) {
		return start + pct * (end - start);
	}

	private static int floor(double value) {
		int i = (int) value;
		return value < (double) i ? i - 1 : i;
	}

	private static int signum(double x) {
		if (x == 0.0D) {
			return 0;
		} else {
			return x > 0.0D ? 1 : -1;
		}
	}

	private static double frac(double number) {
		return number - (double) lfloor(number);
	}

	private static long lfloor(double value) {
		long i = (long) value;
		return value < (double) i ? i - 1L : i;
	}

	private static double squareDist(double x1, double y1, double z1, double x2, double y2, double z2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1);
	}

	public static class MutableRayTraceResult {

		public double x;
		public double y;
		public double z;
		public EnumFacing facing;

		public MutableRayTraceResult() {
			this.x = 0.0D;
			this.y = 0.0D;
			this.z = 0.0D;
			this.facing = EnumFacing.NORTH;
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

		public double squareDist(MutableRayTraceResult other) {
			return this.squareDist(other.x, other.y, other.z);
		}

		public double squareDist(double x, double y, double z) {
			double d1 = x - this.x;
			double d2 = y - this.y;
			double d3 = z - this.z;
			return d1 * d1 + d2 * d2 + d3 * d3;
		}

	}

}
