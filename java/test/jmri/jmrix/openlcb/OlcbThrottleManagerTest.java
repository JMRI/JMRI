package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

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
        OlcbSystemConnectionMemo m = new OlcbSystemConnectionMemo();
        m.setTrafficController(new jmri.jmrix.can.TestTrafficController());
        tm = new OlcbThrottleManager(m,new OlcbConfigurationManager(m));
    }

    @After
    public  void tearDown() {
        JUnitUtil.tearDown();
    }
}
