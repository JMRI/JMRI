package jmri.jmrit.display.palette;

import jmri.Turnout;
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
public class IconDialogTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<Turnout> tableModel = PickListModel.turnoutPickModelInstance();
        EditorScaffold editor = new EditorScaffold("Editor");
        DisplayFrame df = new DisplayFrame("Icon Dialog Test", editor);
        TableItemPanel<Turnout> tip = new TableItemPanel<>(df,"IS01","",tableModel);
        IconDialog t = new IconDialog("Icon","Icon",tip);
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IconDialogTest.class);

}
