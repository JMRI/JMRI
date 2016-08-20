package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for XBeeNodeManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeNodeManagerTest {

   XBeeInterfaceScaffold tc = null; // set in setUp.

   @Test
   public void ConstructorTest(){
       XBeeNodeManager a = new XBeeNodeManager(tc);
       Assert.assertNotNull(a);
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new XBeeInterfaceScaffold();
        jmri.util.JUnitAppender.assertErrorMessage("Deprecated Method setInstance called");
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        tc = null;
    }


}
