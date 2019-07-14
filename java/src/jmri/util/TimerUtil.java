package jmri.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nonnull;

import jmri.util.ThreadingUtil;

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
 *
 * @author Bob Jacobsen Copyright 2018
 */
@javax.annotation.concurrent.Immutable
final public class TimerUtil {

    // Timer implementation methods
    
    static public void schedule(@Nonnull TimerTask task, @Nonnull Date time) {
        commonTimer.schedule(task, time);
    }

    static public void schedule(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        commonTimer.schedule(task, firstTime, period);
    }
    
    static public void schedule(@Nonnull TimerTask task, long delay) {
        commonTimer.schedule(task, delay);
    }
    
    static public void schedule(@Nonnull TimerTask task, long delay, long period) {
        commonTimer.schedule(task, delay, period);
    }
    
    static public void scheduleAtFixedRate(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        commonTimer.schedule(task, firstTime, period);
    }
    
    static public void scheduleAtFixedRate(@Nonnull TimerTask task, long delay, long period) {
        commonTimer.schedule(task, delay, period);
    }
   
   
    // GUI-thread implementation methods
    
    // arrange to run on GUI thread
    static private TimerTask gtask(TimerTask task) {
        return new TimerTask(){
                @Override
                public void run() {
                    ThreadingUtil.runOnGUIEventually(() -> {task.run();});
                }
        };
    }

    static public void scheduleOnGUIThread(@Nonnull TimerTask task, @Nonnull Date time) {
        commonTimer.schedule(gtask(task), time);
    }

    static public void scheduleOnGUIThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        commonTimer.schedule(gtask(task), firstTime, period);
    }
    
    static public void scheduleOnGUIThread(@Nonnull TimerTask task, long delay) {
        commonTimer.schedule(gtask(task), delay);
    }
    
    static public void scheduleOnGUIThread(@Nonnull TimerTask task, long delay, long period) {
        commonTimer.schedule(gtask(task), delay, period);
    }
    
    static public void scheduleAtFixedRateOnGUIThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        commonTimer.schedule(gtask(task), firstTime, period);
    }
    
    static public void scheduleAtFixedRateOnGUIThread(@Nonnull TimerTask task, long delay, long period) {
        commonTimer.schedule(gtask(task), delay, period);
    }
   
   
    // arrange to run on layout thread
    static private TimerTask ltask(TimerTask task) {
        return new TimerTask(){
                @Override
                public void run() {
                    ThreadingUtil.runOnLayoutEventually(() -> {task.run();});
                }
        };
    }
    
    static public void scheduleOnLayoutThread(@Nonnull TimerTask task, @Nonnull Date time) {
        commonTimer.schedule(ltask(task), time);
    }

    static public void scheduleOnLayoutThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        commonTimer.schedule(ltask(task), firstTime, period);
    }
    
    static public void scheduleOnLayoutThread(@Nonnull TimerTask task, long delay) {
        commonTimer.schedule(ltask(task), delay);
    }
    
    static public void scheduleOnLayoutThread(@Nonnull TimerTask task, long delay, long period) {
        commonTimer.schedule(ltask(task), delay, period);
    }
    
    static public void scheduleAtFixedRateOnLayoutThread(@Nonnull TimerTask task, @Nonnull Date firstTime, long period) {
        commonTimer.schedule(ltask(task), firstTime, period);
    }
    
    static public void scheduleAtFixedRateOnLayoutThread(@Nonnull TimerTask task, long delay, long period) {
        commonTimer.schedule(ltask(task), delay, period);
    }
   
   
    final static Timer commonTimer = new Timer("JMRI Common Timer", true);
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimerUtil.class);
}
