package jmri.jmrit.display.switchboardEditor.configurexml;

import jmri.util.JUnitUtil;
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
public class SwitchboardEditor$BeanSwitchXmlTest {

    @Test
    public void testCTor() {
        SwitchboardEditor$BeanSwitchXml t = new SwitchboardEditor$BeanSwitchXml();
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

    private final static Logger log = LoggerFactory.getLogger(SwitchboardEditor$BeanSwitchXmlTest.class.getName());

}
