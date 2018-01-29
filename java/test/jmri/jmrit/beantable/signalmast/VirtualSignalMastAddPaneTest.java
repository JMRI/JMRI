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
        
        Assert.assertFalse(vp.canHandleMast(null));
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        vp.setMast(s1);
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");

    }

    @Test
    public void testCreateMast() {
        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();
        new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "no user name"){
            { lastRef = 4; } // reset references - this leads to $0005 below, just in case anybody else has created one
        };
        
        vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name");
                
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName("IF$vsm:AAR-1946:PL-2-high($0005)"));
        
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
