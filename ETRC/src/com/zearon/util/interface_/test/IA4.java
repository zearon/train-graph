package com.zearon.util.interface_.test;


public interface IA4 extends IA3 {
	
	default void IMI_init(IA4 thisObject) {
		IMI_initSuper(IA3.class, thisObject);
		System.out.println("init " + IA4.class.getName());
	}
	
}
