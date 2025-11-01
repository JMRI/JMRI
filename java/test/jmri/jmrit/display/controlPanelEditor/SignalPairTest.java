package jmri.jmrit.display.controlPanelEditor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.SignalMastManager;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.Portal;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of the CircuitBuilder class.
 *
 * @author  Pete Cressman Copyright (C) 2020
 */
public class SignalPairTest {

    private ControlPanelEditor cpe;
    private CircuitBuilder cb;
    private SignalMastManager mastMgr;
    private PortalManager portalMgr;

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        getCPEandCB();

        jmri.SignalMast mast = mastMgr.getNamedBean("WestMainExit");
        assertNotNull( mast, "Mast exists");
        Portal portal = portalMgr.getPortal("West-WestExit");
        assertNotNull( portal, "Portal exists");
        SignalPair sp = new SignalPair(mast, portal);
        assertNotNull( sp, "SignalPair exists");

        String msg = sp.getDiscription();
        assertNotNull( msg, "msg exists");

    }

    void getCPEandCB() {
        ResourceBundle rbxWarrant = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
        Thread t = JemmyUtil.createModalDialogOperatorThread(
            rbxWarrant.getString("ErrorDialogTitle"), "OK");

        File f = new File("java/test/jmri/jmrit/display/controlPanelEditor/valid/CircuitBuilderTest.xml");
        assertDoesNotThrow( () ->
            InstanceManager.getDefault(ConfigureManager.class).load(f));

        JUnitUtil.waitThreadTerminated(t);

        cpe = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("CircuitBuilderTest Editor");
        assertNotNull( cpe, "CPE exists");
        cb = cpe.getCircuitBuilder();
        assertNotNull( cb, "CB exists");

        portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
        assertNotNull( portalMgr, "PortMgr exists");
        mastMgr = InstanceManager.getDefault(SignalMastManager.class);
        assertNotNull( mastMgr, "MastMgr exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
    }

    @AfterEach
    public void tearDown() {
        if (cpe != null) {
            JUnitUtil.dispose(cpe);
            cpe = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
            List<Runnable> rlist = sm.getRunnables();
            if (rlist.size() == 1 && rlist.get(0) instanceof jmri.jmrit.logix.WarrantShutdownTask) {
                sm.deregister((ShutDownTask)rlist.get(0));
            }
        }
        // cleaning up nameless invisible frame created by creating a dialog with a null parent
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalPairTest.class);

}
