package meldexun.entityculling.util;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class MutableAABB {

	private double minX;
	private double minY;
	private double minZ;
	private double maxX;
	private double maxY;
	private double maxZ;

	public MutableAABB() {
		this(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	public MutableAABB(MutableAABB aabb) {
		this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	public MutableAABB(AxisAlignedBB aabb) {
		this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	public MutableAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.set(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public AxisAlignedBB toVanillaAABB() {
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public MutableAABB set(MutableAABB aabb) {
		return set(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	public MutableAABB set(AxisAlignedBB aabb) {
		return set(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	public MutableAABB set(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		return this;
	}

	public MutableAABB grow(double d) {
		return grow(d, d, d);
	}

	public MutableAABB grow(Vec3d vec) {
		return grow(vec.x, vec.y, vec.z);
	}

	public MutableAABB grow(double x, double y, double z) {
		this.minX -= x;
		this.minY -= y;
		this.minZ -= z;
		this.maxX += x;
		this.maxY += y;
		this.maxZ += z;
		return this;
	}

	public MutableAABB offset(double x, double y, double z) {
		this.minX += x;
		this.minY += y;
		this.minZ += z;
		this.maxX += x;
		this.maxY += y;
		this.maxZ += z;
		return this;
	}

	public MutableAABB expand(Vec3d vec) {
		return expand(vec.x, vec.y, vec.z);
	}

	public MutableAABB expand(Vec3d vec, double d) {
		return expand(vec.x, vec.y, vec.z, d);
	}

	public MutableAABB expand(double x, double y, double z, double d) {
		return expand(x * d, y * d, z * d);
	}

	public MutableAABB expand(double x, double y, double z) {
		if (x < 0.0D) {
			this.minX += x;
		} else {
			this.maxX += x;
		}
		if (y < 0.0D) {
			this.minY += y;
		} else {
			this.maxY += y;
		}
		if (z < 0.0D) {
			this.minZ += z;
		} else {
			this.maxZ += z;
		}
		return this;
	}

	public boolean isVisible(ICamera frustum) {
		if (frustum instanceof Frustum) {
			return ((Frustum) frustum).isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
		}
		return frustum.isBoundingBoxInFrustum(this.toVanillaAABB());
	}

	public double sizeX() {
		return maxX - minX;
	}

	public double sizeY() {
		return maxY - minY;
	}

	public double sizeZ() {
		return maxZ - minZ;
	}

	public double minX() {
		return minX;
	}

	public double minY() {
		return minY;
	}

	public double minZ() {
		return minZ;
	}

	public double maxX() {
		return maxX;
	}

	public double maxY() {
		return maxY;
	}

	public double maxZ() {
		return maxZ;
	}

}
