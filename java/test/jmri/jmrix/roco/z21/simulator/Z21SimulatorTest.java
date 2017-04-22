package jmri.jmrix.roco.z21.simulator;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Z21SimulatorTest.java
 * 
 * Description:	tests that determine if the Roco z21 Simulator is functional
 * after configuration.
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21SimulatorTest {
        
    private static  java.net.InetAddress host;
    private static int port = 21105; // default port for Z21 connections.
    private static Z21SimulatorAdapter a = null;


    // verify there is a railComm manager
    @Test
    @Ignore("tests before this class are leaving the port open, need to close first")
    public void testProgrammerManager() {
        Assert.assertTrue(a.getSystemConnectionMemo().provides(jmri.ProgrammerManager.class));
    }

    // verify there is a Reporter manager
    @Test
    @Ignore("tests before this class are leaving the port open, need to close first")
    public void testReporterManager() {
        Assert.assertTrue(a.getSystemConnectionMemo().provides(jmri.ReporterManager.class));
    }



    // one time configuration of the simulator.
    @BeforeClass
    static public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        try {
           host = java.net.InetAddress.getLocalHost();
        } catch(java.net.UnknownHostException uhe){
            Assert.fail("Unable to create host localhost");
        } 
        // create a new simulator.
        a = new Z21SimulatorAdapter();
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
    }

    @AfterClass
    static public void tearDown() {
        a.dispose();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
