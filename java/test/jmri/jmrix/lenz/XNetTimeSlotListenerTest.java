package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetTimeSlotListenerTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.XNetTimeSlotListener class
 *
 * @author  Paul Bender Copyright (C) 2010,2016,2017
 */
public class XNetTimeSlotListenerTest {

    private XNetTimeSlotListener tsl = null;
    private XNetPortController p = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull(tsl);
    }

    @Test
    public void testMessage(){
       XNetReply r = new XNetReply("01 05 04"); // timeslot removed
       tsl.message(r);
       // after sending the reply, the controller should show okToSend() as false.
       Assert.assertFalse("ok to send false after timeslot removed",p.okToSend());
       r = new XNetReply("01 07 06"); // timeslot restored
       tsl.message(r);
       // after sending the reply, the controller should show okToSend() as true.
       Assert.assertTrue("ok to send true after timeslot restored",p.okToSend());
       r = new XNetReply("01 08 09"); // message sent, no timeslot.
       tsl.message(r);
       // after sending the reply, the controller should show okToSend() as false.
       Assert.assertFalse("ok to send true after message sent without a timeslot restored",p.okToSend());
       r = new XNetReply("01 04 05"); // OK message.
       tsl.message(r);
       // after sending the reply, the controller should show okToSend() as false.
       Assert.assertFalse("ok to send still false after OK message",p.okToSend());
       r = new XNetReply("01 07 06"); // timeslot restored
       tsl.message(r);
       // after sending the reply, the controller should show okToSend() as true.
       Assert.assertTrue("ok to send true after timeslot restored",p.okToSend());
       r = new XNetReply("01 07 06"); // timeslot restored
       tsl.message(r);
       // after sending the reply, the controller should show okToSend() as true.
       Assert.assertTrue("ok to send true after timeslot restored",p.okToSend());
       r = new XNetReply("01 04 05"); // OK message.
       tsl.message(r);
       // after sending the reply, the controller should show okToSend() as true.
       Assert.assertTrue("ok to send still true after OK message",p.okToSend());
    }


    @Before
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
  
    @After
    public void tearDown(){
       p=null;
       tsl=null;
       jmri.util.JUnitUtil.tearDown();
    }

}
