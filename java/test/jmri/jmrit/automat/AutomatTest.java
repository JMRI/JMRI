package jmri.jmrit.automat;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for classes in the jmri.jmrit.automat package
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class AutomatTest {

    private volatile boolean initDone;
    private volatile boolean handleDone;

    @Test
    public void testCreate() {
        Assertions.assertNotNull( new AbstractAutomaton() {} );
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

        // handle() runs after init() on the automaton thread, so wait for it too
        jmri.util.JUnitUtil.waitFor(()->{return handleDone;},"handleDone after run");
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

        // handle() runs after init() on the automaton thread, so wait for it too
        jmri.util.JUnitUtil.waitFor(()->{return handleDone;},"handleDone after run");

        // restart
        initDone = false;
        handleDone = false;
        Assert.assertTrue("!initDone at second", !initDone);
        Assert.assertTrue("!handleDone at second", !handleDone);

        // now run it again
        a.start();

        // wait for thread to exec, failing if not
        jmri.util.JUnitUtil.waitFor(()->{return initDone;},"initDone after 2nd run");

        // handle() runs after init() on the automaton thread, so wait for it too
        jmri.util.JUnitUtil.waitFor(()->{return handleDone;},"handleDone after 2nd run");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
