package jmri.implementation;

import java.awt.GraphicsEnvironment;
import jmri.Block;
import jmri.BlockManager;
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
import org.junit.*;


/**
 *
 * @author Paul Bender Copyright (C) 2019	
 */
public class DefaultCabSignalTest {

    @Test
    public void testCTor() {
        DefaultCabSignal cs = new DefaultCabSignal(new DccLocoAddress(1234,true));
        Assert.assertNotNull("exists",cs);
        //check the defaults.
        Assert.assertEquals("Address",new DccLocoAddress(1234,true),cs.getCabSignalAddress());
        Assert.assertNull("current block",cs.getBlock());
        Assert.assertNull("next block",cs.getNextBlock());
        Assert.assertNull("next mast",cs.getNextMast());
        Assert.assertTrue("cab signal active",cs.isCabSignalActive());
        cs.dispose(); // verify no exceptions
    }

    @Test
    public void testSetBlock() {
        DefaultCabSignal cs = new DefaultCabSignal(new DccLocoAddress(1234,true)){
            @Override
            public jmri.SignalMast getNextMast(){
               // don't check for signal masts, they aren't setup for this
               // test.
               return null;
            }
        };

        Block b1 = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB12");
        // set the block contents to our locomotive address.
        b1.setValue(new DccLocoAddress(1234,true));
        // call setBlock() for the cab signal.
        cs.setBlock();
        // and verify getBlock returns the block we set.
        Assert.assertEquals("Block set",b1,cs.getBlock());

        cs.dispose(); // verify no exceptions
    }

    @Test
    public void testSignalSequence() throws jmri.JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        // load and display test panel file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml");
        cm.load(f);

        // Find new window by name (should be more distinctive, comes from sample file)
        EditorFrameOperator to = new EditorFrameOperator("Cab Signal Test");
        LayoutEditor le = (LayoutEditor) jmri.util.JmriJFrame.getFrame("Cab Signal Test");
        InstanceManager.getDefault(jmri.jmrit.display.PanelMenu.class).addEditorPanel(le);
        jmri.SignalMastLogicManager smlm = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);
        smlm.initialise();
        for(jmri.SignalMastLogic sml:smlm.getSignalMastLogicList()) {
            sml.setupLayoutEditorDetails();
        }

        // Panel is up, continue set up for tests.
        ConnectivityUtil cu = new ConnectivityUtil(le);
        Assert.assertNotNull("connectivity util",cu);

        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        // make sure the block paths are initialized.
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        
        BlockManager bm = InstanceManager.getDefault(jmri.BlockManager.class);
        SensorManager sm = InstanceManager.getDefault(jmri.SensorManager.class);
        TurnoutManager tm = InstanceManager.getDefault(jmri.TurnoutManager.class);
        SignalMastManager smm = InstanceManager.getDefault(jmri.SignalMastManager.class);

        tm.provideTurnout("EastTurnout").setState(Turnout.CLOSED);
        tm.provideTurnout("WestTurnout").setState(Turnout.CLOSED);

        sm.provideSensor("Mainline").setState(Sensor.ACTIVE);
        sm.provideSensor("Siding").setState(Sensor.INACTIVE);
        sm.provideSensor("EastTurnoutOS").setState(Sensor.INACTIVE);
        sm.provideSensor("East1").setState(Sensor.INACTIVE);
        sm.provideSensor("East2").setState(Sensor.INACTIVE);
        sm.provideSensor("WestTurnoutOS").setState(Sensor.INACTIVE);
        sm.provideSensor("West1").setState(Sensor.INACTIVE);
        sm.provideSensor("West2").setState(Sensor.INACTIVE);

        Block b1 = bm.provideBlock("MainlineBlock");
        // set the block contents to our locomotive address.
        b1.setValue(new DccLocoAddress(1234,true));

        // setup the cab signal.

        DefaultCabSignal cs = new DefaultCabSignal(new DccLocoAddress(1234,true));
        // get the initial block for the cab signal.
        cs.setBlock();

        // and verify getBlock returns the block we set.
        Assert.assertEquals("Block set",b1,cs.getBlock());
        Assert.assertEquals("next Block set",bm.getBlock("EastTurnoutOSBlock"),cs.getNextBlock());
        Assert.assertEquals("Mast set",smm.getSignalMast("IF$vsm:AAR-1946:PL-1-high-abs($0005)"),cs.getNextMast());
        Assert.assertEquals("Mast Aspect clear","Clear",cs.getNextMast().getAspect());

        // use sensors to move to the next block.
        sm.provideSensor("EastTurnoutOS").setState(Sensor.ACTIVE);
        sm.provideSensor("Mainline").setState(Sensor.INACTIVE);
        Assert.assertEquals("Block set",bm.getBlock("EastTurnoutOSBlock"),cs.getBlock());
        Assert.assertEquals("next Block set",bm.getBlock("East1Block"),cs.getNextBlock());
        Assert.assertEquals("Mast set",smm.getSignalMast("IF$vsm:AAR-1946:PL-1-high-pbs($0002)"),cs.getNextMast());
        Assert.assertEquals("Mast Aspect clear","Clear",cs.getNextMast().getAspect());


        cs.dispose(); // verify no exceptions

        // and close the window
        to.closeFrameWithConfirmations();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
        InstanceManager.setDefault(jmri.jmrit.display.PanelMenu.class,new jmri.jmrit.display.PanelMenu());
        JUnitUtil.initShutDownManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(DefaultCabSignalTest.class);

}
