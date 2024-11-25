package jmri.jmrix.openlcb.swing;

import jmri.jmrix.openlcb.OlcbSystemConnectionMemoScaffold;
import jmri.*;
import jmri.jmrit.beantable.signalmast.*;
import jmri.jmrix.openlcb.*;
import jmri.implementation.*;
import jmri.util.*;

import java.util.*;

import javax.swing.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.openlcb.*;

/**
 * @author Bob Jacobsen Copyright 2018
 */
public class OlcbSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() { return new OlcbSignalMastAddPane(); }

    @Test
    public void testSetMast() {
        OlcbSignalMast s1 = new OlcbSignalMast("MF$olm:basic:one-searchlight($0001)", "user name");
        OlcbSignalMast s2 = new OlcbSignalMast("SF$olm:basic:one-low($0002)", "user name");
        OlcbSignalMast s3 = new OlcbSignalMast("M3F$olm:basic:two-searchlight($0003)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();

        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertTrue(vp.canHandleMast(s2));
        Assert.assertTrue(vp.canHandleMast(s3));
        Assert.assertFalse(vp.canHandleMast(m1));

        vp.setMast(null);

        SignalSystemManager ssm = InstanceManager.getDefault(SignalSystemManager.class);
        Assert.assertNotNull(ssm);
        SignalSystem ss = ssm.getSystem("basic");
        Assert.assertNotNull(ss);

        vp.setAspectNames(s1.getAppearanceMap(), ss );
        vp.setMast(s1);
        vp.setAspectNames(s2.getAppearanceMap(), ss );
        vp.setMast(s2);
        vp.setAspectNames(s3.getAppearanceMap(), ss );
        vp.setMast(s3);

        vp.setAspectNames(m1.getAppearanceMap(), ss );
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

        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        Assert.assertNotNull(smm );
        SignalMast sm = smm.getByUserName("user name");
        Assert.assertNotNull(sm );
        Assert.assertEquals("PL-2-high", sm.getMastType());
        Assert.assertNotNull(smm.getBySystemName("MF$olm:AAR-1946:PL-2-high($0005)"));

    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCreateAndDisableViaGui() throws java.beans.PropertyVetoException {

        SignalMastManager mgr = InstanceManager.getDefault(SignalMastManager.class);
        for (SignalMast m : mgr.getNamedBeanSet()) mgr.deleteBean(m, "DoDelete");
        Assert.assertEquals(0, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());

        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();
        SignalSystemManager ssm = InstanceManager.getDefault(SignalSystemManager.class);
        Assert.assertNotNull(ssm);
        SignalSystem ss = ssm.getSystem("basic");
        Assert.assertNotNull(ss);

        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                @Override
                public Enumeration<String> getAspects() {
                    return java.util.Collections.enumeration(
                        java.util.Arrays.asList("Clear","Approach Medium","Advance Approach",
                                "Medium Clear", "Approach", "Slow Approach", "Permissive", "Restricting", "Stop and Proceed", "Stop"));
                    }
            }
                , ss );

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
        ThreadingUtil.runOnGUI(() -> {
            var approachMed = vp.allAspectsCheckBoxes.get("Approach Medium");
            Assert.assertNotNull(approachMed);
            approachMed.setSelected(true);

            var clear = vp.aspectEventIDs.get("Clear");
            Assert.assertNotNull(clear);
            clear.setText("01.02.03.04.05.06.07.08");

            vp.litEventID.setText(    "03.02.01.01.01.01.01.01");
            vp.notLitEventID.setText( "04.02.01.01.01.01.01.01");
            vp.heldEventID.setText(   "05.02.01.01.01.01.01.01");
            vp.notHeldEventID.setText("06.02.01.01.01.01.01.01");

            vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name 1");
        });

        // check list of SignalMasts
        Assert.assertEquals(1, mgr.getObjectCount());
        SignalMast sm1 = mgr.getByUserName("user name 1");
        Assert.assertNotNull(sm1);
        // system name not checked, depends on history of how many SignalMast objects have been created

        // check aspect disabled
        Assert.assertTrue(sm1.isAspectDisabled("Approach Medium"));
        Assert.assertFalse(sm1.isAspectDisabled("Clear"));

        // check correct eventid present
        OlcbSignalMast foundMast = (OlcbSignalMast)sm1;
        Assert.assertEquals(new OlcbAddress("01.02.03.04.05.06.07.08"), new OlcbAddress(foundMast.getOutputForAppearance("Clear")));

        Assert.assertEquals(new OlcbAddress("03.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getLitEventId()));
        Assert.assertEquals(new OlcbAddress("04.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotLitEventId()));
        Assert.assertEquals(new OlcbAddress("05.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getHeldEventId()));
        Assert.assertEquals(new OlcbAddress("06.02.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotHeldEventId()));

        ThreadingUtil.runOnGUI(frame::dispose);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEditAndDisableViaGui() {

        Assert.assertEquals(0, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        OlcbSignalMast mast = new OlcbSignalMast("MF$olm:basic:one-searchlight($0001)", "user name 2");
        mast.setOutputForAppearance("Approach", "01.01.01.01.01.01.01.01");
        mast.setLitEventId("03.01.01.01.01.01.01.01");
        mast.setNotLitEventId("04.01.01.01.01.01.01.01");
        mast.setHeldEventId("05.01.01.01.01.01.01.01");
        mast.setNotHeldEventId("06.01.01.01.01.01.01.01");

        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        smm.register(mast);

        Assert.assertEquals(1, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        mast.setAspectDisabled("Stop");
        mast.setAspectDisabled("Unlit"); // we will renable this below

        OlcbSignalMastAddPane vp = new OlcbSignalMastAddPane();

        SignalSystemManager ssm = InstanceManager.getDefault(SignalSystemManager.class);
        Assert.assertNotNull(ssm);
        SignalSystem ss = ssm.getSystem("basic");
        Assert.assertNotNull(ss);

        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                @Override
                public Enumeration<String> getAspects() { return mast.getAllKnownAspects().elements(); }
            }
                , ss);
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
        ThreadingUtil.runOnGUI(() -> {
            var approach = vp.allAspectsCheckBoxes.get("Approach");
            Assert.assertNotNull(approach);
            approach.setSelected(true);
            
            var unlit = vp.allAspectsCheckBoxes.get("Unlit");
            Assert.assertNotNull(unlit);
            unlit.setSelected(false);

            var clear = vp.aspectEventIDs.get("Clear");
            Assert.assertNotNull(clear);
            clear.setText("01.02.03.04.05.06.07.08");

            vp.createMast("basic", "appearance-one-searchlight.xml", "user name 1");
        });

        // check list of SignalMasts
        Assert.assertEquals(1, smm.getObjectCount());
        SignalMast sm2 = smm.getByUserName("user name 2");
        Assert.assertNotNull(sm2);
        // system name not checked, depends on history of how many SignalMast objects have been created

        // check correct aspects disabled
        Assert.assertFalse(sm2.isAspectDisabled("Clear"));
        Assert.assertTrue(sm2.isAspectDisabled("Approach"));
        Assert.assertTrue(sm2.isAspectDisabled("Stop"));
        Assert.assertFalse(sm2.isAspectDisabled("Unlit"));

        // check correct eventid present
        OlcbSignalMast foundMast = (OlcbSignalMast)sm2;
        Assert.assertEquals(new OlcbAddress("00.00.00.00.00.00.00.00"), new OlcbAddress(foundMast.getOutputForAppearance("Stop")));
        Assert.assertEquals(new OlcbAddress("01.02.03.04.05.06.07.08"), new OlcbAddress(foundMast.getOutputForAppearance("Clear")));
        Assert.assertEquals(new OlcbAddress("01.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getOutputForAppearance("Approach")));

        Assert.assertEquals(new OlcbAddress("03.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getLitEventId()));
        Assert.assertEquals(new OlcbAddress("04.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotLitEventId()));
        Assert.assertEquals(new OlcbAddress("05.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getHeldEventId()));
        Assert.assertEquals(new OlcbAddress("06.01.01.01.01.01.01.01"), new OlcbAddress(foundMast.getNotHeldEventId()));

        ThreadingUtil.runOnGUI(frame::dispose);
    }

    // TODO: GUI test of icons in Add/Edit pane

    // from here down is testing infrastructure
    private static OlcbSystemConnectionMemoScaffold memo;
    private static OlcbSystemConnectionMemoScaffold memo1;
    private static OlcbSystemConnectionMemoScaffold memo2;
    static Connection connection;
    static NodeID nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
    static NodeID nodeID1 = new NodeID(new byte[]{2, 0, 0, 0, 0, 0});
    static NodeID nodeID2 = new NodeID(new byte[]{3, 0, 0, 0, 0, 0});
    static java.util.ArrayList<Message> messages;

    private static void resetMessages(){
        messages = new java.util.ArrayList<>();
    }

    //
    // This only initialized JUnit and Log4J once per class so that it
    // can only initialize the OpenLCB structure once per class
    @BeforeEach
    @Override
    public void setUp() {
        resetMessages();
    }

    @BeforeAll
    @SuppressWarnings("deprecation") // OlcbInterface(NodeID, Connection)
    static public void preClassInit() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
        nodeID1 = new NodeID(new byte[]{2, 0, 0, 0, 0, 0});
        nodeID2 = new NodeID(new byte[]{3, 0, 0, 0, 0, 0});

        messages = new java.util.ArrayList<>();
        connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
                messages.add(msg);
            }
        };

        // Enable multiple OpenLCB connections for tests.
        memo = new OlcbSystemConnectionMemoScaffold(); // this self-registers as 'M'
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.setInterface(new OlcbInterface(nodeID, connection) {
            @Override
            public Connection getOutputConnection() {
                return connection;
            }
        });
        memo1 = new OlcbSystemConnectionMemoScaffold("S");
        memo1.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo1.setInterface(new OlcbInterface(nodeID1, connection) {
            @Override
            public Connection getOutputConnection() {
                return connection;
            }
        });
        memo2 = new OlcbSystemConnectionMemoScaffold("M3");
        memo2.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo2.setInterface(new OlcbInterface(nodeID2, connection) {
            @Override
            public Connection getOutputConnection() {
                return connection;
            }
        });

        JUnitUtil.waitFor(()-> (!messages.isEmpty()),"Initialization Complete message");
    }

    @AfterEach
    @Override
    public void tearDown() {
    }

    @AfterAll
    public static void postClassTearDown() {
        if(memo != null && memo.getInterface() !=null ) {
           memo.getInterface().dispose();
        }
        if(memo1 != null && memo1.getInterface() !=null ) {
           memo1.getInterface().dispose();
        }
        if(memo2 != null && memo2.getInterface() !=null ) {
           memo2.getInterface().dispose();
        }
        memo = null;
        memo1 = null;
        memo2 = null;
        connection = null;
        nodeID = null;
        nodeID1 = null;
        nodeID2 = null;
        JUnitUtil.tearDown();
    }
}

