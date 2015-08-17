package com.zearon.util.interface_.test;

import com.zearon.util.interface_.IMultiInheritance;


public interface IC extends IMultiInheritance {
	
	default void IMI_init(IC thisObject) {
		IMI_initSuper(IMultiInheritance.class, thisObject);
		System.out.println("init " + IC.class.getName());
	}
	
}
