package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * ConcentratorReporterManagerTest.java
 *
 * Description:	tests for the ConcentratorReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "RR" + "A";
    }

    @Override
    protected int maxN() { return 1; }
    
    @Override
    protected String getNameToTest1() {
        return "A";
    }
    @Override
    protected String getNameToTest2() {
        return "C";
    }

    @Override
    @Test
    @Ignore("Not supported by this manager at this time")
    public void testReporterProvideByNumber() {
    }

    ConcentratorTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
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
        JUnitUtil.tearDown();
    }

}
