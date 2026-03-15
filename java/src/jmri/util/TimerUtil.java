package jmri.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nonnull;


/**
 * Common utility methods for working with (@link java.util.Timer)
 * <p>
 * Each {@link java.util.Timer} uses a thread, which means that they're
 * not throw-away timers:  You either track when you can destroy them
 * (and that destruction is not obvious), or they stick around consuming
 * resources.
 * <p>
 * This class provides most of the functionality of a Timer.
 * Some differences:
 * <ul>
 * <li>When migrating code that uses Timer.cancel() to end operation, you have to
 *     retain references to the individual TimerTask objects and cancel them instead.
 * </ul>
 * <p>
 * For convenience, this also provides methods to ensure that the task is invoked
 * on a specific JMRI thread.
 * <p>
 * Please note the comment in the {@link Timer} Javadoc about how
 * {@link java.util.concurrent.ScheduledThreadPoolExecutor} might provide a better
 * underlying implementation.
 * Method JavaDoc tweaked from java.util.Timer.
 * @author Bob Jacobsen Copyright 2018
 */
@javax.annotation.concurrent.Immutable
public final class TimerUtil {

    // class only supplies static methods
    private TimerUtil() {}

    // Timer implementation methods

    /**
     * Schedule a TimerTask for execution at the specified time.
     * If time is in the past, the task is scheduled for immediate execution.
     * @param task task to be scheduled.
     * @param time time at which task is to be executed.
     */
    public static void schedule(@Nonnull TimerTask task, @Nonnull Date time) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(task, time);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void schedule(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(task, firstTime, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for execution after the specified delay.
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.
     */
    public static void schedule(@Nonnull TimerTask task, long delay) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(task, delay);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void schedule(@Nonnull TimerTask task, long delay, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(task, delay, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleAtFixedRate(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(task, firstTime, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleAtFixedRate(@Nonnull TimerTask task, long delay, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(task, delay, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }


    // GUI-thread implementation methods

    // arrange to run on GUI thread
    private static TimerTask gtask(TimerTask task) {
        return new TimerTask(){
                @Override
                public void run() {
                    ThreadingUtil.runOnGUIEventually(() -> {task.run();});
                }
        };
    }

    /**
     * Schedule a TimerTask on GUI Thread for execution at the specified time.
     * If time is in the past, the task is scheduled for immediate execution.
     * @param task task to be scheduled.
     * @param time time at which task is to be executed.
     */
    public static void scheduleOnGUIThread(@Nonnull TimerTask task, @Nonnull Date time) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(gtask(task), time);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the GUI Thread, beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleOnGUIThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(gtask(task), firstTime, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for execution on the GUI Thread
     * after the specified delay.
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.
     */
    public static void scheduleOnGUIThread(@Nonnull TimerTask task, long delay) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(gtask(task), delay);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the GUI Thread, beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleOnGUIThread(@Nonnull TimerTask task, long delay, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(gtask(task), delay, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * on the GUI Thread, beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleAtFixedRateOnGUIThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(gtask(task), firstTime, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the GUI Thread beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleAtFixedRateOnGUIThread(@Nonnull TimerTask task, long delay, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(gtask(task), delay, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }


    // arrange to run on layout thread
    private static TimerTask ltask(TimerTask task) {
        return new TimerTask(){
                @Override
                public void run() {
                    ThreadingUtil.runOnLayoutEventually(() -> {task.run();});
                }
        };
    }

    /**
     * Schedule a TimerTask on Layout Thread for execution at the specified time.
     * If time is in the past, the task is scheduled for immediate execution.
     * @param task task to be scheduled.
     * @param time time at which task is to be executed.
     */
    public static void scheduleOnLayoutThread(@Nonnull TimerTask task, @Nonnull Date time) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(ltask(task), time);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the Layout Thread, beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleOnLayoutThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(ltask(task), firstTime, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for execution on the Layout Thread
     * after the specified delay.
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.
     */
    public static void scheduleOnLayoutThread(@Nonnull TimerTask task, long delay) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(ltask(task), delay);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the Layout Thread beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleOnLayoutThread(@Nonnull TimerTask task, long delay, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(ltask(task), delay, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * on the Layout Thread, beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleAtFixedRateOnLayoutThread(
        @Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(ltask(task), firstTime, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the Layout Thread beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     */
    public static void scheduleAtFixedRateOnLayoutThread(@Nonnull TimerTask task, long delay, long period) {
        synchronized (commonTimer) {
            try {
                commonTimer.schedule(ltask(task), delay, period);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }


    private static final Timer commonTimer = new Timer("JMRI Common Timer", true);

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimerUtil.class);
}
