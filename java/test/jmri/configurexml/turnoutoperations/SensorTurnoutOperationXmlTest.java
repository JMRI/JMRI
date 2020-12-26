package jmri.configurexml.turnoutoperations;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorTurnoutOperationXmlTest {

    @Test
    public void testCTor() {
        SensorTurnoutOperationXml t = new SensorTurnoutOperationXml();
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

    // private final static Logger log = LoggerFactory.getLogger(SensorTurnoutOperationXmlTest.class);

}
