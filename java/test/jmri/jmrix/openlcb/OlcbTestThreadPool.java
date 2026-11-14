package jmri.jmrix.openlcb;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openlcb.OlcbThreadFactory;

/**
 * The thread pool for an OlcbInterface a test makes itself, in place of the
 * one {@code new OlcbInterface(NodeID, Connection)} makes.
 * <p>
 * It is the same pool, except that when it is shut down it gives its tasks
 * up to 10 seconds to end, as the pool a CanInterface makes does.
 * {@code OlcbInterface.dispose()} gives them 200 ms and then logs "Pool did
 * not terminate", which fails the test; on a busy test machine a task can
 * still be running then.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class OlcbTestThreadPool extends ThreadPoolExecutor {

    private static final long SHUTDOWN_WAIT_MS = 10000;

    public OlcbTestThreadPool() {
        super(10, 100, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new OlcbThreadFactory());
        allowCoreThreadTimeOut(true);
    }

    /**
     * Wait for the tasks to end, for up to 10 seconds however short the time
     * asked for.
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return super.awaitTermination(Math.max(unit.toMillis(timeout), SHUTDOWN_WAIT_MS), TimeUnit.MILLISECONDS);
    }

}
