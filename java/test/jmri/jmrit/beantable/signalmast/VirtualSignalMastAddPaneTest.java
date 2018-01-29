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

        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        
        Assert.assertTrue(vp.setMast(s1));
        Assert.assertFalse(vp.setMast(m1));
        
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
