package jmri.jmrit.beantable.signalmast;

import java.util.*;
import javax.swing.*;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import org.junit.*;

import org.netbeans.jemmy.operators.*;

/**
 * @author	Bob Jacobsen Copyright 2018
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
        
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        
        vp.setAspectNames(s1.getAppearanceMap(), InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(s1);
        
        vp.setAspectNames(m1.getAppearanceMap(), InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
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
                
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name"));
        Assert.assertEquals("PL-2-high", InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name").getMastType());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName("IF$vsm:AAR-1946:PL-2-high($0005)"));
        
    }

    @Test
    public void testCreateAndDisableViaGui() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        Assert.assertEquals(0, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        
        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();

        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                @Override
                public Enumeration<String> getAspects() {
                    return java.util.Collections.enumeration(
                        java.util.Arrays.asList(
                            new String[]{"Clear","Approach Medium","Advance Approach"}));
                    }
            }
                , InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        
        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        frame.pack();
        frame.setVisible(true);
        
        JFrameOperator frameOp = new JFrameOperator("Add/Edit Signal Mast");
        JCheckBoxOperator bBox = new JCheckBoxOperator(frameOp, "Approach Medium");
        
        // disable Approach Medium
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            bBox.push();
            vp.createMast("AAR-1946", "appearance-PL-2-high.xml", "user name 1");
        });

        // check list of SignalMasts
        Assert.assertEquals(1, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1"));
        // system name not checked, depends on history of how many VirtualSignalMast objects have been created

        // check aspect disabled
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1").isAspectDisabled("Approach Medium"));
        Assert.assertFalse(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 1").isAspectDisabled("Clear"));

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.dispose();
        });
    }

    @Test
    public void testEditAndDisableViaGui() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        Assert.assertEquals(0, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        VirtualSignalMast mast = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "user name 2"){
            { setLastRef(7); } // reset references - this leads to $0007 below, just in case anybody else has created one
        };
        InstanceManager.getDefault(jmri.SignalMastManager.class).register(mast);
        Assert.assertEquals(1, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        mast.setAspectDisabled("Stop");
        mast.setAspectDisabled("Unlit"); // we will reenable this below
        
        VirtualSignalMastAddPane vp = new VirtualSignalMastAddPane();
        
        vp.setAspectNames(
            new jmri.implementation.DefaultSignalAppearanceMap("IM123") {
                @Override
                public Enumeration<String> getAspects() { return mast.getAllKnownAspects().elements(); }
            }
                , InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(mast);
              
        JFrame frame = new JFrame("Add/Edit Signal Mast");
        frame.add(vp);
        frame.pack();
        frame.setVisible(true);
        
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
        Assert.assertEquals(1, InstanceManager.getDefault(jmri.SignalMastManager.class).getObjectCount());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2"));
        // system name not checked, depends on history of how many VirtualSignalMast objects have been created

        // check correct aspect disabled
        Assert.assertFalse(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Clear"));
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Approach"));
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Stop"));
        Assert.assertFalse(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name 2").isAspectDisabled("Unlit"));

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.dispose();
        });
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
