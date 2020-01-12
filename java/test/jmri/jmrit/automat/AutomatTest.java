package jmri.jmrit.automat;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for classes in the jmri.jmrit.automat package
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class AutomatTest {

    boolean initDone;
    boolean handleDone;

    @Test
    public void testCreate() {
        new AbstractAutomaton() {
        };
    }

    @Test
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

    @Test
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
