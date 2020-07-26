package jmri.jmrit.turnoutoperations;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorTurnoutOperationConfigTest {

    @Test
    public void testCTor() {
        jmri.TurnoutOperation to = new jmri.SensorTurnoutOperation();
        SensorTurnoutOperationConfig t = new SensorTurnoutOperationConfig(to);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SensorTurnoutOperationConfigTest.class);

}
