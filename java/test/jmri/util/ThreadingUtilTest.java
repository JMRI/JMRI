package jmri.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for ThreadingUtil class
 *
 * @author	Bob Jacobsen Copyright 2015
 */
public class ThreadingUtilTest extends TestCase {

    boolean done;
    
    public void testToLayout() {
        done = false;
        
        ThreadingUtil.runOnLayout( ()-> { 
            done = true; 
        } );
        
        Assert.assertTrue(done);
    }

    public void testThreadingNesting() {
        done = false;
        
        new Thread(
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
        ).start();

        // wait for separate thread to do it's work before confirming test
        JUnitUtil.waitFor( ()->{ return done; }, "Separate thread complete");
    }

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

    /**
     * Show how to query state of _current_ thread
     */
    public void testSelfState() {
    
        // To run the tests, this thread has to be running, not waiting
        Assert.assertTrue(ThreadingUtil.canThreadRun(Thread.currentThread()));
        Assert.assertFalse(ThreadingUtil.isThreadWaiting(Thread.currentThread()));
    }

    // from here down is testing infrastructure
    public ThreadingUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ThreadingUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThreadingUtilTest.class);
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
