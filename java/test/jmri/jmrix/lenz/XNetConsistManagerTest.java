package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetConsistManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetConsistManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2017
 */
public class XNetConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        cm = new XNetConsistManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        jmri.util.JUnitUtil.tearDown();
    }

    @Test
    @Override
    public void testIsCommandStationConsistPossible(){
       // true for XPressNet
       Assert.assertTrue("CS Consist Possible",cm.isCommandStationConsistPossible());
    }


}
