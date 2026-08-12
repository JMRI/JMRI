package jmri.jmrit.beantable.signalmast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

import javax.swing.*;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * @author Bob Jacobsen Copyright 2018
 */
public class MatrixSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() { return new MatrixSignalMastAddPane(); }

    @Test
    public void testSetMastOK() {
        MatrixSignalMast s1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user name");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT3");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT4");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT5");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT666");
        // s1.setBitNum(6); // defaults to 6
        s1.setOutput("output1", "IT1");
        s1.setOutput("output2", "IT2");
        s1.setOutput("output3", "IT3");
        s1.setOutput("output4", "IT4");
        s1.setOutput("output5", "IT5");
        s1.setOutput("output6", "IT666");

        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();

        assertTrue(vp.canHandleMast(s1));

        vp.setMast(null);
        vp.setMast(s1);

        SignalSystem basicSys = InstanceManager.getDefault(SignalSystemManager.class).getSystem("basic");
        assertNotNull(basicSys);

        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IF$xsm:basic:one-low($0001)-3t") {
                @Override
                public Enumeration<String> getAspects() {
                    return java.util.Collections.enumeration(
                        java.util.Arrays.asList(
                            new String[]{"Approach","Stop","Unlit"}));
                    }
            }
            , basicSys);
    }

    @Test
    public void testSetMastReject() {
        TurnoutSignalMast m1 = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)", "user name");

        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();

        assertFalse(vp.canHandleMast(m1));

        vp.setMast(null);

        SignalSystem basicSys = InstanceManager.getDefault(SignalSystemManager.class).getSystem("basic");
        assertNotNull(basicSys);

        vp.setAspectNames(m1.getAppearanceMap(), basicSys);
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$tsm:basic:one-searchlight($1) jmri.implementation.TurnoutSignalMast");
    }

    @Test
    @DisabledIfHeadless
    public void testEditAndDisableViaGui() {
        assertEquals(0, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        // create a mast
        InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        InstanceManager.turnoutManagerInstance().provideTurnout("IT3");

        MatrixSignalMast mast = new MatrixSignalMast("IF$xsm:basic:one-low($0002)-3t", "user name 2"){
            { setLastRef(3); } // reset references - this leads to $0003 below, just in case anybody else has created one
        };
        mast.setBitNum(3);
        mast.setOutput("output1", "IT1");
        mast.setOutput("output2", "IT2");
        mast.setOutput("output3", "IT3");

        mast.setBitstring("Clear", "111"); // used for test below
        mast.setBitstring("Approach", "100");
        mast.setBitstring("Stop", "001"); // used for test below
        mast.setBitstring("Unlit", "000");

        InstanceManager.getDefault(SignalMastManager.class).register(mast);
        assertEquals(1, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        mast.setAspectDisabled("Unlit"); // we will reenable this below
        mast.setAllowUnLit(true);

        // set up a mast edit pane
        MatrixSignalMastAddPane vp = new MatrixSignalMastAddPane();

        SignalSystem basicSys = InstanceManager.getDefault(SignalSystemManager.class).getSystem("basic");
        assertNotNull(basicSys);

        vp.setAspectNames(
                new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                    @Override
                    public Enumeration<String> getAspects() { return mast.getAllKnownAspects().elements(); }
                }
                , basicSys);
        vp.setMast(mast);

        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        ThreadingUtil.runOnGUI( () -> {
            frame.pack();
            frame.setVisible(true);
        });

        JFrameOperator frameOp = new JFrameOperator("Add/Edit Signal Mast");
        JCheckBoxOperator rBox = new JCheckBoxOperator(frameOp, Bundle.getMessage("ResetPrevious"));

        // enable Reset
        ThreadingUtil.runOnGUI(() -> {
            rBox.push(); // this should set Reset enabled
            vp.createMast("basic", "appearance-one-searchlight.xml", "user name 2");
        });

        // check list of SignalMasts
        assertEquals(1, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        SignalMast userName2 = InstanceManager.getDefault(SignalMastManager.class).getByUserName("user name 2");
        assertNotNull(userName2);
        // system name not checked, depends on history of how many MatrixSignalMast objects have been created

        // check correct aspect disabled
        assertFalse(userName2.isAspectDisabled("Stop"));
        assertTrue(userName2.isAspectDisabled("Unlit"));
        // check Reset setting in mast
        assertTrue(mast.resetPreviousStates());

        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
