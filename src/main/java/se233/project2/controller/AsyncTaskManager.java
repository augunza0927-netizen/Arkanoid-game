package se233.project2.controller;

import javafx.concurrent.Task;
import se233.project2.exception.GlobalExceptionHandler;
import se233.project2.util.GameLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages background worker threads and JavaFX {@link Task} execution for asynchronous
 * asset preloading and concurrent timer tasks (satisfying SE233 multithreading skills).
 */
public class AsyncTaskManager {

    private static final AtomicInteger THREAD_SEQ = new AtomicInteger(1);

    private final ExecutorService workerPool;
    private final ScheduledExecutorService timerScheduler;

    public AsyncTaskManager() {
        this.workerPool = Executors.newFixedThreadPool(2, runnable -> {
            Thread t = new Thread(runnable, "arkanoid-worker-" + THREAD_SEQ.getAndIncrement());
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(new GlobalExceptionHandler());
            return t;
        });

        this.timerScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "arkanoid-timer-" + THREAD_SEQ.getAndIncrement());
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(new GlobalExceptionHandler());
            return t;
        });
    }

    public <T> void submitTask(Task<T> javafxTask) {
        if (javafxTask == null || workerPool.isShutdown()) {
            return;
        }
        javafxTask.setOnFailed(event -> {
            Throwable ex = javafxTask.getException();
            GlobalExceptionHandler.handle(ex);
        });
        workerPool.submit(javafxTask);
    }

    public ScheduledFuture<?> scheduleDelayed(Runnable command, long delayMillis) {
        if (command == null || timerScheduler.isShutdown()) {
            return null;
        }
        return timerScheduler.schedule(() -> {
            try {
                command.run();
            } catch (Throwable t) {
                GlobalExceptionHandler.handle(t);
            }
        }, Math.max(0L, delayMillis), TimeUnit.MILLISECONDS);
    }

    public void shutdownAll() {
        GameLogger.logGameEvent("Shutting down AsyncTaskManager background thread pools");
        workerPool.shutdownNow();
        timerScheduler.shutdownNow();
    }
}
