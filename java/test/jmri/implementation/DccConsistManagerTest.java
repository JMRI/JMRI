package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DccConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase  {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
        cm = new DccConsistManager(jmri.InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DccConsistManagerTest.class);

}
