package jmri.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DccConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase  {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
        cm = new DccConsistManager(jmri.InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class));
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DccConsistManagerTest.class);

}
