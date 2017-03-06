package jmri.jmrix.ieee802154.swing;

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
public class IEEE802154MenuTest {

    @Test
    public void testCTor() {
        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo = new jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo();
        IEEE802154Menu t = new IEEE802154Menu("IEEE 802.15.4 test menu",memo);
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

    private final static Logger log = LoggerFactory.getLogger(IEEE802154MenuTest.class.getName());

}
