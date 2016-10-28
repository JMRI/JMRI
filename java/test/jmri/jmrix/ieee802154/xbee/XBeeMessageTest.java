package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XBeeMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeMessage class
 *
 * @author	Paul Bender
 */
public class XBeeMessageTest {

    @Test
    public void testCtor() {
        XBeeMessage m = new XBeeMessage(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
