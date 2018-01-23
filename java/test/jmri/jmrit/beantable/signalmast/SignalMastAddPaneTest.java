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
        // group these in a single test, as the services can only be loaded once.
        Assert.assertNotNull(SignalMastAddPane.getInstancesCollection());
        Assert.assertNotNull(SignalMastAddPane.getInstancesMap());
        Assert.assertTrue(SignalMastAddPane.getInstancesMap().size() > 0); // found at least one service
        Assert.assertEquals(SignalMastAddPane.getInstancesMap().size(), SignalMastAddPane.getInstancesCollection().size()); // same size
        
        // check map is in sorted order; also check lookup works
        Map<String, SignalMastAddPane> map = SignalMastAddPane.getInstancesMap();
        Collection<SignalMastAddPane> collection = SignalMastAddPane.getInstancesCollection();
        String last = "";
        for (String name : map.keySet()) {
            Assert.assertTrue(name.compareTo(last) > 0);  // no identical ones
            Assert.assertTrue(collection.contains(map.get(name)));
            last = name;
        }

        // check collection in in sorted order
        last = "";
        for (SignalMastAddPane pane : collection) {
            Assert.assertTrue(pane.getPaneName().compareTo(last) > 0);  // no identical ones
            last = pane.getPaneName();
        }
        
        // partial check that results are unmodifiable
        try {
            SignalMastAddPane.getInstancesMap().put("Foo", null);
            Assert.fail("Should have thrown");
        } catch (java.lang.UnsupportedOperationException e) {
            // this is a pass
        }
        try {
            SignalMastAddPane.getInstancesCollection().add(null);
            Assert.fail("Should have thrown");
        } catch (java.lang.UnsupportedOperationException e) {
            // this is a pass
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
