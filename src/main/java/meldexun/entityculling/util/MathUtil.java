package meldexun.entityculling.util;

public class MathUtil {

	public static double lerp(double min, double max, double x) {
		return min + (max - min) * x;
	}

	public static int signum(double x) {
		if (x > 0.0D) {
			return 1;
		}
		if (x < 0.0D) {
			return -1;
		}
		return 0;
	}

	public static double frac(double number) {
		return number - floor(number);
	}

	public static int floor(double value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	public static int ceil(double value) {
		int i = (int) value;
		return value > i ? i + 1 : i;
	}

	public static int round(double value) {
		int i = (int) value;
		if (value > i) {
			return (int) (value + 0.5D);
		}
		if (value < i) {
			return (int) (value - 0.5D);
		}
		return i;
	}

	public static double dist(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(distSqr(x1, y1, z1, x2, y2, z2));
	}

	public static double distSqr(double x1, double y1, double z1, double x2, double y2, double z2) {
		x2 -= x1;
		y2 -= y1;
		z2 -= z1;
		return x2 * x2 + y2 * y2 + z2 * z2;
	}

	public static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt(distSqr(x1, y1, x2, y2));
	}

	public static double distSqr(double x1, double y1, double x2, double y2) {
		x2 -= x1;
		y2 -= y1;
		return x2 * x2 + y2 * y2;
	}

	public static double square(double d) {
		return d * d;
	}

}
