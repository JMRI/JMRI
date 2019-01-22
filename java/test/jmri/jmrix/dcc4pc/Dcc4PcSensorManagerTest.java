package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcc4PcSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i){
        return "DS" + i;
    }

    @Test
    @Ignore("need to trace receive for a better post reply check")
    public void checkReceiveData(){
       // message copied from DCC4PC omnibus specification
       byte packetData[]={(byte)0x42,(byte)0x01,(byte)0x3F,(byte)0xA8,
	       (byte)0x90,(byte)0x90,(byte)0xC0,(byte)0x00,(byte)0x00,
               (byte)0x42,(byte)0x02,(byte)0xA3,(byte)0xAC,(byte)0x8B,
	       (byte)0x69,(byte)0x4E,(byte)0xA5,(byte)0x99,(byte)0x9A,
               (byte)0x01,(byte)0x01,(byte)0x91,(byte)0x04,(byte)0x22,
               (byte)0x99,(byte)0xA5,(byte)0xA3,(byte)0xAC,(byte)0x81,
               (byte)0x42,(byte)0x03,(byte)0x3F,(byte)0xBC,(byte)0x80,
               (byte)0x90,(byte)0xC0,(byte)0x00,(byte)0x20,(byte)0x24,
               (byte)0x8E,(byte)0xD4,(byte)0x4E,(byte)0x59,(byte)0xA3,
	       (byte)0xAC};
      Dcc4PcReply rep = new Dcc4PcReply(packetData);
      rep.setUnsolicited();
      ((Dcc4PcSensorManager)l).reply(rep);
      Assert.assertNotNull("sensor exists after packet",l.getSensor("DS2"));   
      Assert.assertEquals("sensor state after packet",jmri.Sensor.ACTIVE,l.getSensor("DS2").getState());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
          @Override
          public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
          }
       };
       Dcc4PcSystemConnectionMemo memo = new Dcc4PcSystemConnectionMemo(tc);

       l = new Dcc4PcSensorManager(tc,memo);
    }

    @After
    public void tearDown() {
	l.dispose();
	l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcSensorManagerTest.class);

}
