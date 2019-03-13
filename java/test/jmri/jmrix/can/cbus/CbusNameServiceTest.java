package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
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
public class CbusNameServiceTest {

    @Test
    public void testCTor() {
        CbusNameService t = new CbusNameService();
        Assert.assertNotNull("exists",t);
        t = null;
    }
    
    @Test
    public void testgetEventName() {
        CbusNameService t = new CbusNameService();
        Assert.assertEquals("no ev name no ev table","",t.getEventName(123,456));
        
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        
        CbusEventTableDataModel m = new CbusEventTableDataModel(memo, 2,CbusEventTableDataModel.MAX_COLUMN);
        Assert.assertNotNull("exists",m);
        m.addEvent(123,456, 0, null, "Event Name", "Node Name", "Comment", 0, 0, 0, 0);
        Assert.assertEquals("Event and Node Name","Event Name",t.getEventName(123,456));
        
        memo = null;
        tcis = null;
        t = null;
    }

    @Test
    public void testgetEventNodeString() {
        CbusNameService t = new CbusNameService();
        Assert.assertEquals("EventNodeStr","NN:123 EN:456 ",t.getEventNodeString(123,456));
        Assert.assertEquals("EventNodeStr nd 0","EN:456 ",t.getEventNodeString(0,456));
        
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        
        CbusEventTableDataModel m = new CbusEventTableDataModel(memo, 3,CbusEventTableDataModel.MAX_COLUMN);
        Assert.assertNotNull("exists",m);
        m.addEvent(123,456, 0, null, "Event Name", "Node Name", "Comment", 0, 0, 0, 0);
        m.addEvent(69,741, 0, null, "John Smith", "My Node", "My Comment", 0, 0, 0, 0);
        m.addEvent(0,357, 0, null, "Alonso", "Other Node Name", "My Second Comment", 0, 0, 0, 0);
        Assert.assertEquals("evstr Event and Node Name","NN:123 Node Name EN:456 Event Name ",t.getEventNodeString(123,456));
        Assert.assertEquals("evstr Not on table","NN:98 EN:76 ",t.getEventNodeString(98,76));
        Assert.assertEquals("js evstr Event and Node Name","NN:69 My Node EN:741 John Smith ",t.getEventNodeString(69,741));
        Assert.assertEquals("alonso evstr Event Name","EN:357 Alonso ",t.getEventNodeString(0,357));
        
        memo = null;
        tcis = null;
        t = null;
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNameServiceTest.class);

}
