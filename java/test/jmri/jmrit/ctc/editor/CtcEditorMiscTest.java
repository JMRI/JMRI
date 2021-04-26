package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import jmri.InstanceManager;
import jmri.profile.NullProfile;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the CtcEditorAction Class using signal masts.
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcEditorMiscTest {

    JFrameOperator _jfo = null;

    static final int DELAY = 0;
    static final boolean PAUSE = false;

    @Test
    public void testEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Misc_Scenarios.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        JUnitUtil.waitFor(5000);     // Wait for block routing and SML initialization


        // Start the Editor
        new CtcEditorAction().actionPerformed(null);

        _jfo = new JFrameOperator("CTC Editor");
        Assert.assertNotNull(_jfo);
        if (!PAUSE) JUnitUtil.waitFor(2000);

        // ButtonXmlFiles -- No GUI object
        JButtonOperator jbXMLFiles = new JButtonOperator(_jfo, Bundle.getMessage("ButtonXmlFiles"));
        jbXMLFiles.doClick();

        _jfo.requestClose();
        _jfo = null;
        if (!PAUSE) JUnitUtil.waitFor(2000);
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile(folder));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcEditorSignalMastsTest.class);
}
