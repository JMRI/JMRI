package jmri.jmrit.dispatcher;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AllocationRequestTest {

    @Test
    public void testCTor() {
        jmri.Transit transit = new jmri.Transit("TT1");
        ActiveTrain at = new ActiveTrain(transit,"Train",ActiveTrain.USER);
        jmri.Section section1 = new jmri.Section("TS1");
        AllocationRequest t = new AllocationRequest(section1,1,1,at);
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

    // private final static Logger log = LoggerFactory.getLogger(AllocationRequestTest.class);

}
