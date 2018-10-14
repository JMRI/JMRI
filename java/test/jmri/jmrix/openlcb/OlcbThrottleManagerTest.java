package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import jmri.jmrix.can.TestTrafficController;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbThrottleManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 * @author      Paul Bender Copyright (C) 2016
 */
public class OlcbThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private static OlcbSystemConnectionMemo m;
    private static OlcbConfigurationManagerScaffold ocm;

    @Test
    @Override
    @Ignore("test requires further setup")
    public void testGetThrottleInfo() {
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        tm = new OlcbThrottleManager(m,ocm);
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
        ocm = new OlcbConfigurationManagerScaffold(m);
    }

    @AfterClass
    public static void postClassTearDown() {
        if(m != null && m.getInterface() !=null ) {
           m.getInterface().dispose();
        }
        m = null;
        ocm = null;
        JUnitUtil.tearDown();
    }
}
