package jmri.jmrit.logixng.tools.swing;

import jmri.InstanceManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test EditThreadsDialog
 *
 * @author Daniel Bergqvist 2021
 */
public class EditThreadsDialogTest {

    @Test
    public void testCtor() {
        jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                .createLogixNG("A logixNG with an empty conditionlNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, null);
        EditThreadsDialog t = new EditThreadsDialog(conditionalNG);
        Assertions.assertNotNull( t, "not null");
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
