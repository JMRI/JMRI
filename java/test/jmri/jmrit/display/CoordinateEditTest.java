package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CoordinateEdit
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class CoordinateEditTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void initCheck() {
        Editor ef = new EditorScaffold();
        SensorIcon i = new SensorIcon(ef);
        // this test (currently) makes sure there are no exceptions
        // thrown when initComponents is called.
        assertDoesNotThrow( () ->
            ((CoordinateEdit)frame).init("foo",i,false));
        ThreadingUtil.runOnGUI( () -> frame.setVisible(true));
        JUnitUtil.dispose(ef);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        frame = new CoordinateEdit();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

}
