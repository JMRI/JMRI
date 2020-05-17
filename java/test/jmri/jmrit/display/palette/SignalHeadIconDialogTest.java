package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.*;
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
public class SignalHeadIconDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<SignalHead> tableModel = PickListModel.signalHeadPickModelInstance(); // NOI18N
        EditorScaffold editor = new EditorScaffold("ED");
        DisplayFrame df = new DisplayFrame("Indicator TO Icon Dialog Test", editor); // NOI18N
        SignalHeadItemPanel sip = new SignalHeadItemPanel(df,"IS01","",tableModel);  // NOI18N
        SignalHeadIconDialog t = new SignalHeadIconDialog("SignalHead","SignalHead",sip,null); // NOI18N
        Assert.assertNotNull("exists",t); // NOI18N
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalHeadIconDialogTest.class);

}
