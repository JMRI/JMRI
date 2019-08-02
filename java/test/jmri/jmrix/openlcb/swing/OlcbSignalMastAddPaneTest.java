package jmri.jmrix.openlcb.swing;

import jmri.*;
import jmri.jmrit.beantable.signalmast.*;
import jmri.jmrix.openlcb.*;
import jmri.implementation.*;
import jmri.util.*;

import java.util.*;
import javax.swing.*;

import org.junit.*;

import org.netbeans.jemmy.operators.*;

import org.openlcb.*;

/**
 * @author	Bob Jacobsen Copyright 2018
 */
public class OlcbSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() { return new OlcbSignalMastAddPane(); }    
    
    @Test
    public void testSetMast() {
        OlcbSignalMast s1 = new OlcbSignalMast("MF$olm:basic:one-searchlight($0001)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        
        vp.setAspectNames(s1.getAppearanceMap(), InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(s1);
        
        vp.setAspectNames(m1.getAppearanceMap(), InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");

    }
    
    @Test
    public void testCanHandleMast() {
        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        SignalMast mast = new OlcbSignalMast("MF$olm:basic:one-searchlight($1)", "no user name"){
            { setLastRef(4); } // reset references
        };
        Assert.assertTrue(vp.canHandleMast(mast));
        
        Assert.assertFalse(vp.canHandleMast(new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($0001)")));
        
    }

    @Test
    public void testCreateMast() {
        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        new OlcbSignalMast("MF$olm:basic:one-searchlight($1)", "no user name"){
            { setLastRef(4); } // reset references - this leads to ($0005) below, just in case anybody else has created one
        };
        
        vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name");
                
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name"));
        Assert.assertEquals("PL-2-high", InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name").getMastType());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName("MF$olm:AAR-1946:PL-2-high($0005)"));
        
    }

    @Test
    public void testCreateAndDisableViaGui() throws java.beans.PropertyVetoException {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        SignalMastManager mgr = InstanceManager.getDefault(SignalMastManager.class);
        for (SignalMast m : mgr.getNamedBeanSet()) mgr.deleteBean(m, "DoDelete");
        Assert.assertEquals(0, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        
        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();

        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                @Override
                public Enumeration<String> getAspects() {
                    return java.util.Collections.enumeration(
                        java.util.Arrays.asList(
                            new String[]{"Clear","Approach Medium","Advance Approach",
                                    "Medium Clear", "Approach", "Slow Approach",
                                    "Permissive", "Restricting", "Stop and Proceed", "Stop"}));
                    }
            }
                , InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic") );
        
        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        frame.pack();
        frame.setVisible(true);
        
        // check load
        Assert.assertEquals("00.00.00.00.00.00.00.00", vp.litEventID.getText());
        Assert.assertEquals("00.00.00.00.00.00.00.00", vp.notLitEventID.getText());
        Assert.assertEquals("00.00.00.00.00.00.00.00", vp.heldEventID.getText());
        Assert.assertEquals("00.00.00.00.00.00.00.00", vp.notHeldEventID.getText());

        // disable Approach Medim, change some of the event IDs
        // then build the mast, all on Swing thread
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            vp.disabledAspects.get("Approach Medium").setSelected(true);

            vp.aspectEventIDs.get("Clear").setText("01.02.03.04.05.06.07.08");
            
            vp.litEventID.setText(    "03.02.01.01.01.01.01.01");
            vp.notLitEventID.setText( "04.02.01.01.01.01.01.01");
            vp.heldEventID.setText(   "05.02.01.01.01.01.01.01");
            vp.notHeldEventID.setText("06.02.01.01.01.01.01.01");
            
            vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name 1");
        });

        // check list of SignalMasts
        Assert.assertEquals(1, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1"));
        // system name not checked, depends on history of how many SignalMast objects have been created

        // check aspect disabled
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1").isAspectDisabled("Approach Medium"));
        Assert.assertTrue(! InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1").isAspectDisabled("Clear"));

        // check correct eventid present
        OlcbSignalMast foundMast = (OlcbSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1");
        Assert.assertEquals(new OlcbAddress("01.02.03.04.05.06.07.08"), new OlcbAddress(foundMast.getOutputForAppearance("Clear")));

        Assert.assertEquals(new OlcbAddress("03.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getLitEventId()));
        Assert.assertEquals(new OlcbAddress("04.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotLitEventId()));
        Assert.assertEquals(new OlcbAddress("05.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getHeldEventId()));
        Assert.assertEquals(new OlcbAddress("06.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotHeldEventId()));

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.dispose();
        });
    }

    @Test
    public void testEditAndDisableViaGui() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        Assert.assertEquals(0, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        OlcbSignalMast mast = new OlcbSignalMast("MF$olm:basic:one-searchlight($0001)", "user name 2");
        mast.setOutputForAppearance("Approach", "01.01.01.01.01.01.01.01");
        mast.setLitEventId("03.01.01.01.01.01.01.01");
        mast.setNotLitEventId("04.01.01.01.01.01.01.01");
        mast.setHeldEventId("05.01.01.01.01.01.01.01");
        mast.setNotHeldEventId("06.01.01.01.01.01.01.01");
        InstanceManager.getDefault(jmri.SignalMastManager.class).register(mast);
        
        Assert.assertEquals(1, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        mast.setAspectDisabled("Stop");
        mast.setAspectDisabled("Unlit"); // we will renable this below
        
        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        
        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                @Override
                public Enumeration<String> getAspects() { return mast.getAllKnownAspects().elements(); }
            }
                , InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(mast);
              
        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        frame.pack();
        frame.setVisible(true);
        
        // check load
        Assert.assertEquals(new OlcbAddress("03.01.01.01.01.01.01.01"), new OlcbAddress(vp.litEventID.getText()));
        Assert.assertEquals(new OlcbAddress("04.01.01.01.01.01.01.01"), new OlcbAddress(vp.notLitEventID.getText()));
        Assert.assertEquals(new OlcbAddress("05.01.01.01.01.01.01.01"), new OlcbAddress(vp.heldEventID.getText()));
        Assert.assertEquals(new OlcbAddress("06.01.01.01.01.01.01.01"), new OlcbAddress(vp.notHeldEventID.getText()));
        
        // disable Approach, change some of the event IDs
        // then build the mast, all on Swing thread
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            vp.disabledAspects.get("Approach").setSelected(true);
            vp.disabledAspects.get("Unlit").setSelected(false);

            vp.aspectEventIDs.get("Clear").setText("01.02.03.04.05.06.07.08");
            
            vp.createMast("basic", "appearance-one-searchlight.xml", "user name 1");
        });

        // check list of SignalMasts
        Assert.assertEquals(1, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2"));
        // system name not checked, depends on history of how many SignalMast objects have been created

        // check correct aspects disabled
        Assert.assertTrue(! InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Clear"));
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Approach"));
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Stop"));
        Assert.assertTrue(! InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Unlit"));

        // check correct eventid present
        OlcbSignalMast foundMast = (OlcbSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2");
        Assert.assertEquals(new OlcbAddress("00.00.00.00.00.00.00.00"), new OlcbAddress(foundMast.getOutputForAppearance("Stop")));
        Assert.assertEquals(new OlcbAddress("01.02.03.04.05.06.07.08"), new OlcbAddress(foundMast.getOutputForAppearance("Clear")));
        Assert.assertEquals(new OlcbAddress("01.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getOutputForAppearance("Approach")));

        Assert.assertEquals(new OlcbAddress("03.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getLitEventId()));
        Assert.assertEquals(new OlcbAddress("04.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotLitEventId()));
        Assert.assertEquals(new OlcbAddress("05.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getHeldEventId()));
        Assert.assertEquals(new OlcbAddress("06.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotHeldEventId()));

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.dispose();
        });
    }


    // from here down is testing infrastructure
    private static OlcbSystemConnectionMemo memo;
    static Connection connection;
    static NodeID nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
    static java.util.ArrayList<Message> messages;
    
    // The minimal setup for log4J
    //
    // This only initialized JUnit and Log4J once per class so that it
    // can only initialize the OpenLCB structure once per class
    @Before
    @Override
    public void setUp() {
        messages = new java.util.ArrayList<>();
    }

    @BeforeClass
    static public void preClassInit() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
        
        messages = new java.util.ArrayList<>();
        connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
                messages.add(msg);
            }
        };

        memo = new OlcbSystemConnectionMemo(); // this self-registers as 'M'
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.setInterface(new OlcbInterface(nodeID, connection) {
            @Override
            public Connection getOutputConnection() {
                return connection;
            }
        });
        
        jmri.util.JUnitUtil.waitFor(()->{return (messages.size()>0);},"Initialization Complete message");
    }

    @After
    @Override
    public void tearDown() {
        messages = null;
    }

    @AfterClass
    public static void postClassTearDown() throws Exception {
        if(memo != null && memo.getInterface() !=null ) {
           memo.getInterface().dispose();
        }
        memo = null;
        connection = null;
        nodeID = null;
        JUnitUtil.tearDown();
    }
}

