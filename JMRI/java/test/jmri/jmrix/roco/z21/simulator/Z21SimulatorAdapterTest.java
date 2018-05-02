package jmri.jmrix.roco.z21.simulator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Z21SimulatorAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.simulator.z21SimulatorAdapter
 * class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21SimulatorAdapterTest {

    private static java.net.InetAddress host;
    private static int port = 21105; // default port for Z21 connections.
    private static Z21SimulatorAdapter a  = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(a);
    }

    /*
     * Test that the Z21 simulator correctly sets up the network connection.
     */
    @Test
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

          /*  try {
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
            }*/
        } catch(java.net.SocketException se) {
            Assert.fail("Failure Creating Socket");
        }
    }

    @Test
    public void RailComDataChangedReply(){
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method getZ21RailComDataChangedReplyMethod = null;
        try {
            getZ21RailComDataChangedReplyMethod = a.getClass().getDeclaredMethod("getZ21RailComDataChangedReply");
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method getZ21RailComDataChagnedReply in Z21SimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(getZ21RailComDataChangedReplyMethod);
        getZ21RailComDataChangedReplyMethod.setAccessible(true);

        try {
            Z21Reply z = (Z21Reply) getZ21RailComDataChangedReplyMethod.invoke(a);
            Assert.assertEquals("Empty Railcom Report", "04 00 88 00",z.toString());
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method getZ21RailComDataChangedReply in Z21SimulatorAdapter class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("getZ21RailComDataChangedReply  executon failed reason: " + cause.getMessage());
        }

    }

    // The minimal setup for log4J
    @BeforeClass
    static public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConfigureManager();
        try {
           host = java.net.InetAddress.getLocalHost();
        } catch(java.net.UnknownHostException uhe){
            Assert.fail("Unable to create host localhost");
        }
        // create a new simulator.
        a = new Z21SimulatorAdapter();
    }

    @AfterClass
    static public void tearDown() {
        a.getSystemConnectionMemo().getTrafficController().terminateThreads();
        a.dispose();
        a.terminateThread();
        JUnitUtil.tearDown();
    }

}
