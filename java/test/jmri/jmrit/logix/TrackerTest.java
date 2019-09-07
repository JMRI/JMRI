package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrackerTest {

    private Tracker tracker = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tracker);
    }

    @Test
    public void testSetupCheck(){
        tracker.setupCheck();
        Assert.assertEquals("trainname","Test",tracker.getTrainName());
        Assert.assertNotNull("range",tracker.getRange());
        Assert.assertNotNull("headblock",tracker.getHeadBlock());
        Assert.assertNotNull("tailblock",tracker.getTailBlock());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tracker = new Tracker(new OBlock("OB1", "Test"), "Test");
    }

    @After
    public void tearDown() {
        tracker = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackerTest.class);

}
