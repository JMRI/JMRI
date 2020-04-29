package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.Turnout;
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
public class IconDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<Turnout> tableModel = PickListModel.turnoutPickModelInstance();
        EditorScaffold editor = new EditorScaffold("Editor");
        DisplayFrame df = new DisplayFrame("Icon Dialog Test", editor);
        TableItemPanel<Turnout> tip = new TableItemPanel<>(df,"IS01","",tableModel);
        IconDialog t = new IconDialog("Icon","Icon",tip,null);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IconDialogTest.class);

}
