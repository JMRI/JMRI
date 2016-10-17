package jmri.jmrix.openlcb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbThrottleManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 * @author      Paul Bender Copyright (C) 2016
 */
public class OlcbThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        OlcbSystemConnectionMemo m = new OlcbSystemConnectionMemo();
        m.setTrafficController(new jmri.jmrix.can.TestTrafficController());
        tm = new OlcbThrottleManager(m,new OlcbConfigurationManager(m));
    }

    @After
    public  void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
