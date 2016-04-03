package jmri.managers;

import java.util.ArrayList;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage tasks to be completed when the program shuts down normally.
 * Specifically, allows other object to register and deregister
 * {@link ShutDownTask} objects, which are invoked in an orderly way when the
 * program is is commanded to terminate.
 * <p>
 * Operations:
 * <ol>
 * <li>Execute each {@link ShutDownTask} in order, allowing it to abort the
 * shutdown if needed.
 * <li>If not aborted, terminate the program.
 * </ol>
 * <p>
 * There can only be one instance of this operating, and it is generally
 * obtained via the instance manager.
 * <p>
 * To avoid being unable to quit the program, which annoys people, an exception
 * in a ShutDownTask is treated as permission to continue after logging.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class DefaultShutDownManager implements ShutDownManager {

    static boolean shuttingDown = false;

    public DefaultShutDownManager() {
        // This shutdown hook allows us to perform a clean shutdown when
        // running in headless mode and SIGINT (Ctrl-C) or SIGTERM. It
        // executes the shutdown tasks without calling System.exit() since
        // calling System.exit() within a shutdown hook will cause the
        // application to hang.
        // This shutdown hook also allows OS X Application->Quit to trigger our
        // shutdown tasks, since that simply calls System.exit(); 
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                DefaultShutDownManager.this.shutdown(0, false);
            }
        });
    }

    /**
     * Register a task object for later execution.
     *
     * @param s
     */
    @Override
    public void register(ShutDownTask s) {
        if (!tasks.contains(s)) {
            tasks.add(s);
        } else {
            log.error("already contains " + s);
        }
    }

    /**
     * Deregister a task object.
     *
     * @param s
     * @throws IllegalArgumentException if task object not currently registered
     */
    @Override
    public void deregister(ShutDownTask s) {
        if (tasks.contains(s)) {
            tasks.remove(s);
        } else {
            throw new IllegalArgumentException("task not registered");
        }
    }

    /**
     * Run the shutdown tasks, and then terminate the program with status 0 if
     * not aborted. Does not return under normal circumstances. Does return if
     * the shutdown was aborted by the user, in which case the program should
     * continue to operate.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DM_EXIT") // OK to directly exit standalone main
    @Override
    public Boolean shutdown() {
        return shutdown(0, true);
    }

    /**
     * Run the shutdown tasks, and then terminate the program with status 100 if
     * not aborted. Does not return under normal circumstances. Does return if
     * the shutdown was aborted by the user, in which case the program should
     * continue to operate.
     *
     * By exiting the program with status 100, the batch file (MS Windows) or
     * shell script (Linux/Mac OS X/UNIX) can catch the exit status and restart
     * the java program.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DM_EXIT") // OK to directly exit standalone main
    @Override
    public Boolean restart() {
        return shutdown(100, true);
    }

    /**
     * Run the shutdown tasks, and then terminate the program if not aborted.
     * Does not return under normal circumstances. Does return if the shutdown
     * was aborted by the user, in which case the program should continue to
     * operate.
     *
     * @param status Integer status returned on program exit
     * @param exit   True if System.exit() should be called if all tasks are executed correctly.
     * @return false if shutdown or restart failed.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DM_EXIT") // OK to directly exit standalone main
    protected Boolean shutdown(int status, boolean exit) {
        if (!shuttingDown) {
            shuttingDown = true;
            for (int i = tasks.size() - 1; i >= 0; i--) {
                try {
                    ShutDownTask t = tasks.get(i);
                    shuttingDown = t.execute(); // if a task aborts the shutdown, stop shutting down
                    if (!shuttingDown) {
                        log.info("Program termination aborted by " + t.name());
                        return false;  // abort early
                    }
                } catch (Throwable e) {
                    log.error("Error during processing of ShutDownTask " + i + ": " + e);
                }
            }

            // success
            log.info("Normal termination complete");
            // and now terminate forcefully
            if (exit) {
                System.exit(status);
            }
        }
        return false;
    }

    ArrayList<ShutDownTask> tasks = new ArrayList<>();

    private final static Logger log = LoggerFactory.getLogger(DefaultShutDownManager.class);
}
