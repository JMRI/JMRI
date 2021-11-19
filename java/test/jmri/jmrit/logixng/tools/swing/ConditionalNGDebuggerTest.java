package jmri.jmrit.logixng.tools.swing;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.*;

/**
 * Test ConditionalNGDebugger
 *
 * @author Daniel Bergqvist 2021
 */
public class ConditionalNGDebuggerTest {

    @org.junit.Ignore("Fails in Java 11 testing")
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ThreadingUtil.runOnGUI(() -> {
            jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                    .createLogixNG("A logixNG with an empty conditionlNG");
            ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, null);
            new ConditionalNGDebugger(conditionalNG);
        });
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        // Java 11 integration temporary - clear messages to get JUnit 5 traceback
        //jmri.util.JUnitAppender.clearBacklog();

        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
