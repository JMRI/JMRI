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
    @Ignore("causes exception, needs more setup, see comments")
    /* generates the following:
     * ERROR - Trying to get aspect Approach but it has not been configured [main] jmri.implementation.MatrixSignalMast.getBitsForAspect()
     * ERROR - Trying to get aspect Stop but it has not been configured [main] jmri.implementation.MatrixSignalMast.getBitsForAspect()
     * ERROR - Trying to get aspect Unlit but it has not been configured [main] jmri.implementation.MatrixSignalMast.getBitsForAspect()
     * ERROR - Trying to read name of output 1 which has not been configured [main] jmri.implementation.MatrixSignalMast.getOutputName()
     * ERROR - Trying to read name of output 2 which has not been configured [main] jmri.implementation.MatrixSignalMast.getOutputName()
     * ERROR - Trying to read name of output 3 which has not been configured [main] jmri.implementation.MatrixSignalMast.getOutputName()
     * ERROR - Trying to read name of output 4 which has not been configured [main] jmri.implementation.MatrixSignalMast.getOutputName()
     * ERROR - Trying to read name of output 5 which has not been configured [main] jmri.implementation.MatrixSignalMast.getOutputName()
     * ERROR - Trying to read name of output 6 which has not been configured [main] jmri.implementation.MatrixSignalMast.getOutputName()
     */
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
