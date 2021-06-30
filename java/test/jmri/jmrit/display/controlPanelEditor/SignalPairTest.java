package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.SignalMastManager;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.Portal;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of the CircuitBuilder class.
 *
 * @author  Pete Cressman Copyright (C) 2020
 */
public class SignalPairTest {

    ControlPanelEditor cpe;
    CircuitBuilder cb;
    private SignalMastManager mastMgr;
    private PortalManager portalMgr;

    @Test
    public void testCtor() {
        getCPEandCB();

        jmri.SignalMast mast = mastMgr.getNamedBean("WestMainExit");
        Assert.assertNotNull("Mast exists", mast);
        Portal portal = portalMgr.getPortal("West-WestExit");
        Assert.assertNotNull("Portal exists", portal);
        SignalPair sp = new SignalPair(mast, portal);
        Assert.assertNotNull("SignalPair exists", sp);

        String msg = sp.getDiscription();
        Assert.assertNotNull("msg exists", msg);
    }

    void getCPEandCB() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        File f = new File("java/test/jmri/jmrit/display/controlPanelEditor/valid/CircuitBuilderTest.xml");
        try {
            InstanceManager.getDefault(ConfigureManager.class).load(f);
        } catch(JmriException je) {
            log.error("SignalPairTest can't load CircuitBuilderTester.xml {}", je);
        }
        cpe = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("CircuitBuilderTest Editor");
        Assert.assertNotNull("CPE exists", cpe );
        cb = cpe.getCircuitBuilder();
        Assert.assertNotNull("CB exists", cb );
        //fixed: jmri.util.JUnitAppender.assertWarnMessage("getIconMap failed. family \"null\" not found in item type \"Portal\"");
        portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
        Assert.assertNotNull("PortMgr exists", portalMgr );
        mastMgr = InstanceManager.getDefault(SignalMastManager.class);
        Assert.assertNotNull("MastMgr exists", mastMgr );
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
            cpe.dispose();
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
            List<Runnable> rlist = sm.getRunnables();
            if (rlist.size() == 1 && rlist.get(0) instanceof jmri.jmrit.logix.WarrantShutdownTask) {
                sm.deregister((ShutDownTask)rlist.get(0));
            }
        }
        JUnitUtil.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(SignalPairTest.class);
}
