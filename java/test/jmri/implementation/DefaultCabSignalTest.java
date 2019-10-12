package jmri.implementation;

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
public class DefaultCabSignalTest {

    protected jmri.CabSignal cs = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",cs);
        //check the defaults.
        Assert.assertEquals("Address",new DccLocoAddress(1234,true),cs.getCabSignalAddress());
        Assert.assertNull("current block",cs.getBlock());
        Assert.assertNull("next block",cs.getNextBlock());
        Assert.assertNull("next mast",cs.getNextMast());
        Assert.assertTrue("cab signal active",cs.isCabSignalActive());
    }

    @Test
    public void testSetBlock() {
        DefaultCabSignal acs = new DefaultCabSignal(new DccLocoAddress(1234,true)){
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
        acs.setBlock();
        // and verify getBlock returns the block we set.
        Assert.assertEquals("Block set",b1,acs.getBlock());

        acs.dispose(); // verify no exceptions
    }

    @Test
    public void testSignalSequence() throws jmri.JmriException {
        runSequence(new DccLocoAddress(1234,true));
    }

    @Test
    public void testSignalSequenceIdTag() throws jmri.JmriException {
        runSequence(new DefaultRailCom("ID1234","Test Tag"));
    }
    
    protected void runSequence(Object initialBlockContents) throws jmri.JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display test panel file
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).setStabilisedSensor("IS_ROUTING_DONE");

        java.io.File f = new java.io.File("java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml");
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);

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
        JUnitUtil.waitFor(() -> {
            return InstanceManager.sensorManagerInstance().provideSensor("IS_ROUTING_DONE").getKnownState() == jmri.Sensor.ACTIVE;
        },
                "LayoutEditor stabilized sensor went ACTIVE");
        
        BlockManager bm = InstanceManager.getDefault(jmri.BlockManager.class);
        SensorManager sm = InstanceManager.getDefault(jmri.SensorManager.class);
        TurnoutManager tm = InstanceManager.getDefault(jmri.TurnoutManager.class);

        ThreadingUtil.runOnLayout( ()-> { 
             try{
                tm.provideTurnout("EastTurnout").setState(Turnout.CLOSED);
                tm.provideTurnout("WestTurnout").setState(Turnout.CLOSED);

                sm.provideSensor("Mainline").setState(Sensor.ACTIVE);
                sm.provideSensor("Siding").setState(Sensor.ACTIVE);
                sm.provideSensor("EastTurnoutOS").setState(Sensor.INACTIVE);
                sm.provideSensor("East1").setState(Sensor.INACTIVE);
                sm.provideSensor("East2").setState(Sensor.INACTIVE);
                sm.provideSensor("WestTurnoutOS").setState(Sensor.INACTIVE);
                sm.provideSensor("West1").setState(Sensor.INACTIVE);
                sm.provideSensor("West2").setState(Sensor.INACTIVE);
             } catch (JmriException je) {
                log.error("Expected error setting up test", je);
             }
        });

        Block b1 = bm.provideBlock("MainlineBlock");
        // set the block contents to a railcom address for our locomotive.
        b1.setValue(initialBlockContents);

        // get the initial block for the cab signal.
        cs.setBlock();

        // and verify getBlock returns the block we set.
        checkBlock(cs,"MainlineBlock","EastTurnoutOSBlock","IF$vsm:AAR-1946:PL-1-high-abs($0005)");
        moveBlock("Mainline","EastTurnoutOS");
        checkBlock(cs,"EastTurnoutOSBlock","East1Block","IF$vsm:AAR-1946:PL-1-high-pbs($0002)");
        moveBlock("EastTurnoutOS","East1");
        checkBlock(cs,"East1Block","East2Block","IF$vsm:AAR-1946:PL-1-high-pbs($0002)");
        moveBlock("East1","East2");
        checkBlock(cs,"East2Block","","");

        moveBlock("East2","West2");
        checkBlock(cs,"West2Block","","");
        moveBlock("West2","West1");
        checkBlock(cs,"West1Block","WestTurnoutOSBlock","IF$vsm:AAR-1946:SL-2-high-abs($0008)");
        moveBlock("West1","WestTurnoutOS");
        checkBlock(cs,"WestTurnoutOSBlock","MainlineBlock","IF$vsm:AAR-1946:PL-1-high-abs($0005)");
        moveBlock("WestTurnoutOS","Mainline");
        checkBlock(cs,"MainlineBlock","EastTurnoutOSBlock","IF$vsm:AAR-1946:PL-1-high-abs($0005)");

        //throw the turnout behind the train.
        ThreadingUtil.runOnLayout( ()-> { 
             try{
              tm.provideTurnout("WestTurnout").setState(Turnout.THROWN);
             } catch (JmriException je) {
             }
        });
        // and verify the state does not change.
        checkBlock(cs,"MainlineBlock","EastTurnoutOSBlock","IF$vsm:AAR-1946:PL-1-high-abs($0005)");

        // throw the turnout in front of the train
        ThreadingUtil.runOnLayout( ()-> { 
             try{
              tm.provideTurnout("EastTurnout").setState(Turnout.THROWN);
             } catch (JmriException je) {
             }
        });
        // and verify the state changes.
        checkBlock(cs,"MainlineBlock","","");
        
        cs.dispose(); // verify no exceptions

        // and close the editor window
        to.closeFrameWithConfirmations();
    }

    private void moveBlock(String startingBlock,String endingBlock) {
        // use sensors to move to the next block.
        ThreadingUtil.runOnLayout( ()-> { 
             try{
                 SensorManager sm = InstanceManager.getDefault(jmri.SensorManager.class);
                 sm.provideSensor(endingBlock).setState(Sensor.ACTIVE);
                 sm.provideSensor(startingBlock).setState(Sensor.INACTIVE); 
             } catch (JmriException je) {
             }
        });
    }

    protected void checkBlock(jmri.CabSignal lcs,String currentBlock,String nextBlock,String mastName){
        BlockManager bm = InstanceManager.getDefault(jmri.BlockManager.class);
        SignalMastManager smm = InstanceManager.getDefault(jmri.SignalMastManager.class);
        Assert.assertEquals("Block set",bm.getBlock(currentBlock),lcs.getBlock());
        Assert.assertEquals("next Block set",bm.getBlock(nextBlock),lcs.getNextBlock());
        Assert.assertEquals("Mast set",smm.getSignalMast(mastName),lcs.getNextMast());
        if(mastName!="") {
           new org.netbeans.jemmy.QueueTool().waitEmpty(100); // wait for signal to settle.
           // mast expected, so check the aspect.
           JUnitUtil.waitFor( () -> { return "Clear".equals(lcs.getNextMast().getAspect().toString());});
           Assert.assertEquals("Mast " + mastName + " Aspect clear","Clear",lcs.getNextMast().getAspect());
        }
    }

    // The minimal setup for log4J
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
        cs = new DefaultCabSignal(new DccLocoAddress(1234,true));
    }

    @After
    public void tearDown() {
        cs.dispose(); // verify no exceptions
        cs = null;
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultCabSignalTest.class);

}
