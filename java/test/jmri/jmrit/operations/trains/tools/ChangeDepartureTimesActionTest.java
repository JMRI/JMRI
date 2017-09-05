package jmri.jmrit.operations.trains.tools;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ChangeDepartureTimesActionTest {

    @Test
    public void testCTor() {
        ChangeDepartureTimesAction t = new ChangeDepartureTimesAction("Test Action");
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

    // private final static Logger log = LoggerFactory.getLogger(ChangeDepartureTimesActionTest.class);

}
