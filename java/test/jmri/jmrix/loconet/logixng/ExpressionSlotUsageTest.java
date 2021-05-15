package jmri.jmrix.loconet.logixng;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the ExpressionSlotUsage class
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSlotUsageTest {
    
    @Test
    public void testCtor() {
        ExpressionSlotUsage e = new ExpressionSlotUsage("IQDE1", null, null);
        Assert.assertNotNull(e);
    }
    
    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        // Temporary let the error messages from this test be shown to the user
//        JUnitAppender.end();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsageTest.class);

}
