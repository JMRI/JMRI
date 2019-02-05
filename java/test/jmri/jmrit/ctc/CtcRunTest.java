package jmri.jmrit.ctc;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the CtcRun Class
 * @author Dave Sand Copyright (C) 2018
 */
public class CtcRunTest {

    @Test
    // test creation
    public void testCreate() {
        new CtcRun();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public  void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRunTest.class);
}