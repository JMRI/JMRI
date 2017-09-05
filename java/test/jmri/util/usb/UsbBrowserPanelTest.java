package jmri.util.usb;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UsbBrowserPanelTest {

    @Test
    @Ignore("we probably need to mock the USB library to obtain consistent results.")
    public void testCTor() {
        UsbBrowserPanel t = new UsbBrowserPanel();
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

    // private final static Logger log = LoggerFactory.getLogger(UsbBrowserPanelTest.class);

}
