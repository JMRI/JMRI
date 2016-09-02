package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for XBeeAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeAdapterTest {

   @Test
   public void ConstructorTest(){
       XBeeAdapter a = new XBeeAdapter();
       Assert.assertNotNull(a);
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
