package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.picker.PickListModel;
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
public class IndicatorTOItemPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel tableModel = PickListModel.turnoutPickModelInstance(); // N11N
        DisplayFrame df = new DisplayFrame("Indicator TO Item Panel Test");
        Editor editor = new EditorScaffold();
        IndicatorTOItemPanel t = new IndicatorTOItemPanel(df,"IT01","",tableModel,editor);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(df);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorTOItemPanelTest.class);

}
