package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.AbstractEditorTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of the ControlPanelEditor class.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ControlPanelEditorTest extends AbstractEditorTestBase<ControlPanelEditor> {

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor f = new ControlPanelEditor();
        Assert.assertNotNull("exists", f);
        f.dispose();
        if (f.makeCatalogWorker != null) JUnitUtil.waitFor(() -> {return f.makeCatalogWorker.isDone();}, "wait for catalog SwingWorker failed");
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", e);
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = new ControlPanelEditor("Control Panel Editor Test");
        }
    }

    @After
    @Override
    public void tearDown() {
        if (e != null) {
            JUnitUtil.dispose(e);
            if (e.makeCatalogWorker != null) 
                JUnitUtil.waitFor(() -> {return e.makeCatalogWorker.isDone();}, "wait for catalog SwingWorker failed");
            e = null;
        }
        JUnitUtil.tearDown();
    }

}
