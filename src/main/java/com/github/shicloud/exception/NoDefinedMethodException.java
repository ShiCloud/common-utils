package com.github.shicloud.exception;

public class NoDefinedMethodException extends Exception {
	private static final long serialVersionUID = 1L;
	public NoDefinedMethodException(String name){
		super(" No Defined Method " + name);
	}
}
