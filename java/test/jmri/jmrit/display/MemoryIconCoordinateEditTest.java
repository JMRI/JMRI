package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MemoryIconCoordinateEdit
 *
 * @author	Paul Bender Copyright (C) 2016
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
