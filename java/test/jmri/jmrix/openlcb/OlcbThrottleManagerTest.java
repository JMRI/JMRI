package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import jmri.jmrix.can.TestTrafficController;

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
        JUnitUtil.setUp();
        OlcbSystemConnectionMemo m = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        m.setTrafficController(tc);
        tm = new OlcbThrottleManager(m,new OlcbConfigurationManagerScaffold(m));
    }

    @After
    public  void tearDown() {
        JUnitUtil.tearDown();
    }
}
