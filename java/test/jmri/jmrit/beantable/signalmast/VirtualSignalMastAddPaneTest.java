package jmri.jmrit.beantable.signalmast;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import java.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author	Bob Jacobsen Copyright 2018
 */
public class VirtualSignalMastAddPaneTest {

    @Test
    public void testSetMast() {
        VirtualSignalMast s1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();
        
        Assert.assertFalse(vp.setMast(null));
        Assert.assertTrue(vp.setMast(s1));
        Assert.assertFalse(vp.setMast(m1));
        
    }

    @Test
    public void testCreateMast() {
        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();

        vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name");
                
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name"));
        System.err.println("ref "+InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name").getSystemName());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName("IF$vsm:AAR-1946:PL-2-high($0001)"));
        
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
