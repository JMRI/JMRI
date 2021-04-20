package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Unit Tests for StayOpenCheckBoxMenuItemUI
 * @author Steve Yound copyright(c) 2021
 */
public class StayOpenCheckBoxMenuItemUITest {
    
    @Test
    public void testCTor() {
        StayOpenCheckBoxMenuItemUI t = new StayOpenCheckBoxMenuItemUI();
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
