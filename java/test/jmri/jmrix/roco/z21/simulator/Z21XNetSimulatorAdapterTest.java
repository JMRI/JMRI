package jmri.jmrix.roco.z21.simulator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;

/**
 * Z21XNetSimulatorAdapterTest.java
 * Description:	tests for the jmri.jmrix.roco.z21.simulator.z21XNetSimulatorAdapter
 * class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21XNetSimulatorAdapterTest {

    @Test
    public void testCtor() {
        Z21XNetSimulatorAdapter a = new Z21XNetSimulatorAdapter();
        Assert.assertNotNull(a);
    }

    @Test
    public void testGenerateCSVersionReply(){
        Z21XNetSimulatorAdapter a = new Z21XNetSimulatorAdapter();
        Assert.assertEquals("CS Version Reply",new XNetReply("63 21 30 12 60"),a.generateReply(new XNetMessage("21 21")));
    }



    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
