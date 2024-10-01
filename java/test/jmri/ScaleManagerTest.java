package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * ScaleMananger tests.
 * @author Dave Sand Copyright (C) 2018
 */
public class ScaleManagerTest {

    @Test
    public void testManager() {
        java.util.List<Scale> list = ScaleManager.getScales();
        Assertions.assertEquals(12, list.size());

        Scale scale = ScaleManager.getScale("HO");
        Assertions.assertEquals(87.1, scale.getScaleRatio(), .1);

        scale = ScaleManager.getScaleByName("QR");
        Assertions.assertNull(scale);

        scale = ScaleManager.getScaleByName("N");
        Assertions.assertNotNull(scale);
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

    // private final static Logger log = LoggerFactory.getLogger(ScaleManagerTest.class);

}
