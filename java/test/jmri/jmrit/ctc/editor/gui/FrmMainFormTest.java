package jmri.jmrit.ctc.editor.gui;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/*
* Tests for the FrmMainForm Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class FrmMainFormTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("FrmMainForm Constructor Return", new FrmMainForm());
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}

// new EventTool().waitNoEvent(10000);
