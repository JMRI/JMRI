package jmri.jmrix.dcc4pc;

import org.junit.After;
import org.junit.Before;

/**
 * Dcc4PcReporterManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dcc4pc.Dcc4PcReporterManager
 * class
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016
 */
public class Dcc4PcReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "DPR" + i;
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
           @Override
           public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
           }
        };
        l = new Dcc4PcReporterManager(tc,new Dcc4PcSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
