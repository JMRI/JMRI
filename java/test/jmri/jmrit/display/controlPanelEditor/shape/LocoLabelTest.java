package jmri.jmrit.display.controlPanelEditor.shape;

import jmri.jmrit.display.EditorScaffold;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class LocoLabelTest {

    @Test
    public void testCTor() {

        EditorScaffold editor = new EditorScaffold();
        LocoLabel t = new LocoLabel(editor);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(editor);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoLabelTest.class);

}
