package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SignalGroupSubTableActionTest {

    @Test
    public void testCTor() {
        SignalGroupSubTableAction t = new SignalGroupSubTableAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAddRemoveSignalGroupSensorModelListeners() {
        
        JUnitUtil.initInternalSensorManager();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Assert.assertEquals("No Sm Listeners at start",0, sm.getPropertyChangeListeners().length);

        JUnitUtil.initInternalTurnoutManager();
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        Assert.assertEquals("No Tm Listeners at start",0, tm.getPropertyChangeListeners().length);

        
        SignalGroupSubTableAction t = new SignalGroupSubTableAction();
        Assert.assertEquals("No Sm Listeners in SignalGroupSubTableAction",0, sm.getPropertyChangeListeners().length);
        Assert.assertEquals("No Tm Listeners in SignalGroupSubTableAction",0, tm.getPropertyChangeListeners().length);
        
        SignalGroupSubTableAction.SignalGroupSensorModel _SignalGroupSensorModel = t.new SignalGroupSensorModel();
        Assert.assertEquals("1 Sm Listener in SignalGroupSensorModel",1, sm.getPropertyChangeListeners().length);
        Assert.assertEquals("No Tm Listeners in SignalGroupSensorModel",0, tm.getPropertyChangeListeners().length);
        
        _SignalGroupSensorModel.dispose();
        Assert.assertEquals("0 Sm Listener in SignalGroupSensorModel",0, sm.getPropertyChangeListeners().length);
        Assert.assertEquals("No Tm Listeners in SignalGroupSensorModel",0, tm.getPropertyChangeListeners().length);
        
    }
    
    @Test
    public void testAddRemoveSignalGroupTurnoutModelListeners() {
        
        JUnitUtil.initInternalSensorManager();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Assert.assertEquals("No Sm Listeners at start",0, sm.getPropertyChangeListeners().length);

        JUnitUtil.initInternalTurnoutManager();
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        Assert.assertEquals("No Tm Listeners at start",0, tm.getPropertyChangeListeners().length);

        
        SignalGroupSubTableAction t = new SignalGroupSubTableAction();
        Assert.assertEquals("No Sm Listeners in SignalGroupSubTableAction",0, sm.getPropertyChangeListeners().length);
        Assert.assertEquals("No Tm Listeners in SignalGroupSubTableAction",0, tm.getPropertyChangeListeners().length);
        
        SignalGroupSubTableAction.SignalGroupTurnoutModel _SignalGroupTurnoutModel = t.new SignalGroupTurnoutModel();
        Assert.assertEquals("0 Sm Listener in SignalGroupTurnoutModel",0, sm.getPropertyChangeListeners().length);
        Assert.assertEquals("1 Tm Listener in SignalGroupTurnoutModel",1, tm.getPropertyChangeListeners().length);
        
        _SignalGroupTurnoutModel.dispose();
        Assert.assertEquals("0 Sm Listener in SignalGroupTurnoutModel",0, sm.getPropertyChangeListeners().length);
        Assert.assertEquals("No Tm Listeners in SignalGroupTurnoutModel",0, tm.getPropertyChangeListeners().length);
        
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalGroupSubTableActionTest.class);

}
