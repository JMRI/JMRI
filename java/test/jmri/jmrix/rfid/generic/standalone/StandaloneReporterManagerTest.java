package jmri.jmrix.rfid.generic.standalone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.Reporter;

/**
 * StandaloneReporterManagerTest.java
 *
 * Description:	tests for the StandaloneReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class StandaloneReporterManagerTest extends jmri.managers.AbstractReporterMgrTest {

    @Override
    public String getSystemName(int i) {
        return "RR" + i;
    }

    StandaloneTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new StandaloneTrafficController(new StandaloneSystemConnectionMemo(){
        });
        l = new StandaloneReporterManager(tc,"R"){
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

    @Override
    protected int getNumToTest1() {
        return 1;
    }

    @Override
    protected int getNumToTest2() {
        return 1;
    }

    @Override
    protected int getNumToTest3() {
        return 1;
    }

    @Override
    protected int getNumToTest4() {
        return 1;
    }

    @Override
    protected int getNumToTest5() {
        return 1;
    }


}
