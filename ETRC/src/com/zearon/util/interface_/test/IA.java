package com.zearon.util.interface_.test;

import com.zearon.util.interface_.IMultiInheritance;


public interface IA extends IMultiInheritance {
	
	default void IMI_init(IA thisObject) {
		IMI_initSuper(IMultiInheritance.class, thisObject);
		
		System.out.println("init " + IA.class.getName());
		IMI_setProperty(IMultiInheritance.class, "anInteger", 3);
	}
	
}
