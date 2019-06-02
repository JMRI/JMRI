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
    @SuppressWarnings("unused") // it1 etc. are indirectly used as NamedBeans IT1 etc.
    public void testSetMastOK() {
        MatrixSignalMast s1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user name");
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        Turnout it3 = InstanceManager.turnoutManagerInstance().provideTurnout("IT3");
        Turnout it4 = InstanceManager.turnoutManagerInstance().provideTurnout("IT4");
        Turnout it5 = InstanceManager.turnoutManagerInstance().provideTurnout("IT5");
        Turnout it6 = InstanceManager.turnoutManagerInstance().provideTurnout("IT666");
        // m.setBitNum(6); // defaults to 6
        s1.setOutput("output1", "IT1");
        s1.setOutput("output2", "IT2");
        s1.setOutput("output3", "IT3");
        s1.setOutput("output4", "IT4");
        s1.setOutput("output5", "IT5");
        s1.setOutput("output6", "IT666");

        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();
        
        Assert.assertTrue(vp.canHandleMast(s1));
        
        vp.setMast(null);
        vp.setMast(s1);

        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IF$xsm:basic:one-low($0001)-3t") {
                public Enumeration<String> getAspects() {
                    return java.util.Collections.enumeration(
                        java.util.Arrays.asList(
                            new String[]{"Approach","Stop","Unlit"}));
                    }
            }
            , null);
    }

    @Test
    public void testSetMastReject() {
        TurnoutSignalMast m1 = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)", "user name");

        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();
        
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
                
        vp.setAspectNames(m1.getAppearanceMap(), null);
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$tsm:basic:one-searchlight($1) jmri.implementation.TurnoutSignalMast");
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
