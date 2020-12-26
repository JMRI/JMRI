package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull("exists", c);
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutConnectivityTest.class);
}
