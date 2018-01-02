package jee.thread;

public interface ThreadEventCallback {
	void fire(ManagedThread finishedThread);
}
