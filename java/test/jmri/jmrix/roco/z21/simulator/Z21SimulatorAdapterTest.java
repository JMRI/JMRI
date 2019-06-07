package jmri.jmrix.roco.z21.simulator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.*;
import org.junit.*;
import org.junit.rules.*;

/**
 * Z21SimulatorAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.simulator.z21SimulatorAdapter
 * class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21SimulatorAdapterTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries
    
    private java.net.InetAddress host;
    private int port = 21105; // default port for Z21 connections.
    private Z21SimulatorAdapter a  = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(a);
    }

    /*
     * Test that the Z21 simulator correctly sets up the network connection.
     */
    @Test
    @Ignore("test is currently too unreliable in CI environments.  The class under test frequently fails to bind to the port")
    public void testConnection() {
        // connect the port
        try {
           a.connect();
        } catch(java.net.BindException be) {
            Assert.fail("Exception binding to Socket");
        } catch (java.lang.Exception e) {
           Assert.fail("Exception configuring server port");
        }
        // and configure/start the simulator.
        a.configure();

        // try connecting to the port.
        try(DatagramSocket socket = new DatagramSocket()){
            // create a datagram with the data from a valid message.
            // this is a request to get the serial number.
            byte data[] = {0x04,0x00,0x10,0x00};
            DatagramPacket sendPacket = new DatagramPacket(data,4,host, port);
            // and send it.

            try {
               a.getSocket().send(sendPacket);
            } catch(java.io.IOException ioe) {
              Assert.fail("IOException writing to network port");
            }

            try {
               byte buffer[] = new byte[100];
               DatagramPacket p = new DatagramPacket(buffer,100,host,port);
               // set the timeout on the socket.
               try {
                 a.getSocket().setSoTimeout(30000); // 30 second timeout.
               } catch( java.net.SocketException timeoutse) {
                   // this is not a fatal error for this test, just
                   // an optimization in case something went wrong.
               }
               a.getSocket().receive(p);
               Assert.assertTrue("received data from simulator",0!=p.getLength());
            } catch(java.net.SocketTimeoutException ste) {
              Assert.fail("Socket Timeout Exception reading from network port");
            } catch(java.io.IOException ioe) {
              Assert.fail("IOException reading from network port");
            }
        } catch(java.net.SocketException se) {
            Assert.fail("Failure Creating Socket");
        }
    }

    @Test
    public void RailComDataChangedReply(){
	    cannedMessageCheck("getZ21RailComDataChangedReply","04 00 88 00");
    }

    @Test
    public void HardwareVersionReply(){
	    cannedMessageCheck("getHardwareVersionReply","0C 00 1A 00 00 02 00 00 20 01 00 00");
    }

    @Test
    public void XPressNetUnknownCommandReply(){
	    cannedMessageCheck("getXPressNetUnknownCommandReply","07 00 40 00 61 82 E3");
    }

    @Test
    public void Z21SerialNumberReply(){
	    cannedMessageCheck("getZ21SerialNumberReply","08 00 10 00 00 00 00 00");
    }

    @Ignore("test gives different results depending on test ordering, due to static simulator object")
    @Test
    public void Z21BroadCastFlagsReply(){
	    cannedMessageCheck("getZ21BroadCastFlagsReply","08 00 51 00 01 00 01 0F");
    }

    private void cannedMessageCheck(String methodName,String expectedReply){
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method method = null;
        try {
            method = a.getClass().getDeclaredMethod(methodName);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method " + methodName + "in Z21SimulatorAdapter class");
        }

        // override the default permissions.
        Assert.assertNotNull(method);
        method.setAccessible(true);

        try {
            Z21Reply z = (Z21Reply) method.invoke(a);
            Assert.assertEquals(methodName + " return value", expectedReply,z.toString());
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method " + methodName + " in Z21SimulatorAdapter class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail(methodName + " executon failed reason: " + cause.getMessage());
        }
    }

    // verify there is a railComm manager
    @Test
    public void testAddressedProgrammerManager() {
        // connect the port
        try {
           a.connect();
        } catch(java.net.BindException be) {
            Assert.fail("Exception binding to Socket");
        } catch (java.lang.Exception e) {
           Assert.fail("Exception configuring server port");
        }
        // and configure/start the simulator.
        a.configure();
        Assert.assertTrue(a.getSystemConnectionMemo().provides(jmri.AddressedProgrammerManager.class));
    }

    // verify there is a Reporter manager
    @Test
    public void testReporterManager() {
        Assert.assertTrue(a.getSystemConnectionMemo().provides(jmri.ReporterManager.class));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();

        try {
           host = java.net.InetAddress.getLocalHost();
        } catch(java.net.UnknownHostException uhe){
            Assert.fail("Unable to create host localhost");
        }
        // create a new simulator.
        a = new Z21SimulatorAdapter();
    }

    @After
    public void tearDown() {
	if(a.getSystemConnectionMemo() != null ) {
		if( a.getSystemConnectionMemo().getTrafficController() != null ) {
			a.getSystemConnectionMemo().getTrafficController().terminateThreads();
		}
		a.getSystemConnectionMemo().dispose();
	}
        a.terminateThread();
        // suppress two timeout messages that occur
	JUnitAppender.suppressMessageStartsWith(org.apache.log4j.Level.WARN,"Timeout on reply to message:");
	JUnitAppender.suppressMessageStartsWith(org.apache.log4j.Level.WARN,"Timeout on reply to message:");
        a.dispose();
        JUnitUtil.tearDown();
    }

}
