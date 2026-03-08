package jmri.jmrit.display.palette;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IndicatorTOIconDialogTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<jmri.Turnout> tableModel = PickListModel.turnoutPickModelInstance();
        JUnitUtil.resetProfileManager();
        EditorScaffold editor = new EditorScaffold("ED");
        DisplayFrame df = new DisplayFrame("DisplayFrame", editor);
        IndicatorTOItemPanel itp = new IndicatorTOItemPanel(df,"IT01","",tableModel);
        IndicatorTOIconDialog t = new IndicatorTOIconDialog("Turnout","Turnout",itp);
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
        JUnitUtil.clearShutDownManager();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorTOIconDialogTest.class);

}
