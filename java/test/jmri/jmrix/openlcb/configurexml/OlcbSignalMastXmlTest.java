package jmri.jmrix.openlcb.configurexml;

import jmri.util.JUnitUtil;
import jmri.jmrix.openlcb.OlcbSignalMast;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;

import org.jdom2.Element;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * OlcbSignalMastXmlTest
 *
 * Test for the OlcbSignalMastXml class
 *
 * @author   Bob Jacobsen Copyright (C) 2018
 */
public class OlcbSignalMastXmlTest {

    private static OlcbSystemConnectionMemo memo;
    static Connection connection;
    static NodeID nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
    static java.util.ArrayList<Message> messages;

    @Test
    public void testStore(){
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs($1)");
        t.setLitEventId("1.2.3.4.5.6.7.1");
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        t.setHeldEventId("1.2.3.4.5.6.7.3");
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        t.setOutputForAppearance("Clear", "1.2.3.4.5.6.7.10");
        t.setOutputForAppearance("Approach", "1.2.3.4.5.6.7.11");
        t.setOutputForAppearance("Permissive", "1.2.3.4.5.6.7.12");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.13");

        OlcbSignalMastXml x = new OlcbSignalMastXml();

        Element e = x.store(t);
        Assert.assertNotNull("Element", e);

        Assert.assertEquals("1.2.3.4.5.6.7.1", e.getChild("lit").getChild("lit").getValue());
        Assert.assertEquals("1.2.3.4.5.6.7.2", e.getChild("lit").getChild("notlit").getValue());
        Assert.assertEquals("1.2.3.4.5.6.7.3", e.getChild("held").getChild("held").getValue());
        Assert.assertEquals("1.2.3.4.5.6.7.4", e.getChild("held").getChild("notheld").getValue());
    }

    @BeforeEach
    public void setUp() {
        messages = new java.util.ArrayList<>();
    }

    @BeforeAll
    @SuppressWarnings("deprecated") // OlcbInterface(NodeID, Connection)
    static public void preClassInit() {
        JUnitUtil.setUp();
       // this test is run separately because it leaves a lot of threads behind
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
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

        jmri.util.JUnitUtil.waitFor(()-> (messages.size()>0),"Initialization Complete message");
    }

    @AfterEach
    public void tearDown() {
        messages = null;
    }

    @AfterAll
    public static void postClassTearDown() {
        if (Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning") == false) {
            if(memo != null && memo.getInterface() !=null ) {
               memo.getInterface().dispose();
            }
            memo = null;
            connection = null;
            nodeID = null;
        }
        JUnitUtil.tearDown();
    }
}

