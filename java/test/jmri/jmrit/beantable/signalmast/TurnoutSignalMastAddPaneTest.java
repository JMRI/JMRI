package jmri.jmrit.beantable.signalmast;

import jmri.InstanceManager;
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
public class TurnoutSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() { return new TurnoutSignalMastAddPane(); }    
    
    @Test
    public void testSetMast() {
        TurnoutSignalMast s1 = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        TurnoutSignalMastAddPane vp = new TurnoutSignalMastAddPane();
        
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        
        vp.setAspectNames(s1.getAppearanceMap(), InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(s1);
        
        vp.setAspectNames(m1.getAppearanceMap(), InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
