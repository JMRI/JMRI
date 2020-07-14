package jmri.jmrit.automat;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SampleAutomaton3ActionTest {

    @Test
    public void testCTor() {
        SampleAutomaton3Action t = new SampleAutomaton3Action("Test");
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

    // private final static Logger log = LoggerFactory.getLogger(SampleAutomaton3ActionTest.class);

}
