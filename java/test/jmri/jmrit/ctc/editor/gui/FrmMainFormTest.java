package jmri.jmrit.ctc.editor.gui;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.ctc.setup.CreateTestObjects;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        CreateTestObjects.createTestObjects();
        CreateTestObjects.createTestFiles();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}

// new EventTool().waitNoEvent(10000);
