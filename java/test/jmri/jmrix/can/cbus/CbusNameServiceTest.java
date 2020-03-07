package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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
    }
    
    @Test
    public void testgetEventName() {
        CbusNameService t = new CbusNameService();
        Assert.assertEquals("no ev name no ev table","",t.getEventName(123,456));
        
        CbusEventTableDataModel m = new CbusEventTableDataModel(memo, 2,CbusEventTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusEventTableDataModel.class,m );
        
        m.addEvent(123,456, 0, null, "Event Name", "Comment", 0, 0, 0, 0);
        Assert.assertEquals("Event and Node Name","Event Name",t.getEventName(123,456));
        
        m.skipSaveOnDispose();
        m.dispose();
        
    }

    @Test
    public void testgetEventNodeString() {
        
        // memo.setTrafficController(tcis);
        
        CbusNameService t = new CbusNameService(memo);
        
        Assert.assertEquals("EventNodeStr","NN:123 EN:456 ",t.getEventNodeString(123,456));
        Assert.assertEquals("EventNodeStr nd 0","EN:456 ",t.getEventNodeString(0,456));
        
        CbusEventTableDataModel m = new CbusEventTableDataModel(
            memo, 5, CbusEventTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusEventTableDataModel.class,m );
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        
        nodeModel.provideNodeByNodeNum(123).setUserName("Node Name");
        nodeModel.provideNodeByNodeNum(69).setUserName("My Node");
        
        m.addEvent(123,456, 0, null, "Event Name", "Comment", 0, 0, 0, 0);
        m.addEvent(69,741, 0, null, "John Smith", "My Comment", 0, 0, 0, 0);
        m.addEvent(0,357, 0, null, "Alonso", "My Second Comment", 0, 0, 0, 0);
        Assert.assertEquals("evstr Event and Node Name","NN:123 Node Name EN:456 Event Name ",t.getEventNodeString(123,456));
        Assert.assertEquals("evstr Not on table","NN:98 EN:76 ",t.getEventNodeString(98,76));
        Assert.assertEquals("js evstr Event and Node Name","NN:69 My Node EN:741 John Smith ",t.getEventNodeString(69,741));
        Assert.assertEquals("alonso evstr Event Name","EN:357 Alonso ",t.getEventNodeString(0,357));
        
        m.skipSaveOnDispose();
        m.dispose();
        nodeModel.dispose();
    }

    private CanSystemConnectionMemo memo;
    private CbusPreferences pref;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // The minimal setup for log4J
    @Before
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        memo = new CanSystemConnectionMemo();
        pref = new CbusPreferences();
        jmri.InstanceManager.store(pref,CbusPreferences.class );
        
    }

    @After
    public void tearDown() {
        memo.dispose();
        memo = null;
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNameServiceTest.class);

}
