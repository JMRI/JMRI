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
public class MatrixSignalMastAddPaneTest {

    @Test
    public void testSetMast() {
        MatrixSignalMast s1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        TurnoutSignalMast m1 = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)", "user name");

        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();
        
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        
        vp.setAspectNames(s1.getAppearanceMap());
        vp.setMast(s1);
        
        vp.setAspectNames(m1.getAppearanceMap());
        vp.setMast(m1);
        // uncomment later, after migration
        //JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");
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
