package jmri.jmrit.beantable.signalmast;

import jmri.implementation.SignalSystemTestUtil;
import jmri.util.JUnitUtil;
import java.util.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author	Bob Jacobsen Copyright 2018
 */
public class SignalMastAddPaneTest {

    @Test
    public void testLoad() {
    
        Assert.assertNotNull(SignalMastAddPane.getInstancesCollection());
        Assert.assertNotNull(SignalMastAddPane.getInstancesMap());
        Assert.assertTrue(SignalMastAddPane.getInstancesMap().size() > 0); // found something
        
        // check sorted order
        Map<String, SignalMastAddPane> map = SignalMastAddPane.getInstancesMap();
        String last = "";
        for (String name : map.keySet()) {
            Assert.assertTrue(name.compareTo(last) > 0);  // no identical ones
            last = name;
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
