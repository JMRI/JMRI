package jmri.jmrix.rfid.merg.concentrator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.Reporter;

/**
 * ConcentratorReporterManagerTest.java
 *
 * Description:	tests for the ConcentratorReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorReporterManagerTest extends jmri.managers.AbstractReporterMgrTest {

    @Override
    public String getSystemName(int i) {
        return "RR" + "A";
    }

    @Test
    @Override
    public void testDefaultSystemName() {
        // create - Merg Concentrator uses letters instead of numbers
        Reporter t = l.provideReporter("" + "A");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest3())));
    }


    @Test
    @Override
    public void testUpperLower() {
        // Merg concentrator uses letters instead of numbers.
        Reporter t = l.provideReporter("" + "C");
        String name = t.getSystemName();
        Assert.assertNull(l.getReporter(name.toLowerCase()));
    }


    ConcentratorTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new ConcentratorTrafficController(new ConcentratorSystemConnectionMemo(),"A-H"){
           @Override
           public void sendInitString(){
           }
        };
        l = new ConcentratorReporterManager(tc,"R"){
            @Override
            public void message(jmri.jmrix.rfid.RfidMessage m){}

            @Override
            public void reply(jmri.jmrix.rfid.RfidReply m){}

        };
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
