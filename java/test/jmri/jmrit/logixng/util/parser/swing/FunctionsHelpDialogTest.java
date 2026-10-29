package jmri.jmrit.logixng.util.parser.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test FunctionsHelpDialog
 *
 * @author Daniel Bergqvist 2021
 */
public class FunctionsHelpDialogTest {

    @Test
    public void testCtor() {
        FunctionsHelpDialog t = new FunctionsHelpDialog();
        Assertions.assertNotNull( t, "not null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initTimeProviderManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
