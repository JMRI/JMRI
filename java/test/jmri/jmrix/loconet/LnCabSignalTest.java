package jmri.jmrix.loconet;

import java.awt.GraphicsEnvironment;
import jmri.Block;
import jmri.BlockManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import jmri.util.ThreadingUtil;
import org.junit.*;


/**
 *
 * @author Paul Bender Copyright (C) 2019	
 */
public class LnCabSignalTest extends jmri.implementation.DefaultCabSignalTest {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Override
    @Test
    public void testSignalSequenceIdTag() throws jmri.JmriException {
        // since this is on loconet, use a transponding tag.
        runSequence(new TranspondingTag("LD1234"));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        InstanceManager.setDefault(jmri.BlockManager.class,new jmri.BlockManager());
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        InstanceManager.setDefault(jmri.jmrit.display.PanelMenu.class,new jmri.jmrit.display.PanelMenu());
        JUnitUtil.initShutDownManager();

	// prepare an interface
        memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        cs = new LnCabSignal(memo,new DccLocoAddress(1234,true));
    }

    @Override
    @After
    public void tearDown() {
        cs.dispose(); // verify no exceptions
        cs = null;
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(LnCabSignalTest.class);

}
