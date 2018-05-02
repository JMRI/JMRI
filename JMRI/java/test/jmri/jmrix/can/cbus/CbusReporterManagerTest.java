package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * CbusReporterManagerTest.java
 *
 * Description:	tests for the CbusReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class CbusReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "MR" + i;
    }

    TrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        l = new CbusReporterManager(memo);
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.tearDown();
    }


}
