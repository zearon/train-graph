package com.zearon.util.interface_.test;

import com.zearon.util.interface_.IMultiInheritance;

public class A implements IA4, IB3, IA3B2, IC {
	{
		IMI_initInterface();
	}
	
	@Override
	public IMultiInheritance IMI_getThisObject() {
		return this;
	}

	public void sayHello() {
		System.out.println("Say hello from " + A.class.getName());
	}
	
	public static void sayHi() {
		System.out.println("Say hi from " + A.class.getName());
	}
}
