package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of MemoryIconCoordinateEdit
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MemoryIconCoordinateEditTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MemoryIconCoordinateEdit frame = new MemoryIconCoordinateEdit();
        Assert.assertNotNull("exists", frame );
    }

    @Test
    public void initCheck() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MemoryIconCoordinateEdit frame = new MemoryIconCoordinateEdit();
        Editor ef = new EditorScaffold();
        SensorIcon i = new SensorIcon(ef);
        // this test (currently) makes sure there are no exceptions
        // thrown when initComponents is called.
        try {
           frame.init("foo",i,false);
        } catch( Exception e) {
            Assert.fail("Exception " + e + " Thrown during init call ");
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }


}
