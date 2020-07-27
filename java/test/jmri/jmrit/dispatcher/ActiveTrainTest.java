package jmri.jmrit.dispatcher;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ActiveTrainTest {

    @Test
    public void testCTor() {
        jmri.Transit transit = new jmri.Transit("TT1");
        ActiveTrain t = new ActiveTrain(transit,"Train",ActiveTrain.USER);
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

    // private final static Logger log = LoggerFactory.getLogger(ActiveTrainTest.class);

}
