package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of the CircuitBuilder class.
 *
 * @author  Paul Bender Copyright (C) 2017 
 */
public class CircuitBuilderTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor();
        CircuitBuilder cb = new CircuitBuilder(frame);
        Assert.assertNotNull("exists", cb );
        JUnitUtil.dispose(frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

}
