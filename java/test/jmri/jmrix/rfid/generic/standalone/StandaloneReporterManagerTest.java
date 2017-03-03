package jmri.jmrix.rfid.generic.standalone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;

/**
 * Note: Standalone only allows _one_ NamedBean, named e.g. RR1, which means certain tests
 * are defaulted away.
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class StandaloneReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
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
    protected int maxN() { return 1; }

    @Override
    protected String getNameToTest1() {
        return "1";
    }
}
