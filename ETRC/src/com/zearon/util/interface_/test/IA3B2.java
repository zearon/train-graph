package com.zearon.util.interface_.test;


public interface IA3B2 extends IA3, IB2 {
	
	default void IMI_init(IA3B2 thisObject) {
		IMI_initSuper(IA3.class, thisObject);
		IMI_initSuper(IB2.class, thisObject);
		System.out.println("init " + IA3B2.class.getName());
	}
	
}
