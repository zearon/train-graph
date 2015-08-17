package com.zearon.util.interface_.test;

public class Test1 {
	public void greet() {
		A a = new A();
		a.sayHello();
		
		A.sayHi();
	}
	
	public static void main(String... args) {
		Test1 test1 = new Test1();
		test1.greet();
	}
}
