package jmri;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Scale mananger tests.
 * @author Dave Sand Copyright (C) 2018
 */
public class ScaleManagerTest {

    @Test
    public void ctorTest() {
        ScaleManager sm = new ScaleManager();
        Assert.assertNotNull(sm);
    }

    @Test
    public void testManager() {
        java.util.ArrayList list = jmri.ScaleManager.getScales();
        Assert.assertEquals(list.size(), 12);

        Scale scale = ScaleManager.getScale("HO");
        Assert.assertEquals(scale.getScaleRatio(), 87.1, .1);

        scale = ScaleManager.getScaleByName("QR");
        Assert.assertNull(scale);

        scale = ScaleManager.getScaleByName("N");
        Assert.assertNotNull(scale);
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

    // private final static Logger log = LoggerFactory.getLogger(ScaleTest.class);

}
