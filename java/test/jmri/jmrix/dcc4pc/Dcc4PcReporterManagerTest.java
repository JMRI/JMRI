package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Dcc4PcReporterManagerTest.java
 *
 * Test for the jmri.jmrix.dcc4pc.Dcc4PcReporterManager
 * class
 *
 * @author Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016
 */
public class Dcc4PcReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "DR" + i;
    }


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
           @Override
           public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
           }
        };
        l = new Dcc4PcReporterManager(tc,new Dcc4PcSystemConnectionMemo(tc));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
