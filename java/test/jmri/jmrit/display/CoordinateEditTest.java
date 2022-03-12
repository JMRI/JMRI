package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CoordinateEdit
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CoordinateEditTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void initCheck() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new EditorScaffold();
        SensorIcon i = new SensorIcon(ef);
        // this test (currently) makes sure there are no exceptions
        // thrown when initComponents is called.
        try {
           ((CoordinateEdit)frame).init("foo",i,false);
        } catch( Exception e) {
            Assert.fail("Exception " + e + " Thrown during init call ");
        }
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CoordinateEdit();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }


}
