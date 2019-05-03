package jmri.jmrit.symbolicprog.tabbedframe;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class WatchingLabelTest {

    @Test
    public void testCTor() {
        WatchingLabel t = new WatchingLabel("Test Label",new javax.swing.JPanel());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WatchingLabelTest.class.getName());

}
