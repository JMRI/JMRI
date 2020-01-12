package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;
import jmri.jmrix.can.TestTrafficController;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbThrottleManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 * @author      Paul Bender Copyright (C) 2016
 */
public class OlcbThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private static OlcbSystemConnectionMemo m;

    @Test
    @Override
    @Ignore("test requires further setup")
    @ToDo("finish test setup and remove this overriden test so that the parent class test can run")
    public void testGetThrottleInfo() {
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        tm = new OlcbThrottleManager(m);
    }

    @After
    public void tearDown() {
       tm = null;
    }

    @BeforeClass
    public static void preClassInit() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        m.setTrafficController(tc);
    }

    @AfterClass
    public static void postClassTearDown() {
        if(m != null && m.getInterface() !=null ) {
           m.getInterface().dispose();
        }
        m = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
