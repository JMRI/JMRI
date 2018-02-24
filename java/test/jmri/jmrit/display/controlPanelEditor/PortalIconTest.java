package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.jmrit.display.PositionableIconTest;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
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
    @Test
    @Ignore("not supported for PortalIcon")
    public void testDoViemMenu(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Do View Menu",p.doViemMenu());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
           editor = new ControlPanelEditor("Portal Icon Test Panel");
           p = new PortalIcon(editor);
        }
    }

}
