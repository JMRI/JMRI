package jmri.jmrit.logix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ThrottleSettingTest {

    @Test
    public void testCTor() {
        ThrottleSetting t = new ThrottleSetting();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCtor2() {
        ThrottleSetting ts = new ThrottleSetting(1000, "NoOp", "Enter Block", "OB1");
        Assert.assertNotNull("exists",ts);        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleSettingTest.class.getName());

}
