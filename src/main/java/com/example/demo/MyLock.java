package com.example.demo;

public class MyLock implements AutoCloseable {

	private int workerId;
	private int key;

	MyLock(int workerId, int key) {
		this.workerId = workerId;
		this.key = key;
	}

	@Override
	public void close() {
		System.out.println(workerId + ":" + key + " lock_close");
	}

	@Override
	public String toString() {
		return "Lock(worker:" + workerId + ", key:" + key + ")";
	}
}
