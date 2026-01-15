package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutBlockConnectivityTools
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutBlockConnectivityToolsTest {

    @Test
    public void testCtor() {
        LayoutBlockConnectivityTools t = new LayoutBlockConnectivityTools();
        Assertions.assertNotNull( t, "exists");
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
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutBlockConnectivityToolsTest.class);
}
