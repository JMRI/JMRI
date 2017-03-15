package jmri.implementation;

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
public class SignalMastRepeaterTest {

    @Test
    public void testCTor() {
        VirtualSignalMast s1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        VirtualSignalMast s2 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        SignalMastRepeater t = new SignalMastRepeater(s1,s2);
        Assert.assertNotNull("exists",t);
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

    private final static Logger log = LoggerFactory.getLogger(SignalMastRepeaterTest.class.getName());

}
