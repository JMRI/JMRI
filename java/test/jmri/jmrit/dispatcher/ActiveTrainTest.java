package jmri.jmrit.dispatcher;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ActiveTrainTest.class);

}
