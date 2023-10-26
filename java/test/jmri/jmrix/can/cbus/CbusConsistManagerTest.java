package jmri.jmrix.can.cbus;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        cm = new CbusConsistManager(jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        cm = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusConsistManagerTest.class);
}
