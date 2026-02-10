package jmri.jmrit.entryexit;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AddEntryExitPairActionTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        LayoutEditor e = new LayoutEditor();
        AddEntryExitPairAction t = new AddEntryExitPairAction("Test Action",e);
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(e);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddEntryExitPairActionTest.class);

}
