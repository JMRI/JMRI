package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NmraConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        cm = new NmraConsistManager(jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        cm = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NmraConsistManagerTest.class);
}
