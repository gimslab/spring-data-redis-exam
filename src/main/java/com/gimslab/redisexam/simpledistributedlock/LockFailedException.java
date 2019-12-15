package com.gimslab.redisexam.simpledistributedlock;

public class LockFailedException extends Throwable {

	public LockFailedException() {
	}

	public LockFailedException(Throwable cause) {
		super(cause);
	}

	public LockFailedException(String msg) {
		super(msg);
	}
}
