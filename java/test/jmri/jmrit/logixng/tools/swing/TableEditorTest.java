package jmri.jmrit.logixng.tools.swing;

import jmri.InstanceManager;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test TableEditor
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class TableEditorTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {

        InstanceManager.getDefault(NamedTableManager.class).newInternalTable("IQT1", null, 2, 3);
        TableEditor b = new TableEditor(null, "IQT1");
        Assertions.assertNotNull( b, "exists");
        b.finishDone();
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
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeDiagramTest.class);

}
