package jee.thread;

import java.util.List;

import data.EnumUtil;
import data.LogUtil;
import data.Util;

public class ThreadUtil implements Util {
	public enum Option {
		INHERIT_UTILS, WAIT;
	}

	/*
	 * TODO JEE7 private static final String THREAD_CONTEXT =
	 * "java:comp/env/concurrent"; private ManagedThreadFactory threadFactory;
	 */
	private boolean useManagedThreadFactory = false;

	public static ThreadUtil getInstance() {
		return new ThreadUtil();
	}

	public ManagedThread createManagedThread(String factory, Runnable runnable, Option... options) {
		ManagedThread result;
		if (useManagedThreadFactory) {
			/*
			 * TODO JEE7 result = getThreadFactory(factory).newThread(runnable);
			 */
			result = new ManagedThread(runnable, EnumUtil.getInstance().contains(options, Option.INHERIT_UTILS));
		} else
			result = new ManagedThread(runnable, EnumUtil.getInstance().contains(options, Option.INHERIT_UTILS));

		return result;

	}
	/*
	 * TODO JEE7 private ManagedThreadFactory getThreadFactory(String factory) {
	 * if (null == threadFactory) { try { InitialContext ctx = new
	 * InitialContext(); threadFactory = (ManagedThreadFactory)
	 * ctx.lookup(THREAD_CONTEXT + "/" + factory); } catch (NamingException e) {
	 * LogUtil.getInstance().error("problem getting threadfactory from [" +
	 * factory + "]"); } } return threadFactory; }
	 */

	public void sleep(int timeout) throws InterruptedException {
		Thread.sleep(timeout);
	}

	public void fork(List<ManagedThread> threads, Option... options) {
		class Locals {
			int todo = threads.size();
		}
		Locals _locals = new Locals();

		// register collect callback, and start the thread
		for (ManagedThread thread : threads) {
			thread.registerCollectCallback((thr) -> {
				--_locals.todo;
			});
			thread.start();
		}
		if (EnumUtil.getInstance().contains(options, Option.WAIT)) {
			// wait for the threads to complete
			while (_locals.todo > 0) {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					LogUtil.getInstance().info("interrupted forkJoin() join wait operation", e);
				}
			}
		}
	}
}
