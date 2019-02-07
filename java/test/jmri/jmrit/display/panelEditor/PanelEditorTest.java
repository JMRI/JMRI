package jmri.jmrit.display.panelEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PanelEditorTest extends jmri.jmrit.display.AbstractEditorTestBase {
        
    private PanelEditor pe = null;

    @Test
    public void testDefaultCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PanelEditor p = new PanelEditor();
        Assert.assertNotNull("exists",p);
        p.dispose();
    }

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",pe);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = pe = new PanelEditor("Panel Editor Test");
        }
    }

    @After
    public void tearDown() {
        if (pe != null) {
            JUnitUtil.dispose(pe);
            JUnitUtil.dispose(pe.getTargetFrame());
            e = pe = null;
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PanelEditorTest.class);

}
