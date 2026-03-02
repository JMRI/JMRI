package jmri.configurexml.turnoutoperations;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RawTurnoutOperationXmlTest {

    @Test
    public void testCTor() {
        RawTurnoutOperationXml t = new RawTurnoutOperationXml();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RawTurnoutOperationXmlTest.class);

}
