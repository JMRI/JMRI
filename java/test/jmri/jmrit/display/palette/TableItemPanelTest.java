package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.Turnout;
import jmri.jmrit.display.DisplayFrame;
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
public class TableItemPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<Turnout> tableModel = PickListModel.turnoutPickModelInstance();
        DisplayFrame df = new DisplayFrame("Table Item Panel Test"); // NOI18N
        TableItemPanel<Turnout> t = new TableItemPanel<Turnout>(df,"IS01","",tableModel); // NOI18N
        Assert.assertNotNull("exists",t); // NOI18N
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

    // private final static Logger log = LoggerFactory.getLogger(TableItemPanelTest.class);

}
