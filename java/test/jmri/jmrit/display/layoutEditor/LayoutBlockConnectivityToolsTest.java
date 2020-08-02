package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull("exists", t);
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
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutBlockConnectivityToolsTest.class);
}
