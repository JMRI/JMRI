package jmri.jmrix.loconet;

import org.junit.After;
import org.junit.Before;

/**
 * LnReporterManagerTest.java
 *
 * Description:	tests for the LnReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class LnReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "LR" + i;
    }

    LnTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new LocoNetInterfaceScaffold();
        l = new LnReporterManager(tc,"L");
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    @Override
    protected int maxN() { return 1; }
}
