package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.DisplayFrame;
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
public class IndicatorTOIconDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<jmri.Turnout> tableModel = PickListModel.turnoutPickModelInstance();
        jmri.util.JUnitUtil.resetProfileManager();
        EditorScaffold editor = new EditorScaffold("ED");
        DisplayFrame df = new DisplayFrame("DisplayFrame", editor);
        IndicatorTOItemPanel itp = new IndicatorTOItemPanel(df,"IT01","",tableModel);
        IndicatorTOIconDialog t = new IndicatorTOIconDialog("Turnout","Turnout",itp,"",null);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorTOIconDialogTest.class);

}
