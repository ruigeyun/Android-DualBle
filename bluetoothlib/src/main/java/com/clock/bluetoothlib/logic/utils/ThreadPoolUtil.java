/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadPoolUtil {
	
	private ExecutorService mExecutorService;
	private static ThreadPoolUtil instance = new ThreadPoolUtil();
	
	private ThreadPoolUtil() {
		// 线程池为无限大，当执行第二个任务时第一个任务已经完成，会复用执行第一个任务的线程，而不用每次新建线程。
		mExecutorService = Executors.newCachedThreadPool();
//		// 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
//		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
//		// 创建一个定长线程池，支持定时及周期性任务执行
//		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
//		// 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行
//		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	}
	
	public static ThreadPoolUtil get() {
		return instance;
	}
	
	public ExecutorService getThread() {
		return mExecutorService;
	}
	
}
