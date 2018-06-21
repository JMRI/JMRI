package jmri.jmrix.openlcb.swing;

import jmri.*;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
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
public class OlcbSignalMastAddPaneTest {

    @Test
    public void testSetMast() {
        OlcbSignalMast s1 = new OlcbSignalMast("MF$olm:basic:one-searchlight(0001)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        
        Assert.assertFalse(vp.canHandleMast(null));
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        vp.setMast(s1);
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");

    }

    @Test
    public void testCreateMast() {
        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        new OlcbSignalMast("MF$olm:basic:one-searchlight(1)", "no user name"){
            { lastRef = 4; } // reset references - this leads to (0005) below, just in case anybody else has created one
        };
        
        vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name");
                
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName("MF$olm:AAR-1946:PL-2-high(0005)"));
        
    }

    @Test
    public void testCreateAndDisableViaGui() throws java.beans.PropertyVetoException {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        SignalMastManager mgr = InstanceManager.getDefault(SignalMastManager.class);
        for (SignalMast m : mgr.getNamedBeanSet()) mgr.deleteBean(m, "DoDelete");
        Assert.assertEquals(0, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        
        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();

        vp.setAspectNames(
            java.util.Collections.enumeration(
                java.util.Arrays.asList(
                    new String[]{"Clear","Approach Medium","Advance Approach",
                                    "Medium Clear", "Approach", "Slow Approach",
                                    "Permissive", "Restricting", "Stop and Proceed", "Stop"})));
        
        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        frame.pack();
        frame.setVisible(true);
        
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            vp.aspectEventIDs.get("Clear").setText("01.02.03.04.05.06.07.08");
            vp.disabledAspects.get("Approach Medium").setSelected(true);
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
        Assert.assertEquals(new OlcbAddress("01.02.03.04.05.06.07.08"), new OlcbAddress(((OlcbSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1")).getOutputForAppearance("Clear")));

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.dispose();
        });
    }

    @Test
    public void testEditAndDisableViaGui() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        Assert.assertEquals(0, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        OlcbSignalMast mast = new OlcbSignalMast("MF$olm:basic:one-searchlight(0001)", "user name 2");
        mast.setOutputForAppearance("Approach", "01.01.01.01.01.01.01.01");
        InstanceManager.getDefault(jmri.SignalMastManager.class).register(mast);
        
        Assert.assertEquals(1, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        mast.setAspectDisabled("Stop");
        mast.setAspectDisabled("Unlit"); // we will renable this below
        
        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        
        vp.setAspectNames(mast.getAllKnownAspects().elements());
        vp.setMast(mast);
              
        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        frame.pack();
        frame.setVisible(true);
        
        // disable Approach, set Clear value
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            vp.aspectEventIDs.get("Clear").setText("01.02.03.04.05.06.07.08");
            vp.disabledAspects.get("Approach").setSelected(true);
            vp.disabledAspects.get("Unlit").setSelected(false);
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
        Assert.assertEquals(new OlcbAddress("00.00.00.00.00.00.00.00"), new OlcbAddress(((OlcbSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2")).getOutputForAppearance("Stop")));
        Assert.assertEquals(new OlcbAddress("01.02.03.04.05.06.07.08"), new OlcbAddress(((OlcbSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2")).getOutputForAppearance("Clear")));
        Assert.assertEquals(new OlcbAddress("01.01.01.01.01.01.01.01"), new OlcbAddress(((OlcbSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2")).getOutputForAppearance("Approach")));

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
    public void setUp() {
        messages = new java.util.ArrayList<>();
    }

    @BeforeClass
    static public void preClassInit() {
        JUnitUtil.setUp();
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
            public Connection getOutputConnection() {
                return connection;
            }
        });
        
        jmri.util.JUnitUtil.waitFor(()->{return (messages.size()>0);},"Initialization Complete message");
    }

    @After
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

