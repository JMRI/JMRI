package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of the ControlPanelEditor class.
 *
 * @author  Paul Bender Copyright (C) 2017 
 */
public class ControlPanelEditorTest extends jmri.jmrit.display.AbstractEditorTestBase {
        
    private ControlPanelEditor frame = null;

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor f = new ControlPanelEditor();
        Assert.assertNotNull("exists", f );
        f.dispose();
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", frame );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = frame = new ControlPanelEditor("Control Panel Editor Test");
        }
    }

    @After
    public void tearDown() {
        if (frame != null) {
            JUnitUtil.dispose(frame);
            e = frame = null;
        }
       JUnitUtil.tearDown();
    }

}
