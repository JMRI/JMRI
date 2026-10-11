package jmri.util.com.sun;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2020
 */
public class ToggleOrPressButtonModelTest {

    @Test
    public void testCTor() {
        ToggleOrPressButtonModel t = new ToggleOrPressButtonModel(null,true);
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

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ToggleOrPressButtonModelTest.class);

}
