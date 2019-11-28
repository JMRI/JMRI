package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * Tests for XBeeIOStream.
 *
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

   @Test
   @Ignore("data send occurs, but tearDown closes the pipes too quickly")
   public void checkSend() throws java.io.IOException {
       a.configure(); // start the send and receive threads.
       a.getOutputStream().writeChars("Hello World");
       jmri.util.JUnitUtil.waitFor(()->{ return tc.dataSent; });
   }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        tc.setAdapterMemo(new XBeeConnectionMemo());
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node = new XBeeNode(pan,uad,gad);
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("0002"),
             "Node 1");
        node.setXBee(rd);
        tc.registerNode(node);
        a = new XBeeIOStream(node,tc);
    }

    @After
    public void tearDown() {
        a.dispose();
        a=null;
        tc.terminate();
        tc = null;
        node = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

    }

}
