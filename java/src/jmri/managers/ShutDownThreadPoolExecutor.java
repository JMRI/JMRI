package jmri.managers;

import java.util.concurrent.*;

/**
 * @author Steve Young Copyright (C) 2024
 */
public class ShutDownThreadPoolExecutor extends ThreadPoolExecutor {

    public ShutDownThreadPoolExecutor(int poolSize, String threadName) {
        super(poolSize, poolSize, 10L, TimeUnit.SECONDS,
              new LinkedBlockingQueue<>(), new ShutDownThreadFactory(threadName));

        // Set a custom RejectedExecutionHandler to handle tasks that are rejected.
        this.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor executor) ->
            log.error("Task was rejected: {}", r));
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone() && !future.isCancelled()) {
                    future.get(); // this will throw any exceptions encountered during execution
                }
            } catch (CancellationException ce) {
                log.error("Task was cancelled: {}", r );
            } catch (ExecutionException ee) {
                log.error("Exception in task: {}", r, ee.getCause());
            } catch (InterruptedException ie) {
                // Restore interrupted state
                Thread.currentThread().interrupt();
            }
        } else if (t != null) {
            // Log the exception that occurred during execution
            log.error("Exception in task execution: {}", r, t);
        }

    }

    private static class ShutDownThreadFactory implements ThreadFactory {

        private final String threadName;

        private ShutDownThreadFactory( String threadName ){
            super();
            this.threadName = threadName;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, threadName);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShutDownThreadPoolExecutor.class);

}
