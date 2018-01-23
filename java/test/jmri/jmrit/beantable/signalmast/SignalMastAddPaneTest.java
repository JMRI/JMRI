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
        // ground these in a single test, as the services can only be loaded once.
        Assert.assertNotNull(SignalMastAddPane.getInstancesCollection());
        Assert.assertNotNull(SignalMastAddPane.getInstancesMap());
        Assert.assertTrue(SignalMastAddPane.getInstancesMap().size() > 0); // found something
        Assert.assertEquals(SignalMastAddPane.getInstancesMap().size(), SignalMastAddPane.getInstancesCollection().size());
        
        // check map sorted order, lookup
        Map<String, SignalMastAddPane> map = SignalMastAddPane.getInstancesMap();
        Collection<SignalMastAddPane> collection = SignalMastAddPane.getInstancesCollection();
        String last = "";
        for (String name : map.keySet()) {
            Assert.assertTrue(name.compareTo(last) > 0);  // no identical ones
            Assert.assertTrue(collection.contains(map.get(name)));
            last = name;
        }

        // check collection sorted order
        last = "";
        for (SignalMastAddPane pane : collection) {
            Assert.assertTrue(pane.getPaneName().compareTo(last) > 0);  // no identical ones
            last = pane.getPaneName();
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
