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
public class ConcentratorReporterManagerTest {

    ConcentratorTrafficController tc = null;

    @Test
    public void testCtor() {
        ConcentratorReporterManager c = new ConcentratorReporterManager(tc,"R"){
            @Override
            protected Reporter createNewReporter(String systemName, String userName){
               return null;
            }
            @Override
            public void message(jmri.jmrix.rfid.RfidMessage m){}

            @Override
            public void reply(jmri.jmrix.rfid.RfidReply m){}

        };
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new ConcentratorTrafficController(new ConcentratorSystemConnectionMemo(),"A-H"){
           @Override
           public void sendInitString(){
           }
        };
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
