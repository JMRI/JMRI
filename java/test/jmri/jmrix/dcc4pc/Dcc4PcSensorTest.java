package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.NotApplicable;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Test
    @Override
    @NotApplicable("status not currently updated for INCONSISTENT")
    public void testSensorSetKnownState() {
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new Dcc4PcSensor("DS0:1","test");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        t = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcSensorTest.class);

}
