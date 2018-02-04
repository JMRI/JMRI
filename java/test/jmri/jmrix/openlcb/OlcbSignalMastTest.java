package jmri.jmrix.openlcb;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.ConsumerIdentifiedMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.IdentifyEventsMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the OlcbSignalMast implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2013, 2017, 2018
 * updated to JUnit4 2016
 */
public class OlcbSignalMastTest {

    @Test
    public void testCtor1() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");

        Assert.assertEquals("system name", "MF$olm:AAR-1946:PL-1-high-abs(1)", t.getSystemName());
    }

    @Test
    public void testStopAspect() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.8");

        Assert.assertEquals("Stop aspect event", "x0102030405060708", t.getOutputForAppearance("Stop"));
    }

    @Test
    public void testSetGetEvents() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");
        
        t.setLitEventId("1.2.3.4.5.6.7.1");
        Assert.assertEquals("lit", "x0102030405060701", t.getLitEventId());
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        Assert.assertEquals("not lit", "x0102030405060702", t.getNotLitEventId());

        t.setHeldEventId("1.2.3.4.5.6.7.3");
        Assert.assertEquals("held", "x0102030405060703", t.getHeldEventId());
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        Assert.assertEquals("lit", "x0102030405060704", t.getNotHeldEventId());  
    }
 
    @Test
    public void testReceiveLitPcerMessage() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");

        t.setLitEventId("1.2.3.4.5.6.7.1");
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        t.setHeldEventId("1.2.3.4.5.6.7.3");
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        t.setOutputForAppearance("Clear", "1.2.3.4.5.6.7.10");
        t.setOutputForAppearance("Approach", "1.2.3.4.5.6.7.11");
        t.setOutputForAppearance("Permissive", "1.2.3.4.5.6.7.12");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.13");
        
        Assert.assertEquals("lit defaults true", true, t.getLit());
        
        org.openlcb.Message msg;
        msg = new org.openlcb.ProducerConsumerEventReportMessage(new NodeID(), new OlcbAddress("1.2.3.4.5.6.7.2").toEventID());
        t.handleMessage(msg);
        Assert.assertEquals("lit false", false, t.getLit());
        
        msg = new org.openlcb.ProducerConsumerEventReportMessage(new NodeID(), new OlcbAddress("1.2.3.4.5.6.7.1").toEventID());
        t.handleMessage(msg);
        Assert.assertEquals("lit true", true, t.getLit());                           

        Assert.assertEquals("none sent", 0, messages.size());
    }

    @Test
    public void testReceiveIdProducerLitMessage() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");

        t.setLitEventId("1.2.3.4.5.6.7.1");
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        t.setHeldEventId("1.2.3.4.5.6.7.3");
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        t.setOutputForAppearance("Clear", "1.2.3.4.5.6.7.10");
        t.setOutputForAppearance("Approach", "1.2.3.4.5.6.7.11");
        t.setOutputForAppearance("Permissive", "1.2.3.4.5.6.7.12");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.13");
        
        Assert.assertEquals("lit defaults true", true, t.getLit());
        
        org.openlcb.Message msg;
        msg = new org.openlcb.IdentifyProducersMessage(new NodeID(), new OlcbAddress("1.2.3.4.5.6.7.2").toEventID());
        t.handleMessage(msg);

        Assert.assertEquals("lit still true", true, t.getLit());

        Assert.assertEquals("reply sent", 1, messages.size());
    }

    @Test
    public void testReceiveLitProducerIdMessage() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");

        t.setLitEventId("1.2.3.4.5.6.7.1");
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        t.setHeldEventId("1.2.3.4.5.6.7.3");
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        t.setOutputForAppearance("Clear", "1.2.3.4.5.6.7.10");
        t.setOutputForAppearance("Approach", "1.2.3.4.5.6.7.11");
        t.setOutputForAppearance("Permissive", "1.2.3.4.5.6.7.12");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.13");
        
        Assert.assertEquals("lit defaults true", true, t.getLit());
        
        org.openlcb.Message msg;
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress(t.getNotLitEventId()).toEventID(), org.openlcb.EventState.Invalid);
        t.handleMessage(msg);
        Assert.assertEquals("lit true", true, t.getLit()); // default
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress(t.getNotLitEventId()).toEventID(), org.openlcb.EventState.Unknown);
        t.handleMessage(msg);
        Assert.assertEquals("lit true", true, t.getLit());
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress("FF.2.3.4.5.6.7.2").toEventID(), org.openlcb.EventState.Valid); // wrong event
        t.handleMessage(msg);
        Assert.assertEquals("lit true", true, t.getLit());
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress(t.getNotLitEventId()).toEventID(), org.openlcb.EventState.Valid);
        t.handleMessage(msg);
        Assert.assertEquals("lit false", false, t.getLit());
        
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress(t.getLitEventId()).toEventID(), org.openlcb.EventState.Invalid);
        t.handleMessage(msg);
        Assert.assertEquals("lit false", false, t.getLit());                           
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress(t.getLitEventId()).toEventID(), org.openlcb.EventState.Unknown);
        t.handleMessage(msg);
        Assert.assertEquals("lit false", false, t.getLit());                           
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress("FF.2.3.4.5.6.7.1").toEventID(), org.openlcb.EventState.Valid); // wrong event
        t.handleMessage(msg);
        Assert.assertEquals("lit false", false, t.getLit());                           
        msg = new org.openlcb.ProducerIdentifiedMessage(new NodeID(), new OlcbAddress(t.getLitEventId()).toEventID(), org.openlcb.EventState.Valid);
        t.handleMessage(msg);
        Assert.assertEquals("lit true", true, t.getLit());                           

        Assert.assertEquals("none sent", 0, messages.size());
    }

    enum States2 { A, B }
    @Test
    public void testStateMachine2Setup() {
        
        OlcbSignalMast.StateMachine<States2> machine = new OlcbSignalMast.StateMachine<>(connection, nodeID, States2.B);
        
        Assert.assertEquals("starting state", States2.B, machine.getState());
        
        machine.setEventForState(States2.A, new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0}));
        machine.setEventForState(States2.B, new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0}));

        Assert.assertEquals("A event", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0}), machine.getEventForState(States2.A));
        Assert.assertEquals("B event", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0}), machine.getEventForState(States2.B));
        
        machine.setState(States2.A);
        Assert.assertEquals("still starting state", States2.B, machine.getState());
        Assert.assertEquals("one sent", 1, messages.size());

        machine.handleProducerConsumerEventReport( new ProducerConsumerEventReportMessage(nodeID, new EventID(new byte[]{2, 0, 0, 0, 0, 0, 1, 0})), null); // other eventID
        Assert.assertEquals("still starting state", States2.B, machine.getState());

        machine.handleProducerConsumerEventReport( new ProducerConsumerEventReportMessage(nodeID, new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0})), null); // A eventID
        Assert.assertEquals("new state", States2.A, machine.getState());
    }
 
    @Test
    public void testStateMachine2IdEvents() {
        
        OlcbSignalMast.StateMachine<States2> machine = new OlcbSignalMast.StateMachine<>(connection, nodeID, States2.B);
        
        machine.setEventForState(States2.A, new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0}));
        machine.setEventForState(States2.B, new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0}));

        Assert.assertEquals("none sent", 0, messages.size());
        
        machine.handleIdentifyEvents( new IdentifyEventsMessage(new NodeID(), new NodeID()), null); 
        Assert.assertEquals("no reply if wrong address", 0, messages.size());
        
        machine.handleIdentifyEvents( new IdentifyEventsMessage(new NodeID(), nodeID), null); 
        Assert.assertEquals("four sent", 4, messages.size());
        // check by string comparison as a short cut
        Assert.assertEquals("msg 0", "01.00.00.00.00.00                     Consumer Identified Unknown for EventID:01.00.00.00.00.00.01.00", messages.get(0).toString());
        Assert.assertEquals("msg 1", "01.00.00.00.00.00                     Producer Identified Unknown for EventID:01.00.00.00.00.00.01.00", messages.get(1).toString());
        Assert.assertEquals("msg 2", "01.00.00.00.00.00                     Consumer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(2).toString());
        Assert.assertEquals("msg 3", "01.00.00.00.00.00                     Producer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(3).toString());

        messages = new java.util.ArrayList<>();

        machine.handleIdentifyProducers( new IdentifyProducersMessage(new NodeID(), new EventID(new byte[]{11, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("no reply", 0, messages.size());

        machine.handleIdentifyProducers( new IdentifyProducersMessage(new NodeID(), new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("one sent", 1, messages.size());
        // check by string comparison as a short cut
        Assert.assertEquals("reply", "01.00.00.00.00.00                     Producer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(0).toString());
        
        messages = new java.util.ArrayList<>();

        machine.handleIdentifyConsumers( new IdentifyConsumersMessage(new NodeID(), new EventID(new byte[]{11, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("no reply", 0, messages.size());

        machine.handleIdentifyConsumers( new IdentifyConsumersMessage(new NodeID(), new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("one sent", 1, messages.size());
        // check by string comparison as a short cut
        Assert.assertEquals("reply", "01.00.00.00.00.00                     Consumer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(0).toString());
        
    }

    @Test
    public void testStateMachineStringSetup() {
        
        OlcbSignalMast.StateMachine<String> machine = new OlcbSignalMast.StateMachine<>(connection, nodeID, "B");
        
        Assert.assertEquals("starting state", "B", machine.getState());
        
        machine.setEventForState("A", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0}));
        machine.setEventForState("B", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0}));

        Assert.assertEquals("A event", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0}), machine.getEventForState("A"));
        Assert.assertEquals("B event", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0}), machine.getEventForState("B"));
        
        Assert.assertEquals("none sent", 0, messages.size());
        machine.setState("A");
        Assert.assertEquals("still starting state", "B", machine.getState());
        Assert.assertEquals("one sent", 1, messages.size());

        machine.handleProducerConsumerEventReport( new ProducerConsumerEventReportMessage(nodeID, new EventID(new byte[]{2, 0, 0, 0, 0, 0, 1, 0})), null); // other eventID
        Assert.assertEquals("still starting state", "B", machine.getState());

        machine.handleProducerConsumerEventReport( new ProducerConsumerEventReportMessage(nodeID, new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0})), null); // A eventID
        Assert.assertEquals("new state", "A", machine.getState());
    }

    @Test
    public void testStateMachineStringIdEvents() {
        
        OlcbSignalMast.StateMachine<String> machine = new OlcbSignalMast.StateMachine<>(connection, nodeID, "B");
        
        machine.setEventForState("A", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0}));
        machine.setEventForState("B", new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0}));

        Assert.assertEquals("none sent", 0, messages.size());

        machine.handleIdentifyEvents( new IdentifyEventsMessage(new NodeID(), new NodeID()), null); 
        Assert.assertEquals("no reply if wrong address", 0, messages.size());
        
        machine.handleIdentifyEvents( new IdentifyEventsMessage(new NodeID(), nodeID), null); 
        Assert.assertEquals("four sent", 4, messages.size());
        // check by string comparison as a short cut
        Assert.assertEquals("msg 0", "01.00.00.00.00.00                     Consumer Identified Unknown for EventID:01.00.00.00.00.00.01.00", messages.get(0).toString());
        Assert.assertEquals("msg 1", "01.00.00.00.00.00                     Producer Identified Unknown for EventID:01.00.00.00.00.00.01.00", messages.get(1).toString());
        Assert.assertEquals("msg 2", "01.00.00.00.00.00                     Consumer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(2).toString());
        Assert.assertEquals("msg 3", "01.00.00.00.00.00                     Producer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(3).toString());

        messages = new java.util.ArrayList<>();

        machine.handleIdentifyProducers( new IdentifyProducersMessage(new NodeID(), new EventID(new byte[]{11, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("no reply", 0, messages.size());

        machine.handleIdentifyProducers( new IdentifyProducersMessage(new NodeID(), new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("one sent", 1, messages.size());
        // check by string comparison as a short cut
        Assert.assertEquals("reply", "01.00.00.00.00.00                     Producer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(0).toString());
        
        messages = new java.util.ArrayList<>();

        machine.handleIdentifyConsumers( new IdentifyConsumersMessage(new NodeID(), new EventID(new byte[]{11, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("no reply", 0, messages.size());

        machine.handleIdentifyConsumers( new IdentifyConsumersMessage(new NodeID(), new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0})), null); 
        Assert.assertEquals("one sent", 1, messages.size());
        // check by string comparison as a short cut
        Assert.assertEquals("reply", "01.00.00.00.00.00                     Consumer Identified Unknown for EventID:01.00.00.00.00.00.02.00", messages.get(0).toString());
        
    }

    // from here down is testing infrastructure
    Connection connection;
    NodeID nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
    java.util.ArrayList<Message> messages;
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        
        messages = new java.util.ArrayList<>();
        connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
                messages.add(msg);
            }
        };

        OlcbSystemConnectionMemo memo = new OlcbSystemConnectionMemo(); // this self-registers as 'M'
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.setInterface(new OlcbInterface(nodeID, connection) {
            public Connection getOutputConnection() {
                return connection;
            }
        });
        
        jmri.util.JUnitUtil.waitFor(()->{return (messages.size()>0);},"Initialization Complete message");
        messages = new java.util.ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
