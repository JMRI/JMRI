package jmri.jmrit.ctc.editor.gui;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/*
* Tests for the FrmMainForm Class.
*
* @author  Dave Sand   Copyright (C) 2019
*/
public class FrmMainFormTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        javax.swing.JFrame frame = new FrmMainForm();
        Assert.assertNotNull("FrmMainForm Constructor Return", frame);
        JUnitUtil.dispose(frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}

// new EventTool().waitNoEvent(10000);
