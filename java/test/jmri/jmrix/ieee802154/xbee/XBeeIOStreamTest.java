package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <P>
 * Tests for XBeeIOStream
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeIOStreamTest {

   private static XBeeInterfaceScaffold tc = null; // set in setUp.
   private static XBeeNode node = null; // set in setUp.
   private XBeeIOStream a = null; // set in initTest 

   @Test
   public void ConstructorTest(){
       Assert.assertNotNull(a);
   }

   @Test
   public void checkInputStream(){
       Assert.assertNotNull(a.getInputStream());
   }

   @Test
   public void checkOutputStream(){
       Assert.assertNotNull(a.getOutputStream());
   }

   @Test
   public void checkStatus(){
       Assert.assertTrue(a.status());
   }

   @Test
   public void checkPortName(){
       Assert.assertEquals("NONE",a.getCurrentPortName());
   }

   @Test
   public void checkDisabled(){
       Assert.assertFalse(a.getDisabled());
   }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        tc.setAdapterMemo(new XBeeConnectionMemo());
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        node = (XBeeNode) tc.getNodeFromAddress(uad);
        Assume.assumeNotNull(tc,node);
        a = new XBeeIOStream(node,tc);
    }

    @After
    public void tearDown() {
        a.dispose();
        a=null;
        tc.terminate();
        tc = null;
        node = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
