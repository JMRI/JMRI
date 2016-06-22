package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for XBeeIOStream
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeIOStreamTest {

   private XBeeInterfaceScaffold tc = null; // set in setUp.
   private XBeeNode node = null; // set in setUp.

   @Test
   public void ConstructorTest(){
       XBeeIOStream a = new XBeeIOStream(node,tc);
       Assert.assertNotNull(a);
   }

   @Test
   public void checkInputStream(){
       XBeeIOStream a = new XBeeIOStream(node,tc);
       Assert.assertNotNull(a.getInputStream());
   }

   @Test
   public void checkOutputStream(){
       XBeeIOStream a = new XBeeIOStream(node,tc);
       Assert.assertNotNull(a.getInputStream());
   }

   @Test
   public void checkStatus(){
       XBeeIOStream a = new XBeeIOStream(node,tc);
       Assert.assertTrue(a.status());
   }

   @Test
   public void checkPortName(){
       XBeeIOStream a = new XBeeIOStream(node,tc);
       Assert.assertEquals("NONE",a.getCurrentPortName());
   }

   @Test
   public void checkDisabled(){
       XBeeIOStream a = new XBeeIOStream(node,tc);
       Assert.assertFalse(a.getDisabled());
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new XBeeInterfaceScaffold();
        jmri.util.JUnitAppender.assertErrorMessage("Deprecated Method setInstance called");
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node = new XBeeNode(pan,uad,gad);
        tc.setAdapterMemo(new XBeeConnectionMemo());
        Assume.assumeNotNull(tc,node);
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        tc = null;
        node = null;
    }


}
