package jmri.util;

import java.util.ArrayList;
import java.util.List;
import jmri.ShutDownManager;
import jmri.ShutDownTask;

/**
 * Mock ShutDownManager for unit testing.
 * <p>
 * To test that a method behaves differently when an application is shutting
 * down or not, call {@link #shutdown()} or {@link #restart()} before invoking
 * the method to test. To clear the state of shutting down, call
 * {@link #resetShuttingDown()}.</p>
 * <p>
 * The list of registered tasts is exposed via {@link #shutDownTasks()} to allow
 * verification that a method that registers or deregisters a ShutDownTask as a
 * side effect did so correctly.</p>
 *
 * @author Randall Wood
 */
public class MockShutDownManager implements ShutDownManager {

    private boolean isShuttingDown = false;
    private final ArrayList<ShutDownTask> tasks = new ArrayList<>();

    public MockShutDownManager() {
    }

    @Override
    public void register(ShutDownTask task) {
        this.tasks.add(task);
    }

    @Override
    public void deregister(ShutDownTask task) {
        this.tasks.remove(task);
    }

    @Override
    public boolean restart() {
        this.isShuttingDown = true;
        return true;
    }

    @Override
    public boolean shutdown() {
        this.isShuttingDown = true;
        return true;
    }

    @Override
    public boolean isShuttingDown() {
        return this.isShuttingDown;
    }

    public void resetShuttingDown() {
        this.isShuttingDown = false;
    }

    public List<ShutDownTask> shutDownTasks() {
        return new ArrayList<>(this.tasks);
    }
}
