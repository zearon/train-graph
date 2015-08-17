package com.zearon.util.interface_.test;


public interface IB2 extends IB {
	
	default void IMI_init(IB2 thisObject) {
		IMI_initSuper(IB.class, thisObject);
		System.out.println("init " + IB2.class.getName());
	}
	
}
