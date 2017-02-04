package jmri.jmrix.rfid;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RfidConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidConnectionTypeList class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class RfidConnectionTypeListTest {

    @Test
    public void testCtor() {
        RfidConnectionTypeList c = new RfidConnectionTypeList();
        Assert.assertNotNull(c);
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
