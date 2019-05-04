package main;

import java.util.ArrayList;

public class test3 {

	public static void main(String[] args) {

		ArrayList<A> list = new ArrayList<A>();

		A a2 = new A();

		list.add(new A());
		list.get(0).c.set("first");


		System.out.println(a2.c.str);

		a2.copy(list.get(0).c);

		System.out.println(a2.c.str);

		list.clear();

		System.out.println(a2.c.str);

	}



}


class A{

	public A() {
		this.c = new B();
	}

	int a;
	int[] b;
	B c;

	public void copy(B b) {
		this.c = b;
	}
}

class B{
	String str = "default";

	public void set(String str) {
		this.str = str;
	}
}