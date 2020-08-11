package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2018
 */
public class StayOpenCheckBoxItemTest {
    
    @Test
    public void testCTor() {
        StayOpenCheckBoxItem t = new StayOpenCheckBoxItem("Test Menu Item");
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

    // private final static Logger log = LoggerFactory.getLogger(StayOpenCheckBoxItemTest.class);

}
