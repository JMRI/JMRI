package jmri.jmrit.powerpanel;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PowerButtonActionTest {

    @Test
    public void testCTor() {
        PowerButtonAction t = new PowerButtonAction();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugPowerManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PowerButtonActionTest.class);

}
