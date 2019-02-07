package jmri.jmrit.beantable.signalmast;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import java.util.*;

import org.junit.*;

/**
 * @author	Bob Jacobsen Copyright 2018
 */
public class MatrixSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    protected SignalMastAddPane getOTT() { return new MatrixSignalMastAddPane(); }    
    
    @Test
    @Ignore("causes missing data for other tests?")
    public void testSetMastOK() {
        MatrixSignalMast s1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();
        
        Assert.assertTrue(vp.canHandleMast(s1));
        
        vp.setMast(null);
        
        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                public Enumeration<String> getAspects() {
                    return java.util.Collections.enumeration(
                        java.util.Arrays.asList(
                            new String[]{"Approach","Stop","Unlit"}));
                    }
            }
            , null);
        vp.setMast(s1);
        
    }

    @Test
    @Ignore("causes missing data for other tests?")
    public void testSetMastReject() {
        TurnoutSignalMast m1 = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)", "user name");

        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();
        
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
                
        vp.setAspectNames(m1.getAppearanceMap(), null);
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");
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
