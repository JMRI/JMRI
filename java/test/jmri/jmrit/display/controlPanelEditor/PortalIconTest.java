package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.jmrit.display.PositionableIconTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of the PortalIcon class.
 *
 * @author  Paul Bender Copyright (C) 2017 
 */
public class PortalIconTest extends PositionableIconTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", p);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
           ControlPanelEditor frame = new ControlPanelEditor("Portal Icon Test Panel");
           p = new PortalIcon(frame);
        }
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

}
