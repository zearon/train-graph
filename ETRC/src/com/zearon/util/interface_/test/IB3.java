package com.zearon.util.interface_.test;


public interface IB3 extends IB2 {
	
	default void IMI_init(IB3 thisObject) {
		IMI_initSuper(IB2.class, thisObject);
		System.out.println("init " + IB3.class.getName());
	}
	
}
