package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MemoryIconCoordinateEdit
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class MemoryIconCoordinateEditTest {

    @Test
    public void testCtor() {
        MemoryIconCoordinateEdit frame = new MemoryIconCoordinateEdit();
        assertNotNull(frame,"exists");
    }

    @Test
    public void initCheck() {
        MemoryIconCoordinateEdit frame = new MemoryIconCoordinateEdit();
        Editor ef = new EditorScaffold();
        SensorIcon i = new SensorIcon(ef);
        // this test (currently) makes sure there are no exceptions
        // thrown when initComponents is called.
        assertDoesNotThrow( () ->
            frame.init("foo",i,false));
        JUnitUtil.dispose(ef);
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


}
