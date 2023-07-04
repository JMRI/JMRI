package jmri.jmrix.loconet.logixng.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the ExpressionSlotUsageSwing class
 *
 * @author Daniel Bergqvist Copyright 2020
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class GetNumSlotsDialogTest {
    
    @Test
    public void testCtor() {

        GetNumSlotsDialog e = new GetNumSlotsDialog(null, null);
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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsageSwingTest.class);

}
