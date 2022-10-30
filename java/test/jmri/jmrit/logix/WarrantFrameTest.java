package jmri.jmrit.logix;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logix.Bundle;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Pete Cressman Copyright (C) 2020
 */
public class WarrantFrameTest {
    private WarrantManager _warrantMgr;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCTorNull(){
       assertThat(new WarrantFrame(null, null)).withFailMessage("WarrantFrame exits").isNotNull();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCTorWarrant() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/MeetTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        Warrant w = _warrantMgr.getWarrant("WestBoundStart");
        assertThat(new WarrantFrame(w, null)).withFailMessage("WarrantFrame exits").isNotNull();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testJoinWarrants() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/MeetTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();

        Warrant startW = _warrantMgr.getWarrant("WestBoundStart");
        Warrant endW = _warrantMgr.getWarrant("WestBoundFinish");

//        JFrameOperator jfo = new JFrameOperator(tableFrame);
//        JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("stopAtBlock", startW.getLastOrder().getBlock().getDisplayName()));
//        JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonYes"));
//        jbo.push();
        /*       
        new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("QuestionTitle"));
            JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("stopAtBlock", startW.getLastOrder().getBlock().getDisplayName()));
            JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonYes"));
            jbo.push();
        }).start();*/
/*
        QuestionFrame question = new QuestionFrame(Bundle.getMessage("ButtonYes"), startW.getLastOrder().getBlock().getDisplayName());
        question.start();*/

        QuestionFrame question = new QuestionFrame(tableFrame, Bundle.getMessage("ButtonYes"), startW.getLastOrder().getBlock().getDisplayName());
        question.start();
        
        WarrantFrame warrantFrame= new WarrantFrame(startW, endW);
        System.out.println("return WarrantFrame");
        assertThat(warrantFrame).withFailMessage("JoinWFrame exits").isNotNull();

        warrantFrame._userNameBox.setText("WestBound");
        JFrameOperator editFrame = new JFrameOperator(warrantFrame);
        JemmyUtil.pressButton(editFrame, Bundle.getMessage("ButtonSave"));

        Warrant w = _warrantMgr.getWarrant("WestBound");
        assertThat(w).withFailMessage("Concatenated Warrant exits").isNotNull();

        warrantFrame.close();
        warrantFrame.dispose();
    }

    private class QuestionFrame extends Thread {
        String answer;
        String blockName;
        JFrameOperator jfo;
        JDialogOperator jdo;
        JButtonOperator jbo;
        WarrantTableFrame f;
       QuestionFrame(WarrantTableFrame tableFrame, String ans, String name) {
            ans = answer;
            name = blockName;
//            jfo = o;
            f=tableFrame;
        }
       @Override
        public void run() {
            while (true) {
                JUnitUtil.waitFor(100);
//                jfo = new JFrameOperator(Bundle.getMessage("QuestionTitle"));
                jfo = new JFrameOperator(f);
                jdo = new JDialogOperator(jfo, Bundle.getMessage("stopAtBlock", blockName));
                jbo = new JButtonOperator(jdo, answer);
                jbo.push();
                break;
            }
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.resetProfileManager();
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

    // private final static Logger log = LoggerFactory.getLogger(WarrantFrameTest.class);

}
