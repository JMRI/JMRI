package jmri.util;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This class serves as a demonstration of some good
 * threading practices in JMRI, and also as a run-time test of them.
 * <p>
 * It's in the java/test package tree because it's not 
 * something we intend to ship to users.
 * <p?
 * For information on threading in JMRI, see the
 * <href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">Threading doc page</a>.
 * See also the {@link ThreadingUtil} and {@lnk WaitHandler} classes along with the 
 * examples in their associated JUnit test classes:
 * {@link ThreadingUtilTest} and {@lnk WaitHandlerTest}.
 *
 * @author	Bob Jacobsen Copyright 2017
 */
public class ThreadingDemoAndTest extends TestCase {

    volatile boolean flagInterrupted1 = false;
    volatile boolean flagInterrupted2 = false;
    volatile boolean flagInterrupted3 = false;

    /** 
     * Show the basic life-cycle of a thread
     * <ul>
     *    <li>being created, 
     *    <li>being started, 
     *    <li>waiting on a lock on an object,
     *    <li>being woken up, and 
     *    <li>terminating.
     * </ul>
     * Plus the synchronization needed around the wait and wake-up calls
     */
    public void testThreadingLifeCycle() {
        final Object lock = new Object();  // this object is the lock for the wait and notify
        final Thread t = new Thread() {
            public void run()  {
                try {
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.setName("testThreadingLifeCycle");
        t.setDaemon(true);
        
        // confirm our understanding of the life cycle
        Assert.assertTrue(t.getState().equals(Thread.State.NEW));
        
        t.start();
        JUnitUtil.waitFor( ()->{ return ThreadingUtil.isThreadWaiting(t); }, "Got to wait state");
        
        synchronized(lock) {
            lock.notifyAll();
        }
        JUnitUtil.waitFor( ()->{ return t.getState().equals(Thread.State.TERMINATED); }, "Got to terminated state");        
    }

    /**
     * How one thread t2 can "join" on the ending of another thread t1
     */
    public void testThreadingJoinCycle() {
        final Object lock = new Object();
        final Thread t1 = new Thread() {
            public void run()  {
                try {
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t1.setName("testThreadingLifeCycle 1");
        t1.setDaemon(true);
        
        final Thread t2 = new Thread() {
            public void run()  {
                try {
                    t1.join();
                } catch (InterruptedException e) {
                }
            }
        };
        t2.setName("testThreadingLifeCycle 2");
        t2.setDaemon(true);
        
        // confirm our understanding of the life cycle
        Assert.assertTrue(t1.getState().equals(Thread.State.NEW));
        Assert.assertTrue(t2.getState().equals(Thread.State.NEW));
        
        t1.start();
        t2.start();
        
        JUnitUtil.waitFor( ()->{ return ThreadingUtil.isThreadWaiting(t1); }, "Got 1 to wait state");
        JUnitUtil.waitFor( ()->{ return ThreadingUtil.isThreadWaiting(t2); }, "Got 2 to wait state");
        
        synchronized(lock) {
            lock.notifyAll();
        }
        
        JUnitUtil.waitFor( ()->{ return t1.getState().equals(Thread.State.TERMINATED); }, "Got 1 to terminated state");        
        JUnitUtil.waitFor( ()->{ return t2.getState().equals(Thread.State.TERMINATED); }, "Got 2 to terminated state");        
    }
    

    /**
     * Interrupting a thread ends the current wait, but 
     * doesn't kill the thread; it can go on to wait more.
     */
    public void testInterruptAndContinue() {
        flagInterrupted1 = false;  // set true when we get to the first wait
        flagInterrupted2 = false;  // set true when we get to the second wait

        final Object lock = new Object();
        final Thread t1 = new Thread() {
            public void run()  {
                try {
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    flagInterrupted1 = true;
                    // we're just going to continue to another wait
                }
                try {
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    flagInterrupted2 = true;
                    // we're just going to continue and terminate
                }
                
            }
        };
        t1.setName("testInterruptAndContinue");
        t1.setDaemon(true);
                
        // confirm our understanding of the life cycle
        Assert.assertTrue(t1.getState().equals(Thread.State.NEW));
        
        t1.start();
        
        JUnitUtil.waitFor( ()->{ return ThreadingUtil.isThreadWaiting(t1); }, "Got to wait state");
        
        t1.interrupt(); // end 1st wait

        JUnitUtil.waitFor( ()->{ return flagInterrupted1; }, "handled first interrupt");
        JUnitUtil.waitFor( ()->{ return ThreadingUtil.isThreadWaiting(t1); }, "and went to second wait");

        t1.interrupt(); // end 2nd wait
        
        JUnitUtil.waitFor( ()->{ return flagInterrupted2; }, "handled second interrupt");
        JUnitUtil.waitFor( ()->{ return t1.getState().equals(Thread.State.TERMINATED); }, "Got 1 to terminated state");        
    }

    /**
     * Interrupting a thread that restores the interrupted status also kills the next wait
     */
    public void testThreadReassertsInterrupt() {
        flagInterrupted1 = false;  // set true when we leave the first wait
        flagInterrupted2 = false;  // set true when we leave the second wait
        flagInterrupted3 = false;  // set true when we leave the third wait

        final Object lock = new Object();
        final Thread t1 = new Thread() {
            public void run()  {
                try {
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    flagInterrupted1 = true;
                    Thread.currentThread().interrupt();
                    // restored the status for the next wait
                }
                try {
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    flagInterrupted2 = true;
                }
                try {
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    flagInterrupted3 = true;
                    // we're just going to continue and terminate
                }
                
            }
        };
        t1.setName("testThreadReassertsInterrupt");
        t1.setDaemon(true);
                
        // confirm our understanding of the life cycle
        Assert.assertTrue(t1.getState().equals(Thread.State.NEW));
        
        t1.start();
        
        JUnitUtil.waitFor( ()->{ return ThreadingUtil.isThreadWaiting(t1); }, "Got to wait state");
        
        t1.interrupt(); // end 1st wait

        JUnitUtil.waitFor( ()->{ return flagInterrupted1; }, "handled first interrupt");
        JUnitUtil.waitFor( ()->{ return flagInterrupted2; }, "continued to second interrupt handler");
        
        // it ran to the 2nd, but waited there because that consumed the interrupt
        JUnitUtil.waitFor( ()->{ return ThreadingUtil.isThreadWaiting(t1); }, "at third wait");        
        Assert.assertTrue(! flagInterrupted3); 

        t1.interrupt(); // end 3rd wait
        
        JUnitUtil.waitFor( ()->{ return flagInterrupted3; }, "continued to third interrupt handler");
        JUnitUtil.waitFor( ()->{ return t1.getState().equals(Thread.State.TERMINATED); }, "continued off the end to terminated state");        
    }


    // from here down is testing infrastructure
    public ThreadingDemoAndTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ThreadingDemoAndTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThreadingDemoAndTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

}
