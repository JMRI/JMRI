package jmri.jmrit.logixng.tools.swing;

import jmri.InstanceManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test ConditionalNGDebugger
 *
 * @author Daniel Bergqvist 2021
 */
public class ConditionalNGDebuggerTest {

    @Disabled("Fails in Java 11 testing")
    @Test
    @DisabledIfHeadless
    public void testCtor() {

        var cngd = ThreadingUtil.runOnGUIwithReturn(() -> {
            jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                    .createLogixNG("A logixNG with an empty conditionlNG");
            ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, null);
            return new ConditionalNGDebugger(conditionalNG);
        });
        Assertions.assertNotNull(cngd);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
