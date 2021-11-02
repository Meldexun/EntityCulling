package meldexun.entityculling.util;

public class RaytraceInfo {

	public final double x;
	public final double y;
	public final double z;
	public final boolean tested;

	public RaytraceInfo(double x, double y, double z, boolean tested) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.tested = tested;
	}

}
