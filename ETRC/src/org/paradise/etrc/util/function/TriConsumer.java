package org.paradise.etrc.util.function;

@FunctionalInterface
public interface TriConsumer<T1, T2, T3> {
	void accept(T1 a1, T2 a2, T3 a3);
}