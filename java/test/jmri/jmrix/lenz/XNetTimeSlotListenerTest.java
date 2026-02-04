package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.*;

/**
 * XNetTimeSlotListenerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetTimeSlotListener class
 *
 * @author  Paul Bender Copyright (C) 2010,2016,2017
 */
public class XNetTimeSlotListenerTest {

    private XNetTimeSlotListener tsl = null;
    private XNetPortController p = null;

    @Test
    public void testCtor(){
        assertNotNull(tsl);
    }

    @Test
    public void testMessage(){
        XNetReply r = new XNetReply("01 05 04"); // timeslot removed
        tsl.message(r);
        // after sending the reply, the controller should show okToSend() as false.
        assertFalse( p.okToSend(), "ok to send false after timeslot removed");
        r = new XNetReply("01 07 06"); // timeslot restored
        tsl.message(r);
        // after sending the reply, the controller should show okToSend() as true.
        assertTrue( p.okToSend(), "ok to send true after timeslot restored");
        r = new XNetReply("01 08 09"); // message sent, no timeslot.
        tsl.message(r);
        // after sending the reply, the controller should show okToSend() as false.
        assertFalse( p.okToSend(), "ok to send true after message sent without a timeslot restored");
        r = new XNetReply("01 04 05"); // OK message.
        tsl.message(r);
        // after sending the reply, the controller should show okToSend() as false.
        assertFalse( p.okToSend(), "ok to send still false after OK message");
        r = new XNetReply("01 07 06"); // timeslot restored
        tsl.message(r);
        // after sending the reply, the controller should show okToSend() as true.
        assertTrue( p.okToSend(), "ok to send true after timeslot restored");
        r = new XNetReply("01 07 06"); // timeslot restored
        tsl.message(r);
        // after sending the reply, the controller should show okToSend() as true.
        assertTrue( p.okToSend(), "ok to send true after timeslot restored");
        r = new XNetReply("01 04 05"); // OK message.
        tsl.message(r);
        // after sending the reply, the controller should show okToSend() as true.
        assertTrue( p.okToSend(), "ok to send still true after OK message");
    }


    @BeforeEach
    public void setUp(){

       jmri.util.JUnitUtil.setUp();

       p = new XNetSimulatorPortController(){
           @Override
           public DataInputStream getInputStream(){ return null; }
           @Override
           public DataOutputStream getOutputStream(){ return null; }
           @Override
           public boolean status(){ return true; }
           @Override
           public void setOutputBufferEmpty(boolean s){}
           @Override
           public void configure(){}
           @Override
           public String openPort(String s,String p){return "";}
       };

       tsl = new XNetTimeSlotListener(p);

    }
  
    @AfterEach
    public void tearDown(){
       p=null;
       tsl=null;
       jmri.util.JUnitUtil.tearDown();
    }

}
