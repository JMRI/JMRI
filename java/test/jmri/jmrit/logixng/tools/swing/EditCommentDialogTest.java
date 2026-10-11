package jmri.jmrit.logixng.tools.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test EditCommentDialog
 *
 * @author Daniel Bergqvist 2021
 */
public class EditCommentDialogTest {

    @Test
    public void testCtor() {
        EditCommentDialog d = new EditCommentDialog();
        Assertions.assertNotNull(d);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
