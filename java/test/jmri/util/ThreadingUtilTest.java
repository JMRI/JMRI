package jmri.util;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for ThreadingUtil class
 *
 * @author Bob Jacobsen Copyright 2015, 2020
 */
public class ThreadingUtilTest {

    boolean done;
    
    @Test
    public void testToLayout() {
        done = false;
        
        ThreadingUtil.runOnLayout( ()-> { 
            done = true; 
        } );
        
        Assert.assertTrue(done);
    }

    @Test
    public void testThreadGroup() {
        ThreadGroup tg = ThreadingUtil.getJmriThreadGroup();
        Assert.assertNotNull(tg);
        Assert.assertEquals(tg.getName(), "JMRI");
        Assert.assertEquals(tg, ThreadingUtil.getJmriThreadGroup());
    }
    
    @Test
    public void testThreadStartEnd() throws InterruptedException {
        ThreadingUtil.getJmriThreadGroup();  // just used to create the group
        Thread t = ThreadingUtil.newThread(() -> {});  // create and run quick
        t.join();
    }

    Object testRef = null;
    @Test
    public void testToGuiWarn() {
        // if (!java.lang.management.ManagementFactory
        //                                        .getThreadMXBean().isObjectMonitorUsageSupported())
        //        log.info("This JVM doesn't support object monitor tracking");
        // if (!java.lang.management.ManagementFactory
        //                                        .getThreadMXBean().isSynchronizerUsageSupported())
        //        log.info("This JVM doesn't support synchronized lock tracking");
                                                
        final Object lockedThing = new Object();
        done = false;
        
        synchronized (lockedThing) {
            // first, run something that also wants the lock
            new Thread(() -> {
                synchronized (lockedThing) {
                    testRef = lockedThing;
                }
            }).start();
            
            ThreadingUtil.runOnGUI( ()-> { 
                done = true; 
                Assert.assertNull(testRef); // due to lock
            } );
 
            JUnitUtil.waitFor( ()->{ return done; }, "GUI thread complete");
            Assert.assertNull(testRef); // due to lock
        }
        JUnitUtil.waitFor( ()->{ return testRef != null; }, "Locked thread complete");
    }

    @Test
    public void testThreadingNesting() {
        done = false;
        
        Thread t = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    // now on another thread
                    // switch back to Layout thread
                    ThreadingUtil.runOnLayout( ()-> { 
                        // on layout thread, confirm
                        Assert.assertTrue("on Layout thread", ThreadingUtil.isLayoutThread());
                        // mark done so we know
                        done = true; 
                    } );
                }
            }
        );
        t.setName("Thread Nesting Test Thread");
        t.start();

        // wait for separate thread to do it's work before confirming test
        JUnitUtil.waitFor( ()->{ return done; }, "Separate thread complete");
    }

    @Test
    public void testThreadingNestingToSwing() {
        done = false;
        
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    // now on Swing thread
                    // switch back to Layout thread
                    ThreadingUtil.runOnLayout( ()-> { 
                        // on layout thread, confirm
                        Assert.assertTrue("on Layout thread", ThreadingUtil.isLayoutThread());
                        // mark done so we known
                        done = true; 
                    } );
                }
            }
        );

        // wait for separate thread to do it's work before confirming test
        JUnitUtil.waitFor( ()->{ return done; }, "Separate thread complete");
    }

    @Test
    public void testThreadingDelayGUI() {
        done = false;
        
        ThreadingUtil.runOnGUIDelayed( ()-> { 
            done = true; 
        }, 200 );

        // ensure not done now
        Assert.assertTrue(!done);
        
        // wait for separate thread to do it's work before confirming test
        JUnitUtil.waitFor( ()->{ return done; }, "Delayed operation complete");
    }

    @Test
    public void testThreadingRunOnGUIwithReturn() {
        done = false;
        
        Integer value = ThreadingUtil.runOnGUIwithReturn( ()-> { 
            done = true; 
            return new Integer(21);
        });

        Assert.assertTrue(done);
        Assert.assertEquals(new Integer(21), value);
    }

    @Test
    public void testThreadingDelayLayout() {
        done = false;
        
        ThreadingUtil.runOnLayoutDelayed( ()-> { 
            done = true; 
        }, 200 );

        // ensure not done now
        Assert.assertTrue(!done);
        
        // wait for separate thread to do it's work before confirming test
        JUnitUtil.waitFor( ()->{ return done; }, "Delayed oepration complete");
    }

    @Test
    public void testThreadingTests() {
        ThreadingUtil.runOnLayout( ()-> { 
            ThreadingUtil.requireLayoutThread(log);
        } );
        ThreadingUtil.runOnGUI( ()-> { 
            ThreadingUtil.requireGuiThread(log);
        } );
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());

        ThreadingUtil.requireGuiThread(log);
        jmri.util.JUnitAppender.assertWarnMessage("Call not on GUI thread");

        ThreadingUtil.requireLayoutThread(log);
        jmri.util.JUnitAppender.assertWarnMessage("Call not on Layout thread");

        ThreadingUtil.requireGuiThread(log);
        ThreadingUtil.requireLayoutThread(log);
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
        
   }
    
    /**
     * Show how to query state of _current_ thread
     */
    @Test
    public void testSelfState() {
    
        // To run the tests, this thread has to be running, not waiting
        Assert.assertTrue(ThreadingUtil.canThreadRun(Thread.currentThread()));
        Assert.assertFalse(ThreadingUtil.isThreadWaiting(Thread.currentThread()));
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThreadingUtilTest.class);

}
