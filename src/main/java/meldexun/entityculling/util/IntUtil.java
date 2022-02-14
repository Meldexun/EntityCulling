package meldexun.entityculling.util;

public class IntUtil {

	@FunctionalInterface
	public interface BiIntPredicate {

		boolean test(int x, int y);

	}

	public static boolean anyMatch(int x1, int x2, int y1, int y2, BiIntPredicate predicate) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if (predicate.test(x, y)) {
					return true;
				}
			}
		}
		return false;
	}

	@FunctionalInterface
	public interface BiIntConsumer {

		void accept(int x, int y);

	}

	public static void forEach(int x1, int x2, int y1, int y2, BiIntConsumer consumer) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				consumer.accept(x, y);
			}
		}
	}

}
