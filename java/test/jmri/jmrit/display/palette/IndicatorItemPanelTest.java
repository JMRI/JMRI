package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.EditorScaffold;
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
public class IndicatorItemPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DisplayFrame df = new DisplayFrame();
        EditorScaffold es = new EditorScaffold();
        IndicatorItemPanel t = new IndicatorItemPanel(df,"test1","test2",es);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(df);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorItemPanelTest.class);

}
