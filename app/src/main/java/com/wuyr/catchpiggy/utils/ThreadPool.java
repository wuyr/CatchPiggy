package com.wuyr.catchpiggy.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by wuyr on 17-11-30 下午8:19.
 */

public class ThreadPool {

    private volatile static ThreadPool mInstance;
    private ExecutorService mThreadPool;

    private ThreadPool() {
        mThreadPool = Executors.newCachedThreadPool();
    }

    public static ThreadPool getInstance() {
        if (mInstance == null) {
            init();
        }
        return mInstance;
    }

    private static void init() {
        synchronized (ThreadPool.class) {
            if (mInstance == null) {
                mInstance = new ThreadPool();
            }
        }
    }

    public Future<?> execute(Runnable command) {
        return mThreadPool.submit(command);
    }

    public static void shutdown() {
        if (mInstance != null) {
            mInstance.mThreadPool.shutdownNow();
            mInstance = null;
        }
    }

    @Override
    public String toString() {
        return mThreadPool.toString();
    }
}
