package jmri.jmrit.logix;

import java.io.File;

import jmri.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Pete Cressman Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class WarrantFrameTest {

    private WarrantManager _warrantMgr;

    @Test
    public void testCTorNull(){
        WarrantFrame wf = new WarrantFrame(null, null);
        assertNotNull(wf ,"WarrantFrame exists");
        
        wf.dispose();
    }

    @Test
    public void testCTorWarrant() throws JmriException {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/MeetTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        Warrant w = _warrantMgr.getWarrant("WestBoundStart");
        WarrantFrame wf = new WarrantFrame(w, null);
        assertNotNull(wf,"WarrantFrame exists");

        // JFrameOperator requestClose just hides panel, not disposing of it.
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            wf.dispose();
            JmriJFrame.getFrame("TrainMeetTest").dispose();
            return true;
        });
        assertTrue(retVal);

    }

    @Test
    public void testJoinWarrantsStop() throws JmriException {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/MeetTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        JUnitUtil.waitFor(100);

        Warrant startW = _warrantMgr.getWarrant("WestBoundStart");
        Warrant endW = _warrantMgr.getWarrant("WestBoundFinish");

        Thread t = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"));
        });
        t.setName("WarrantFrameTest Answer Question stop train");
        t.start();
        
        WarrantFrame warrantFrame = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new WarrantFrame(startW, endW);
        });
        assertNotNull(warrantFrame,"JoinWFrame exits");

        JUnitUtil.waitFor( () -> !t.isAlive(), "dialogue stop train in block east main? answered");

        JFrameOperator editFrame = new JFrameOperator(warrantFrame);

        JLabelOperator jlo = new JLabelOperator(editFrame, Bundle.getMessage("LabelUserName"));
        ((javax.swing.JTextField) jlo.getLabelFor()).setText("WestBoundLocal");

        JemmyUtil.pressButton(editFrame, Bundle.getMessage("ButtonSave"));

        Warrant w = _warrantMgr.getWarrant("WestBoundLocal");
        assertNotNull(w,"Concatenated Warrant exists");

        // JFrameOperator requestClose just hides panel, not disposing of it.
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            warrantFrame.close();
            JmriJFrame.getFrame("Warrant Table").dispose();
            JmriJFrame.getFrame("TrainMeetTest").dispose();
            return true;
        });
        assertTrue(retVal);
    }

    @Test
    public void testJoinWarrantsNoStop() throws JmriException {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/MeetTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        JUnitUtil.waitFor(100);

        Warrant startW = _warrantMgr.getWarrant("WestBoundStart");
        Warrant endW = _warrantMgr.getWarrant("WestBoundFinish");

        Thread clickDialog = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"));
        });
        clickDialog.setName("WarrantFrameTest click Question No");
        clickDialog.start();

        WarrantFrame warrantFrame = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new WarrantFrame(startW, endW);
        });
        assertNotNull(warrantFrame,"JoinWFrame exists");

        JFrameOperator editFrame = new JFrameOperator(warrantFrame);

        JLabelOperator jlo = new JLabelOperator(editFrame, Bundle.getMessage("LabelUserName"));
        ((javax.swing.JTextField) jlo.getLabelFor()).setText("WestBound");

        JemmyUtil.pressButton(editFrame, Bundle.getMessage("ButtonSave"));

        JUnitUtil.waitFor(() -> !clickDialog.isAlive(),"QuestionTitle ButtonNo clicked");

        Warrant w = _warrantMgr.getWarrant("WestBound");
        assertNotNull(w,"Concatenated Warrant exists");
        
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            warrantFrame.close();
            JmriJFrame.getFrame("Warrant Table").dispose();
            JmriJFrame.getFrame("TrainMeetTest").dispose();
            return true;
        });
        assertTrue(retVal);
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initDebugThrottleManager();
        _warrantMgr = InstanceManager.getDefault(WarrantManager.class);
    }

    @AfterEach
    public void tearDown() {
        _warrantMgr.dispose();
        _warrantMgr = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
