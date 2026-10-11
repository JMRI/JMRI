package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Unit Tests for StayOpenCheckBoxMenuItemUI
 * @author Steve Young copyright(c) 2021
 */
public class StayOpenCheckBoxMenuItemUITest {
    
    @Test
    public void testCTor() {
        StayOpenCheckBoxMenuItemUI t = new StayOpenCheckBoxMenuItemUI();
        Assertions.assertNotNull( t, "exists");
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
