package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XBeeSensorManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeSensorManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeSensorManagerTest {

    @Test
    @Ignore("needs XBee Object from scaffold")
    public void testCtor() {
        XBeeTrafficController tc = new XBeeTrafficController() {
            public void setInstance() {
            }
        };
        XBeeSensorManager m = new XBeeSensorManager(tc, "ABC");
        Assert.assertNotNull("exists", m);
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
