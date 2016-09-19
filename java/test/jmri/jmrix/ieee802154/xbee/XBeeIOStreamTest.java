package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * <P>
 * Tests for XBeeIOStream
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
@RunWith(PowerMockRunner.class)
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
       Assert.assertNotNull(a.getOutputStream());
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
        //apps.tests.Log4JFixture.setUp();
        tc = new XBeeInterfaceScaffold();
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        tc.setAdapterMemo(new XBeeConnectionMemo());
        node = (XBeeNode) tc.getNodeFromAddress(uad);
        Assume.assumeNotNull(tc,node);
    }

    @After
    public void tearDown() {
        //apps.tests.Log4JFixture.tearDown();
        tc = null;
        node = null;
    }


}
