package com.github.shicloud.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class SerialNumUtil {
	
	private AtomicInteger messageIdCounter;

	public SerialNumUtil(int startNum) {
		messageIdCounter = new AtomicInteger(startNum);
	}
	
	public int next() {
		int messageId = messageIdCounter.getAndIncrement();
		return Math.abs(messageId % 0xFFFF);
	}
}
