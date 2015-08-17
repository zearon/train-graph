package com.zearon.util.interface_.test;


public interface IA3 extends IA2 {
	
	default void IMI_init(IA3 thisObject) {
		IMI_initSuper(IA2.class, thisObject);
		System.out.println("init " + IA3.class.getName());
	}
	
}
