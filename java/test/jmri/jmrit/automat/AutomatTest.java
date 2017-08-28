package jmri.jmrit.automat;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for classes in the jmri.jmrit.automat package
 *
 * @author	Bob Jacobsen Copyright 2008
  */
public class AutomatTest extends TestCase {

    boolean initDone;
    boolean handleDone;

    public void testCreate() {
        new AbstractAutomaton() {
        };
    }

    public void testRun() throws InterruptedException {
        initDone = false;
        handleDone = false;
        AbstractAutomaton a = new AbstractAutomaton() {
            @Override
            public void init() {
                initDone = true;
            }

            @Override
            public boolean handle() {
                handleDone = true;
                return false;
            }
        };
        Assert.assertTrue("!initDone at first", !initDone);
        Assert.assertTrue("!handleDone at first", !handleDone);

        // now run it
        a.start();

        // wait for thread to exec, failing if not
        jmri.util.JUnitUtil.waitFor(()->{return initDone;},"initDone after run");

        // and check
        Assert.assertTrue("handleDone after run", handleDone);
    }

    public void testRestart() throws InterruptedException {
        initDone = false;
        handleDone = false;
        AbstractAutomaton a = new AbstractAutomaton() {
            @Override
            public void init() {
                initDone = true;
            }

            @Override
            public boolean handle() {
                handleDone = true;
                return false;
            }
        };
        Assert.assertTrue("!initDone at first", !initDone);
        Assert.assertTrue("!handleDone at first", !handleDone);

        // now run it
        a.start();

        // wait for thread to exec, failing if not
        jmri.util.JUnitUtil.waitFor(()->{return initDone;},"initDone after run");

        // and check
        Assert.assertTrue("handleDone after run", handleDone);

        // restart
        initDone = false;
        handleDone = false;
        Assert.assertTrue("!initDone at second", !initDone);
        Assert.assertTrue("!handleDone at second", !handleDone);

        // now run it again
        a.start();

        // wait for thread to exec, failing if not
        jmri.util.JUnitUtil.waitFor(()->{return initDone;},"initDone after 2nd run");

        // and check
        Assert.assertTrue("handleDone after 2nd run", handleDone);
    }

    // from here down is testing infrastructure
    public AutomatTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AutomatTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(AutomatTest.class);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
