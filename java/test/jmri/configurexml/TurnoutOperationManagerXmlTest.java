package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TurnoutOperationManagerXmlTest {

    @Test
    public void testCTor() {
        TurnoutOperationManagerXml t = new TurnoutOperationManagerXml();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerXmlTest.class);

}
