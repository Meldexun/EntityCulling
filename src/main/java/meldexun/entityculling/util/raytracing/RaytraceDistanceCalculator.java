package meldexun.entityculling.util.raytracing;

import meldexun.entityculling.util.MathUtil;

public enum RaytraceDistanceCalculator {

	SPHERICAL {
		@Override
		public double distSqr(double x0, double y0, double z0, double x1, double y1, double z1) {
			return MathUtil.distSqr(x0, y0, z0, x1, y1, z1);
		}
	},
	CYLINDRICAL {
		@Override
		public double distSqr(double x0, double y0, double z0, double x1, double y1, double z1) {
			return Math.max(MathUtil.distSqr(x0, z0, x1, z1), MathUtil.square(y1 - y0));
		}
	};

	public abstract double distSqr(double x0, double y0, double z0, double x1, double y1, double z1);

}
