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

import org.netbeans.jemmy.operators.*;

/**
 * @author Bob Jacobsen Copyright 2018
 */
public class VirtualSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() { return new VirtualSignalMastAddPane(); }    

    @Test
    public void testSetMast() {
        VirtualSignalMast s1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();

        assertTrue(vp.canHandleMast(s1));
        assertFalse(vp.canHandleMast(m1));

        vp.setMast(null);

        SignalSystem basicSys = InstanceManager.getDefault(SignalSystemManager.class).getSystem("basic");
        assertNotNull(basicSys);

        vp.setAspectNames(s1.getAppearanceMap(), basicSys);
        vp.setMast(s1);

        vp.setAspectNames(m1.getAppearanceMap(), basicSys);
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");

    }

    @Test
    public void testCreateMast() {
        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();
        new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "no user name"){
            { setLastRef(4); } // reset references - this leads to $0005 below, just in case anybody else has created one
        };

        vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name");

        SignalMast aar1946 = InstanceManager.getDefault(SignalMastManager.class).getByUserName("user name");
        assertNotNull(aar1946);
        assertEquals("PL-2-high", aar1946.getMastType());
        assertNotNull(InstanceManager.getDefault(SignalMastManager.class).getBySystemName("IF$vsm:AAR-1946:PL-2-high($0005)"));

    }

    @Test
    @DisabledIfHeadless
    public void testCreateAndDisableViaGui() {
        assertEquals(0, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());

        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();
        SignalSystem basicSys = InstanceManager.getDefault(SignalSystemManager.class).getSystem("basic");
        assertNotNull(basicSys);

        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                @Override
                public Enumeration<String> getAspects() {
                    return java.util.Collections.enumeration(
                        java.util.Arrays.asList(
                            new String[]{"Clear","Approach Medium","Advance Approach"}));
                    }
            }
                , basicSys);

        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        ThreadingUtil.runOnGUI( () -> {
            frame.pack();
            frame.setVisible(true);
        });

        JFrameOperator frameOp = new JFrameOperator("Add/Edit Signal Mast");
        JCheckBoxOperator bBox = new JCheckBoxOperator(frameOp, "Approach Medium");

        // disable Approach Medium
        ThreadingUtil.runOnGUI(() -> {
            bBox.push();
            vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name 1");
        });

        // check list of SignalMasts
        assertEquals(1, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        SignalMast uName1 = InstanceManager.getDefault(SignalMastManager.class).getByUserName("user name 1");
        assertNotNull(uName1);
        // system name not checked, depends on history of how many VirtualSignalMast objects have been created

        // check aspect disabled
        assertTrue(uName1.isAspectDisabled("Approach Medium"));
        assertFalse(uName1.isAspectDisabled("Clear"));

        JUnitUtil.dispose(frame);

    }

    @Test
    @DisabledIfHeadless
    public void testEditAndDisableViaGui() {
        assertEquals(0, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        VirtualSignalMast mast = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "user name 2"){
            { setLastRef(7); } // reset references - this leads to $0007 below, just in case anybody else has created one
        };
        InstanceManager.getDefault(SignalMastManager.class).register(mast);
        assertEquals(1, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        mast.setAspectDisabled("Stop");
        mast.setAspectDisabled("Unlit"); // we will reenable this below

        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();

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
        JCheckBoxOperator aBox = new JCheckBoxOperator(frameOp, "Approach");
        JCheckBoxOperator uBox = new JCheckBoxOperator(frameOp, "Unlit");

        // disable Approach
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            aBox.push(); // this should set disabled
            uBox.push(); // this should set enabled
            vp.createMast("basic", "appearance-one-searchlight.xml", "user name 2");
        });

        // check list of SignalMasts
        assertEquals(1, InstanceManager.getDefault(SignalMastManager.class).getObjectCount());
        SignalMast userName2 = InstanceManager.getDefault(SignalMastManager.class).getByUserName("user name 2");
        assertNotNull(userName2);
        // system name not checked, depends on history of how many VirtualSignalMast objects have been created

        // check correct aspect disabled
        assertFalse(userName2.isAspectDisabled("Clear"));
        assertTrue(userName2.isAspectDisabled("Approach"));
        assertTrue(userName2.isAspectDisabled("Stop"));
        assertFalse(userName2.isAspectDisabled("Unlit"));

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
