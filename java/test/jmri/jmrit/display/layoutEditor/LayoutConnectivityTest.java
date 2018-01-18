package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LayoutConnectivity
 *
 * @author	Paul Bender Copyright (C) 2016
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
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(LayoutConnectivityTest.class);
}
