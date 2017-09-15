package jmri.jmrit.vsdecoder.swing;

import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ManageLocationsFrameTest {

    @Test
    public void testCTor() {
        ListeningSpot s = new ListeningSpot();
        ManageLocationsFrame t = new ManageLocationsFrame(s,null,null,null);
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

    private final static Logger log = LoggerFactory.getLogger(ManageLocationsFrameTest.class.getName());

}
