package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutConnectivity
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutConnectivityTest {

    @Test
    public void testCtor() {
        LayoutBlock b = new LayoutBlock("testb", "testb");
        LayoutBlock d = new LayoutBlock("testd", "testd");
        LayoutConnectivity c = new LayoutConnectivity(b, d);
        Assertions.assertNotNull( c, "exists");
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutConnectivityTest.class);
}
