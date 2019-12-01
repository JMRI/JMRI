package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusMultiMeterTest extends jmri.implementation.AbstractMultiMeterTestBase {

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        mm = new CbusMultiMeter(memo);
    }
    
    @After
    @Override
    public void tearDown() {
        
        mm = null;
        tcis=null;
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        
    }
    
    @Test
    public void testEnableDisable(){
        
        Assert.assertEquals("no listener to start",0,tcis.numListeners());
        
        mm.enable();
        JUnitAppender.assertErrorMessageStartsWith("Unable to Locate Details for Master Command Station");
        Assert.assertEquals("not listening",0,tcis.numListeners());
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        
        mm.enable();
        JUnitAppender.assertErrorMessageStartsWith("Unable to Locate Details for Master Command Station");
        Assert.assertEquals("node table listening",1,tcis.numListeners());
        
        CbusNode testCs = nodeModel.provideNodeByNodeNum(777);
        testCs.setCsNum(0);
        Assert.assertEquals("node + node table listening",2,tcis.numListeners());
        
        mm.enable();
        
        Assert.assertEquals("multimeter listening",3,tcis.numListeners());
        
        mm.disable();
        
        Assert.assertEquals("mm not listening",2,tcis.numListeners());
        
        nodeModel.dispose();
        testCs.dispose();
        
    }
    
    @Test
    public void testMultiMCanReply(){
        
        CbusMultiMeter mm = new CbusMultiMeter(memo);
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        
        CbusNode testCs = nodeModel.provideNodeByNodeNum(54321);
        testCs.setCsNum(0);
        
        mm.enable();
        
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(7);
        r.setElement(0, CbusConstants.CBUS_ACOF2);
        r.setElement(1, 0xd4); // nn 54322
        r.setElement(2, 0x32); // nn 54322
        r.setElement(3, 0x00); // en 1
        r.setElement(4, 0x01); // en 1
        r.setElement(5, 0x00); // 8mA
        r.setElement(6, 0x08); // 8mA
        mm.reply(r);
        
        Assert.assertEquals(0,mm.getCurrent(),0.001 ); // wrong opc
        
        r.setElement(0, CbusConstants.CBUS_ACON2);
        mm.reply(r);
        
        Assert.assertEquals(0,mm.getCurrent(),0.001 ); // wrong node
        
        r.setElement(2, 0x31); // nn 54321
        mm.reply(r);
        Assert.assertEquals(8,mm.getCurrent(),0.001 );
        
        r.setElement(5, 0x12); // 4807mA
        r.setElement(6, 0xc7); // 4807mA
        mm.reply(r);
        Assert.assertEquals(4807,mm.getCurrent(),0.001 );
        
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(7);
        m.setElement(0, CbusConstants.CBUS_ACON2);
        m.setElement(1, 0xd4); // nn 54321
        m.setElement(2, 0x31); // nn 54321
        m.setElement(3, 0x00); // en1
        m.setElement(4, 0x01); // en1
        m.setElement(5, 0x00); // 0mA
        m.setElement(6, 0x00); // 0mA
        
        mm.message(m);
        Assert.assertEquals(4807,mm.getCurrent(),0.001 ); // CanMessage Ignored
        
        r = new CanReply(tcis.getCanid());
        r.setNumDataElements(7);
        r.setElement(0, CbusConstants.CBUS_ACON2);
        r.setElement(1, 0xd4); // nn 54321
        r.setElement(2, 0x31); // nn 54321
        r.setElement(3, 0x00); // en1
        r.setElement(4, 0x01); // en1
        r.setElement(5, 0x00); // 0mA
        r.setElement(6, 0x00); // 0mA
        
        mm.reply(r);
        Assert.assertEquals(0,mm.getCurrent(),0.001 );
        
        
        r = new CanReply(tcis.getCanid());
        r.setNumDataElements(7);
        r.setElement(0, CbusConstants.CBUS_ACON2);
        r.setElement(1, 0xd4); // nn 54321
        r.setElement(2, 0x31); // nn 54321
        r.setElement(3, 0x00); // en1
        r.setElement(4, 0x01); // en1
        r.setElement(5, 0x12); // 4807mA
        r.setElement(6, 0xc7); // 4807mA
        
        r.setRtr(true);
        
        mm.reply(r);
        Assert.assertEquals(0,mm.getCurrent(),0.001 );
        
        r.setExtended(true);
        r.setRtr(false);
        
        mm.reply(r);
        Assert.assertEquals(0,mm.getCurrent(),0.001 );
        
        r.setExtended(false);
        mm.reply(r);
        Assert.assertEquals(4807,mm.getCurrent(),0.001 );
        
        mm.disable();
        
        nodeModel.dispose();
        testCs.dispose();
        
    }
    
    @Test
    @Override
    public void testUpdateAndGetVoltage(){
        Assert.assertEquals("no voltage", false, mm.hasVoltage() );
    }
    
    @Test
    public void testSmallFuncs(){
        
        CbusMultiMeter mm = new CbusMultiMeter(memo);
        
        mm.requestUpdateFromLayout();
        mm.initializeHardwareMeter();
        Assert.assertEquals("name", "CBUS", mm.getHardwareMeterName() );
        
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusMultiMeterTest.class);

}
