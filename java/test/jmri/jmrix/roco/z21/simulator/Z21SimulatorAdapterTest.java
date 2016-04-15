package jmri.jmrix.roco.z21.simulator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.DatagramSocket;
import java.net.DatagramPacket;


/**
 * Z21SimulatorAdapterTest.java
 * 
 * Description:	tests for the jmri.jmrix.roco.z21.simulator.z21SimulatorAdapter
 * class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21SimulatorAdapterTest extends TestCase {
        
    private java.net.InetAddress host;
    private static int port = 21105; // default port for Z21 connections.

    public void testCtor() {
        Z21SimulatorAdapter a = new Z21SimulatorAdapter();
        Assert.assertNotNull(a);
    }

    /* 
     * Test that the Z21 simulator correctly sets up the network connection.
     */ 
    public void testConnection() {
        // create a new simulator.
        Z21SimulatorAdapter a = new Z21SimulatorAdapter();
        // connect the port
        try {
           a.connect();
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

    // from here down is testing infrastructure
    public Z21SimulatorAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading" , Z21SimulatorAdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21SimulatorAdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        apps.tests.Log4JFixture.setUp();
        try {
           host = java.net.InetAddress.getLocalHost();
        } catch(java.net.UnknownHostException uhe){
            Assert.fail("Unable to create host localhost");
        } 
    }

    protected void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
