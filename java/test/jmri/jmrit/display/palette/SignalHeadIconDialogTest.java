package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.*;
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
public class SignalHeadIconDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<SignalHead> tableModel = PickListModel.signalHeadPickModelInstance(); // NOI18N
        DisplayFrame df = new DisplayFrame("Indicator TO Icon Dialog Test"); // NOI18N
        Editor editor = new EditorScaffold();
        SignalHeadItemPanel sip = new SignalHeadItemPanel(df,"IS01","",tableModel,editor);  // NOI18N
        SignalHeadIconDialog t = new SignalHeadIconDialog("SignalHead","SignalHead",sip,null); // NOI18N
        Assert.assertNotNull("exists",t); // NOI18N
        JUnitUtil.dispose(t);
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

    // private final static Logger log = LoggerFactory.getLogger(SignalHeadIconDialogTest.class);

}
