package jmri.jmrix.loconet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.Reporter;

/**
 * LnReporterManagerTest.java
 *
 * Description:	tests for the LnReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class LnReporterManagerTest extends jmri.managers.AbstractReporterMgrTest {

    @Override
    public String getSystemName(int i) {
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
