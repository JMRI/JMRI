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
        
    private PanelEditor pe = new PanelEditor();

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",pe);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            e = pe = new PanelEditor();
        }
    }

    @After
    public void tearDown() {
        if (pe != null) {
            JUnitUtil.dispose(pe);
            e = pe = null;
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PanelEditorTest.class);

}
