package com.zearon.util.interface_.test;

import com.zearon.util.interface_.IMultiInheritance;


public interface IB extends IMultiInheritance {
	
	default void IMI_init(IB thisObject) {
		IMI_initSuper(IMultiInheritance.class, thisObject);
		System.out.println("init " + IB.class.getName());
	}
	
}
