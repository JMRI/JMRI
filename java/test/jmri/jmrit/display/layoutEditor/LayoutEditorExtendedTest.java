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
        Turnout layoutManagerTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        layoutManagerTurnout.setState(Turnout.THROWN);
        
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        Turnout manualTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        manualTurnout.setState(Turnout.CLOSED);
        
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        Assert.assertTrue("Turnout is thrown", turnout.getState() == Turnout.THROWN);
        
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        Assert.assertFalse("Turnout is thrown", turnout.getState() == Turnout.THROWN);
        
        // Verify that the beans don't match each other in both instance managers
        Assert.assertFalse("the beans differ in the instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        jmri.util.JUnitAppender.assertErrorMessage(
                "InstanceManagerA has item IT3 with state 4 and InstanceManagerB has item IT3 with state 2 but they differ in state");
        
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        turnout.setState(Turnout.CLOSED);
        Assert.assertTrue("Turnout is closed", turnout.getState() == Turnout.CLOSED);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertTrue("the beans in both instance managers are equal",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
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
