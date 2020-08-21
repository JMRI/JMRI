package jmri.jmrix.can.cbus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import jmri.Light;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.eventtable.CbusEventBeanData;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.eventtable.CbusTableEvent;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        assertThat(t).isNotNull();
    }
    
    @Test
    public void testgetEventName() {
        CbusNameService t = new CbusNameService();
        assertThat(t.getEventName(123,456)).isEmpty();
        
        CbusEventTableDataModel m = new CbusEventTableDataModel(memo, 2,CbusEventTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusEventTableDataModel.class,m );
        
        CbusTableEvent tabEv = m.provideEvent(123, 456);
        tabEv.setName("Event Name");
        tabEv.setComment("Comment");
        
        assertEquals("Event Name",t.getEventName(123,456));
        
        m.skipSaveOnDispose();
        m.dispose();
        
    }

    @Test
    public void testgetEventNodeString() {
        
        CbusNameService t = new CbusNameService(memo);
        
        assertEquals("NN:123 EN:456 ",t.getEventNodeString(123,456));
        assertEquals("EN:456 ",t.getEventNodeString(0,456));
        
        CbusEventTableDataModel m = new CbusEventTableDataModel(
            memo, 5, CbusEventTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusEventTableDataModel.class,m );
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        
        nodeModel.provideNodeByNodeNum(123).setUserName("Node Name");
        nodeModel.provideNodeByNodeNum(69).setUserName("My Node");
        
        m.provideEvent(123, 456).setName("Event Name");
        m.provideEvent(123, 456).setComment("Event Comment");
                
        m.provideEvent(69, 741).setName("John Smith");
        m.provideEvent(69, 741).setComment("My Comment");
        
        m.provideEvent(0, 357).setName("Alonso");
        m.provideEvent(0, 357).setComment("My Second Comment");
        
        assertEquals("NN:123 Node Name EN:456 Event Name ",t.getEventNodeString(123,456));
        assertEquals("NN:98 EN:76 ",t.getEventNodeString(98,76));
        assertEquals("NN:69 My Node EN:741 John Smith ",t.getEventNodeString(69,741));
        assertEquals("EN:357 Alonso ",t.getEventNodeString(0,357));
        
        m.skipSaveOnDispose();
        m.dispose();
        nodeModel.dispose();
    }
    
    @Test
    public void testgetJmriBeans(){
    
        CbusNameService t = new CbusNameService(memo);
        
        CbusEventBeanData bd = t.getJmriBeans(0, 4, CbusEventDataElements.EvState.ON);
        assertThat(bd.toString()).isEmpty();
        
        CbusEventTableDataModel evModel = new CbusEventTableDataModel(
            memo, 5, CbusEventTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusEventTableDataModel.class,evModel );
        
        CbusLightManager lm = new CbusLightManager(memo);        
        Light lightA = lm.provideLight("+4");
        lightA.setUserName("MyLightA");
        
        evModel.provideEvent(0, 4).appendOnOffBean(lightA, true, CbusEvent.EvState.ON);
        evModel.provideEvent(0, 4).appendOnOffBean(lightA, false, CbusEvent.EvState.OFF);
        
        bd = t.getJmriBeans(0, 4, CbusEvent.EvState.ON);
        assertEquals("Light On: MyLightA",bd.toString());
        
        bd = t.getJmriBeans(0, 4, CbusEvent.EvState.OFF);
        assertEquals("Light Off: MyLightA",bd.toString());
        
        evModel.skipSaveOnDispose();
        evModel.dispose();
        
        lm.dispose();
    }

    private CanSystemConnectionMemo memo;
    private CbusPreferences pref;
    
    @TempDir 
    protected File tempDir;

    @BeforeEach
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        memo = new CanSystemConnectionMemo();
        pref = new CbusPreferences();
        jmri.InstanceManager.store(pref,CbusPreferences.class );
        
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNameServiceTest.class);

}
