package jmri.jmrit.display.layoutEditor;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Extended test of LayoutEditor
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class LayoutEditorExtendedTest {
    
    private InstanceManager getNewInstanceManager() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initIdTagManager();
        
        return InstanceManager.getDefault();
    }
    
    @Test
    public void testSomething() throws JmriException {
        String turnoutDeviceName = "3";
        InstanceManager layoutEditorInstanceManager = getNewInstanceManager();
        InstanceManager manualInstanceManager = getNewInstanceManager();
        
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        Turnout turnoutLayoutManager = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        turnoutLayoutManager.setState(Turnout.THROWN);
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        Turnout turnoutManual = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        turnoutManual.setState(Turnout.CLOSED);
        
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        Assert.assertTrue("Turnout is thrown", turnout.getState() == Turnout.THROWN);
        
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        Assert.assertTrue("Turnout is thrown", turnout.getState() == Turnout.THROWN);
    }
    
    // from here down is testing infrastructure
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }
    
    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
    
}
