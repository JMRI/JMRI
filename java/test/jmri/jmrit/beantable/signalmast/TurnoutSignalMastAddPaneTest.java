package jmri.jmrit.beantable.signalmast;

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
public class TurnoutSignalMastAddPaneTest {

    @Test
    public void testSetMast() {
        TurnoutSignalMast s1 = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        TurnoutSignalMastAddPane vp = new TurnoutSignalMastAddPane();
        
        Assert.assertFalse(vp.canHandleMast(null));
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        vp.setMast(s1);
        vp.setMast(m1);
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
