package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

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
        IndicatorTOIconDialog t = new IndicatorTOIconDialog("Turnout","Turnout",itp);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
        JUnitUtil.clearShutDownManager();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorTOIconDialogTest.class);

}
