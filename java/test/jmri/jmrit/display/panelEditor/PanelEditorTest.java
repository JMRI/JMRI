package jmri.jmrit.display.panelEditor;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.AbstractEditorTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PanelEditorTest extends AbstractEditorTestBase<PanelEditor> {

    @Test
    public void testDefaultCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PanelEditor p = new PanelEditor();
        Assert.assertNotNull("exists", p);
        p.dispose();
    }

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", e);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = new PanelEditor("Panel Editor Test");
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (e != null) {
            JUnitUtil.dispose(e.getTargetFrame());
            JUnitUtil.dispose(e);
            e = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PanelEditorTest.class);

}
