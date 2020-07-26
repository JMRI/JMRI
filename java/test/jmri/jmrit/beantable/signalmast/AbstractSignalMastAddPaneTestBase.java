package jmri.jmrit.beantable.signalmast;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Base-class of the 
 * tests of individual implementations of {@link SignalMastAddPane} subclasses.
 * <p>
 * See {@link SignalMastAddPaneTest} for tests of the overall
 * operation of {@link SignalMastAddPane} services.
 * 
 * @author Bob Jacobsen Copyright 2018
 */
abstract public class AbstractSignalMastAddPaneTestBase {

    /** 
     * Subclass provides Object Under Test
     */
    abstract protected SignalMastAddPane getOTT();
    
    @Test
    public void testInfoMethods() {
        SignalMastAddPane testPane = getOTT();
        
        Assert.assertNotNull(testPane.getPaneName());
        Assert.assertFalse(testPane.getPaneName().isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
