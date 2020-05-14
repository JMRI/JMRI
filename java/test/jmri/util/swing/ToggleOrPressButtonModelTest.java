package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2020
 */
public class ToggleOrPressButtonModelTest {
    
    @Test
    public void testCTor() {
        ToggleOrPressButtonModel t = new ToggleOrPressButtonModel(null,true);
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ToggleOrPressButtonModelTest.class);

}
