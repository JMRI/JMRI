package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Before;

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
        apps.tests.Log4JFixture.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        cm = new XNetConsistManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        apps.tests.Log4JFixture.tearDown();
    }

}
