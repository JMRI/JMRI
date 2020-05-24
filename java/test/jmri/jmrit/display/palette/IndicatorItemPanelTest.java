package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.DisplayFrame;
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
        IndicatorItemPanel t = new IndicatorItemPanel(df,"test1","test2");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(df);
    }

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
