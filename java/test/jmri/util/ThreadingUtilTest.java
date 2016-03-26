package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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


    // from here down is testing infrastructure
    public ThreadingUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ThreadingUtilTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThreadingUtilTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

}
