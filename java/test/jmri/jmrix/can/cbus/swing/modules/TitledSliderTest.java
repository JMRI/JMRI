package jmri.jmrix.can.cbus.swing.modules;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of Cbus Modules Common Code
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class TitledSliderTest {
    
    protected UpdateNV _update;
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCtor() {

        TitledSlider t = new TitledSlider("Title for Test", 1, _update);
        Assert.assertNotNull("exists",t);
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
