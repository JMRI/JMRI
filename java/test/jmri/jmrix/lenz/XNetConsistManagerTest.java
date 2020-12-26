package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XNetConsistManagerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetConsistManager class
 *
 * @author Paul Bender Copyright (C) 2012,2017
 */
public class XNetConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        cm = new XNetConsistManager(new XNetSystemConnectionMemo(tc));
    }

    @AfterEach
    @Override
    public void tearDown() {
        cm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Test
    @Override
    public void testIsCommandStationConsistPossible(){
       // true for XpressNet
       Assert.assertTrue("CS Consist Possible",cm.isCommandStationConsistPossible());
    }


}
