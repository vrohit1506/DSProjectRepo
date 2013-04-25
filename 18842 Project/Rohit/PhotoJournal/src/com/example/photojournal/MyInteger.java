package com.example.photojournal;

public class MyInteger {
	private static Integer instance = null;
	protected MyInteger() {

	}
	public static Integer getInstance() {
		if(instance == null) {
			instance = new Integer(0);
		}
		return instance;
	}
	public static Integer getIncrInstance() {
		if(instance == null) {
			instance = new Integer(0);
		}
		return instance + 1;
	}
}