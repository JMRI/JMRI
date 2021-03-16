package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.*;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SignalHeadIconDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<SignalHead> tableModel = PickListModel.signalHeadPickModelInstance(); // NOI18N
        ControlPanelEditor editor = new ControlPanelEditor("EdTextItem");
        DisplayFrame df = new DisplayFrame("Indicator TO Icon Dialog Test", editor); // NOI18N
        SignalHeadItemPanel ship = new SignalHeadItemPanel(df,"IS01","",tableModel);  // NOI18N
        SignalHeadIconDialog shd = new SignalHeadIconDialog("SignalHead","SignalHead",ship); // NOI18N
        Assert.assertNotNull("exists",shd); // NOI18N
        JUnitUtil.dispose(shd);
        ship.dispose();
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalHeadIconDialogTest.class);

}
