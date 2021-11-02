package meldexun.entityculling.util;

public class MathUtil {

	public static double lerp(double min, double max, double x) {
		return min + (max - min) * x;
	}

}
