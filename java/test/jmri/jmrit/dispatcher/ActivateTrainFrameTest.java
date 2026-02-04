package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.SignalMastManager;
import jmri.SignalMastLogicManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2025
 */
public class ActivateTrainFrameTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {

        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        ActivateTrainFrame t = new ActivateTrainFrame(d);
        assertNotNull( t, "exists");
        t.dispose();
        JUnitUtil.dispose(d);

    }

    @Test
    @DisabledIfHeadless
    public void testOpenThenCancelNewTrainFrame() {

        JFrameOperator dispatcherWindowOperator = launchLayoutAndGetDispatcher();
        JButtonOperator jbo = new JButtonOperator(dispatcherWindowOperator, Bundle.getMessage("InitiateTrain") + "...");
        jbo.doClick();
        jbo.getQueueTool().waitEmpty();

        // ActivateTrainFrame now visible
        JFrameOperator activateFrameOper = new JFrameOperator(Bundle.getMessage("AddTrainTitle"));
        assertNotNull(activateFrameOper);

        // JUnitUtil.waitFor(10000);

        JButtonOperator jboCancel = new JButtonOperator(activateFrameOper, "Cancel");
        jboCancel.doClick();
        jboCancel.getQueueTool().waitEmpty();

        // tidy-up DispatcherFrame
        JUnitUtil.dispose(dispatcherWindowOperator.getWindow());
        dispatcherWindowOperator.waitClosed();

    }

    private JFrameOperator launchLayoutAndGetDispatcher() {

        // load layout file
        ConfigXmlManager cm = new ConfigXmlManager();
        java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml");
        assertDoesNotThrow( () -> cm.load(f));

        InstanceManager.getDefault(LayoutBlockManager.class).initializeLayoutBlockPaths();
        JUnitUtil.waitFor( () -> InstanceManager.getDefault(LayoutBlockManager.class).routingStablised(),
            "Routing Stabilised");

        // load dispatcher options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
        InstanceManager.getDefault(DispatcherFrame.class).setAutoAllocate(false);
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        JFrameOperator leOperator = new JFrameOperator("Test Layout");
        assertNotNull(leOperator, "LE Panel exists");

        JFrameOperator dispatcherWindowOperator = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        assertNotNull(dispatcherWindowOperator,"Dispatcher Window Exists");
        return dispatcherWindowOperator;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.closeAllPanels(); // closes LE Frame
        InstanceManager.getDefault(SignalMastManager.class).dispose();
        InstanceManager.getDefault(SignalMastLogicManager.class).dispose();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ActivateTrainFrameTest.class);
}
