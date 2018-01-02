package jee.thread;

import data.ConfigurationUtil;
import data.LogUtil;

public class ManagedThread {
	private Runnable runnable;
	private ThreadEventCallback cbFinished;
	private ThreadEventCallback cbCollect;
	private Thread thread;
	private boolean inheritUtils;

	protected ManagedThread(Runnable runnable) {
		this(runnable, false);
	}

	protected ManagedThread(Runnable runnable, boolean inheritUtils) {
		this.runnable = runnable;
		this.inheritUtils = inheritUtils;
	}

	public void registerFinishedCallback(ThreadEventCallback callback) {
		this.cbFinished = callback;
	}

	public void registerCollectCallback(ThreadEventCallback callback) {
		this.cbCollect = callback;
	}

	public void start() {
		ConfigurationUtil cfg = ConfigurationUtil.getInstance();
		LogUtil log = LogUtil.getInstance();
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (inheritUtils) {
						ConfigurationUtil.getInstance().clone(cfg);
						LogUtil.getInstance().clone(log);
					}
					// wrapper ! this is not the start() v.s. run() noob error.
					runnable.run();
				} finally {
					if (null != cbFinished)
						cbFinished.fire(ManagedThread.this);
					if (null != cbCollect)
						cbCollect.fire(ManagedThread.this);
				}
			}

		});
		thread.setDaemon(true);
		thread.start();

	}

	public void kill() {
		thread.interrupt();
	}
}
