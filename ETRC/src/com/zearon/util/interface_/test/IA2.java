package com.zearon.util.interface_.test;


public interface IA2 extends IA {
	
	default void IMI_init(IA2 thisObject) {
		IMI_initSuper(IA.class, thisObject);
		System.out.println("init " + IA2.class.getName());
	}
	
}
