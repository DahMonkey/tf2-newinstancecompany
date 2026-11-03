package rafradek.TF2weapons.util;

import java.util.function.Predicate;

/** Replacements for Guava's Predicates.and/or over java.util.function.Predicate. */
public final class PredicateUtil {
	private PredicateUtil() {
	}

	private static final Predicate<Object> ALWAYS_FALSE = input -> false;

	/** Shared instance, so it can be compared by identity like Guava's rafradek.TF2weapons.util.PredicateUtil.alwaysFalse(). */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysFalse() {
		return (Predicate<T>) ALWAYS_FALSE;
	}

	@SafeVarargs
	public static <T> Predicate<T> and(Predicate<? super T>... predicates) {
		return input -> {
			for (Predicate<? super T> p : predicates)
				if (!p.test(input))
					return false;
			return true;
		};
	}

	@SafeVarargs
	public static <T> Predicate<T> or(Predicate<? super T>... predicates) {
		return input -> {
			for (Predicate<? super T> p : predicates)
				if (p.test(input))
					return true;
			return false;
		};
	}
}
