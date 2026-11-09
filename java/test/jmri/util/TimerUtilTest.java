package jmri.util;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class TimerUtilTest {

    // no testCtor as tested class only supplied static methods

    @Test
    public void testScheduleDate() {
        TimerUtil.schedule(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
    }

    @Test
    public void testScheduleDateOnLayout() {
        TimerUtil.scheduleOnLayoutThread(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
        assertTrue(taskRanOnLayout);
    }

    @Test
    public void testScheduleDateOnGUI() {
        TimerUtil.scheduleOnGUIThread(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
        assertTrue(taskRanOnGUI);
    }

    @Test
    public void testTasksStillRunAfterATaskThrows() throws InterruptedException {
        AtomicReference<Thread> timerThread = new AtomicReference<>();
        Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> { }); // the timer's thread ends with the task's exception
        try {
            TimerUtil.schedule(new TimerTask() {
                @Override
                public void run() {
                    timerThread.set(Thread.currentThread());
                    throw new IllegalArgumentException("thrown by a task, as a fault in any task might");
                }
            }, 0);
            assertTrue(JUnitUtil.waitFor(() -> timerThread.get() != null));
            timerThread.get().join(5000);
            assertFalse(timerThread.get().isAlive(), "the exception ended the timer's thread");
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(handler);
        }

        TimerUtil.schedule(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> taskRan), "a task scheduled later still runs");
        JUnitAppender.assertErrorMessageStartsWith("JMRI Common Timer stopped");

        taskRan = false;
        TimerUtil.scheduleOnLayoutThread(new TimerTask() {
            @Override
            public void run() {
                taskRan = true;
            }
        }, 10);
        assertTrue(JUnitUtil.waitFor(() -> taskRan), "and so do delayed layout-thread tasks");
    }

    @Test
    public void testEveryScheduleMethodRunsItsTask() {
        java.util.concurrent.atomic.AtomicInteger ran = new java.util.concurrent.atomic.AtomicInteger();
        java.util.List<TimerTask> scheduled = new java.util.ArrayList<>();
        java.util.function.Supplier<TimerTask> counting = () -> {
            TimerTask t = new TimerTask() {
                @Override
                public void run() {
                    cancel(); // run once, repeating or not
                    ran.incrementAndGet();
                }
            };
            scheduled.add(t);
            return t;
        };
        final long HOUR = 3_600_000L; // repeats never come round during a test; the layout-thread ones can't be cancelled
        java.util.Date soon = new java.util.Date(System.currentTimeMillis() + 10);
        TimerUtil.schedule(counting.get(), soon);
        TimerUtil.schedule(counting.get(), soon, HOUR);
        TimerUtil.schedule(counting.get(), 10);
        TimerUtil.schedule(counting.get(), 10, HOUR);
        TimerUtil.scheduleAtFixedRate(counting.get(), soon, HOUR);
        TimerUtil.scheduleAtFixedRate(counting.get(), 10, HOUR);
        scheduled.add(TimerUtil.scheduleOnGUIThread(counting.get(), soon));
        scheduled.add(TimerUtil.scheduleOnGUIThread(counting.get(), soon, HOUR));
        scheduled.add(TimerUtil.scheduleOnGUIThread(counting.get(), 10));
        scheduled.add(TimerUtil.scheduleOnGUIThread(counting.get(), 10, HOUR));
        scheduled.add(TimerUtil.scheduleAtFixedRateOnGUIThread(counting.get(), soon, HOUR));
        scheduled.add(TimerUtil.scheduleAtFixedRateOnGUIThread(counting.get(), 10, HOUR));
        TimerUtil.scheduleOnLayoutThread(counting.get(), soon);
        TimerUtil.scheduleOnLayoutThread(counting.get(), soon, HOUR);
        TimerUtil.scheduleOnLayoutThread(counting.get(), 10);
        TimerUtil.scheduleOnLayoutThread(counting.get(), 10, HOUR);
        TimerUtil.scheduleAtFixedRateOnLayoutThread(counting.get(), soon, HOUR);
        TimerUtil.scheduleAtFixedRateOnLayoutThread(counting.get(), 10, HOUR);
        assertTrue(JUnitUtil.waitFor(() -> ran.get() >= 18), "all 18 ran");
        scheduled.forEach(TimerTask::cancel); // the repeating GUI and layout wrappers keep repeating until cancelled
    }

    @Test
    public void testTaskScheduledTwiceOnlyWarns() {
        TimerUtil.schedule(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> taskRan));
        TimerUtil.schedule(task, 10); // a TimerTask can be scheduled once only: the caller's mistake, not a stopped timer
        JUnitAppender.assertWarnMessageStartsWith("During schedule()");
        taskRan = false;
        TimerUtil.schedule(new TimerTask() {
            @Override
            public void run() {
                taskRan = true;
            }
        }, 10);
        assertTrue(JUnitUtil.waitFor(() -> taskRan), "the timer carries on");
    }

    private boolean taskRan = false;
    private boolean taskRanOnGUI = false;
    private boolean taskRanOnLayout = false;

    final TimerTask task = new TimerTask(){
        @Override
        public void run() {
            taskRanOnLayout = ThreadingUtil.isGUIThread();
            taskRanOnGUI = ThreadingUtil.isLayoutThread();
            taskRan = true;
        }
    };

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        taskRan = false;
        taskRanOnGUI = false;
        taskRanOnLayout = false;        
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimerUtilTest.class);

}
