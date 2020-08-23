package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

import jmri.jmrix.can.TestTrafficController;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbThrottleManager class.
 *
 * @author Bob Jacobsen Copyright 2008, 2010, 2011
 * @author      Paul Bender Copyright (C) 2016
 */
public class OlcbThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private static OlcbSystemConnectionMemo m;

    @Test
    @Override
    @Disabled("test requires further setup")
    @ToDo("finish test setup and remove this overridden test so that the parent class test can run")
    public void testGetThrottleInfo() {
    }

    @Override
    @BeforeEach
    public void setUp() {
        tm = new OlcbThrottleManager(m);
    }

    @AfterEach
    public void tearDown() {
       tm = null;
    }

    @BeforeAll
    public static void preClassInit() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        m.setTrafficController(tc);
    }

    @AfterAll
    public static void postClassTearDown() {
        if(m != null && m.getInterface() !=null ) {
            m.getTrafficController().terminateThreads();
            m.getInterface().dispose();
        }
        m = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
