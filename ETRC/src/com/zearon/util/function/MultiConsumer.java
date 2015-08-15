package com.zearon.util.function;

@FunctionalInterface
public interface MultiConsumer<T1, T2, T3, T4> {
	void accept(T1 a1, T2 a2, T3 a3, T4 a4);
}