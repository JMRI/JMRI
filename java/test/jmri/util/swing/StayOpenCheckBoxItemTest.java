package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2018
 */
public class StayOpenCheckBoxItemTest {
    
    @Test
    public void testCTor() {
        StayOpenCheckBoxItem t = new StayOpenCheckBoxItem("Test Menu Item");
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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StayOpenCheckBoxItemTest.class);

}
