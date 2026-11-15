package jmri.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;


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
        onTimer(t -> t.schedule(task, time));
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
        onTimer(t -> t.schedule(task, firstTime, period));
    }

    /**
     * Schedules the specified task for execution after the specified delay.
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.
     */
    public static void schedule(@Nonnull TimerTask task, long delay) {
        onTimer(t -> t.schedule(task, delay));
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
        onTimer(t -> t.schedule(task, delay, period));
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
        onTimer(t -> t.schedule(task, firstTime, period));
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
        onTimer(t -> t.schedule(task, delay, period));
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
     * @return Actual scheduled task; use this if you need to cancel
     */
    public static @Nonnull TimerTask scheduleOnGUIThread(@Nonnull TimerTask task, @Nonnull Date time) {
        var gtask = gtask(task);
        onTimer(t -> t.schedule(gtask, time));
        return gtask;
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the GUI Thread, beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @return Actual scheduled task; use this if you need to cancel
     */
    public static @Nonnull TimerTask scheduleOnGUIThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        var gtask = gtask(task);
        onTimer(t -> t.schedule(gtask, firstTime, period));
        return gtask;
    }

    /**
     * Schedules the specified task for execution on the GUI Thread
     * after the specified delay.
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.
     * @return Actual scheduled task; use this if you need to cancel
     */
    public static @Nonnull TimerTask scheduleOnGUIThread(@Nonnull TimerTask task, long delay) {
        var gtask = gtask(task);
        onTimer(t -> t.schedule(gtask, delay));
        return gtask;
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the GUI Thread, beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @return Actual scheduled task; use this if you need to cancel
     */
    public static @Nonnull TimerTask scheduleOnGUIThread(@Nonnull TimerTask task, long delay, long period) {
        var gtask = gtask(task);
        onTimer(t -> t.schedule(gtask, delay, period));
        return gtask;
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * on the GUI Thread, beginning at the specified time.
     * Subsequent executions take place at approximately regular intervals,
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @return Actual scheduled task; use this if you need to cancel
     */
    public static @Nonnull TimerTask scheduleAtFixedRateOnGUIThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        var gtask = gtask(task);
        onTimer(t -> t.schedule(gtask, firstTime, period));
        return gtask;
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>
     * on the GUI Thread beginning after the specified delay.
     * Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @return Actual scheduled task; use this if you need to cancel
     */
    public static @Nonnull TimerTask scheduleAtFixedRateOnGUIThread(@Nonnull TimerTask task, long delay, long period) {
        var gtask = gtask(task);
        onTimer(t -> t.schedule(gtask, delay, period));
        return gtask;
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
        onTimer(t -> t.schedule(ltask(task), time));
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
        onTimer(t -> t.schedule(ltask(task), firstTime, period));
    }

    /**
     * Schedules the specified task for execution on the Layout Thread
     * after the specified delay.
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.
     */
    public static void scheduleOnLayoutThread(@Nonnull TimerTask task, long delay) {
        onTimer(t -> t.schedule(ltask(task), delay));
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
        onTimer(t -> t.schedule(ltask(task), delay, period));
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
        onTimer(t -> t.schedule(ltask(task), firstTime, period));
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
        onTimer(t -> t.schedule(ltask(task), delay, period));
    }

    /**
     * Run one scheduling call on the common timer.
     * <p>
     * A {@link Timer} runs its tasks on one thread, and an exception thrown
     * by any task ends that thread and cancels the Timer: every later
     * scheduling call on it throws {@link IllegalStateException}. If that has
     * happened, a new common timer is started, so that a fault in one task
     * does not stop every later delayed action in JMRI. Tasks that were
     * waiting on the stopped timer when it stopped are lost; that is logged.
     * An {@link IllegalStateException} for another reason, such as a task
     * scheduled twice, is the caller's mistake and is logged as before.
     *
     * @param call the scheduling call, given the timer to use
     */
    private static void onTimer(@Nonnull Consumer<Timer> call) {
        synchronized (timerLock) {
            try {
                call.accept(commonTimer);
                return;
            } catch (IllegalStateException e) {
                if (isRunning(commonTimer)) {
                    log.warn("During schedule()", e);
                    return;
                }
            }
            log.error("JMRI Common Timer stopped after an exception in one of its tasks (logged when it happened);"
                    + " starting a new one. Tasks that were waiting on it have been lost.");
            commonTimer = newCommonTimer();
            try {
                call.accept(commonTimer);
            } catch (IllegalStateException e) {
                log.warn("During schedule()", e);
            }
        }
    }

    /**
     * @param timer a Timer
     * @return false if the Timer has been cancelled, as it is when one of
     *         its tasks throws, so that it can't schedule anything
     */
    private static boolean isRunning(@Nonnull Timer timer) {
        TimerTask probe = new TimerTask() {
            @Override
            public void run() {
                // only scheduled to find out whether the timer still accepts tasks
            }
        };
        try {
            timer.schedule(probe, 60_000L);
            probe.cancel();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private static Timer newCommonTimer() {
        return new Timer("JMRI Common Timer", true);
    }

    private static final Object timerLock = new Object();

    @GuardedBy("timerLock")
    private static Timer commonTimer = newCommonTimer();

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimerUtil.class);
}
