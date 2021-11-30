package jmri.util.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JCheckBox;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Unit Tests for TriStateJCheckBox.
 * 
 * @author Steve Young Copyright (c) 2021
 */
public class TriStateJCheckBoxTest {
    
    @Test
    public void testCTor() {
        TriStateJCheckBox t = new TriStateJCheckBox();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testInitialState() {
        TriStateJCheckBox t = new TriStateJCheckBox();
        Assert.assertEquals("start not selected", false, t.isSelected());
        Assert.assertEquals("start unchecked", TriStateJCheckBox.State.UNCHECKED, t.getState());
    }

    @Test
    public void testSetGetState() {
        
        TriStateJCheckBox t = new TriStateJCheckBox();

        t.setState(TriStateJCheckBox.State.CHECKED);
        Assert.assertEquals("selected", true, t.isSelected());
        Assert.assertEquals("checked", TriStateJCheckBox.State.CHECKED, t.getState());

        t.setState(TriStateJCheckBox.State.UNCHECKED);
        Assert.assertEquals("not selected", false, t.isSelected());
        Assert.assertEquals("unchecked", TriStateJCheckBox.State.UNCHECKED, t.getState());

        t.setState(TriStateJCheckBox.State.PARTIAL);
        Assert.assertEquals("not selected partial", false, t.isSelected());
        Assert.assertEquals("partial", TriStateJCheckBox.State.PARTIAL, t.getState());
        
    }

    @Test
    public void testSetStateFromBoolean() {
        
        TriStateJCheckBox t = new TriStateJCheckBox();

        t.setState(new boolean[]{true});
        Assert.assertEquals("bool selected", true, t.isSelected());
        Assert.assertEquals("bool checked", TriStateJCheckBox.State.CHECKED, t.getState());

        t.setState(new boolean[]{false});
        Assert.assertEquals("bool not selected", false, t.isSelected());
        Assert.assertEquals("bool unchecked", TriStateJCheckBox.State.UNCHECKED, t.getState());

        t.setState(new boolean[]{true, false});
        Assert.assertEquals("bool not selected partial", false, t.isSelected());
        Assert.assertEquals("bool partial", TriStateJCheckBox.State.PARTIAL, t.getState());
        
        t.setState(new boolean[]{false, true, false});
        Assert.assertEquals("bool not selected partial", false, t.isSelected());
        Assert.assertEquals("bool partial", TriStateJCheckBox.State.PARTIAL, t.getState());
        
        t.setState(new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true});
        Assert.assertEquals("bool selected", true, t.isSelected());
        Assert.assertEquals("bool checked", TriStateJCheckBox.State.CHECKED, t.getState());

        t.setState(new boolean[]{false,true,true,true,true,true,true,true,true,true,true,true,true,true});
        Assert.assertEquals("bool selected", false, t.isSelected());
        Assert.assertEquals("bool partial", TriStateJCheckBox.State.PARTIAL, t.getState());
        
    }

    @Test
    public void testSetSelected() {
        
        TriStateJCheckBox t = new TriStateJCheckBox("");

        t.setSelected(true);
        Assert.assertEquals("selected", true, t.isSelected());
        Assert.assertEquals("checked", TriStateJCheckBox.State.CHECKED, t.getState());

        t.setSelected(false);
        Assert.assertEquals("not selected", false, t.isSelected());
        Assert.assertEquals("unchecked", TriStateJCheckBox.State.UNCHECKED, t.getState());
        
    }
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testClickable() {

        TriStateJCheckBox t = new TriStateJCheckBox("TriState");
        
        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle(t.getName()); // "TriState"
        
        f.pack();
        f.setVisible(true);
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( t.getName() );
        
        // Find hardware address field
        JLabelOperator jlo = new JLabelOperator(jfo,t.getName());
        
        
        JCheckBox jcb = (JCheckBox) jlo.getLabelFor();
        Assert.assertNotNull("tsjcb", jcb);
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jcb);
        
        Assert.assertTrue("visible", jcbo.isShowing());
        Assert.assertFalse("Not Selected", jcbo.isSelected());
        
        jcbo.doClick();
        Assert.assertTrue("Selected", jcbo.isSelected());
        
        jcbo.doClick();
        Assert.assertFalse("Back to not Selected", jcbo.isSelected());
        
        t.setState( new boolean[]{true, true});
        Assert.assertTrue("Selected from setState", jcbo.isSelected());
        
        t.setState( new boolean[]{true, false});
        Assert.assertFalse("Partial Not Selected from setState", jcbo.isSelected());
        
        jcbo.doClick();
        Assert.assertFalse("Still not Selected following click from partial", jcbo.isSelected());
        
        t.setState( new boolean[]{true, true});
        Assert.assertTrue("Selected from setState", jcbo.isSelected());
        
        t.setState( new boolean[]{false, false});
        Assert.assertFalse("Not Selected from setState", jcbo.isSelected());
        
        
        jlo.clickMouse();
        Assert.assertTrue("Selected from click Label", jcbo.isSelected());
        
        jlo.clickMouse();
        Assert.assertFalse("Not Selected from click Label", jcbo.isSelected());
        
        jlo.enterMouse();
        Assert.assertTrue(jcbo.isEnabled());
        
        jlo.exitMouse();
        Assert.assertTrue(jcbo.isEnabled());
        
        t.setEnabled(false);
        jcbo.doClick();
        Assert.assertFalse("Still not Selected following click as not Enabled", jcbo.isSelected());
        
        
        t.setState( new boolean[]{true, true});
        Assert.assertTrue("disabled Selected from setState ", jcbo.isSelected());
        
        t.setState( new boolean[]{true, false});
        Assert.assertFalse("disabled Partial Not Selected from setState", jcbo.isSelected());
        
        t.setSelected( true);
        Assert.assertTrue("disabled Selected from setSelected ", jcbo.isSelected());
        
        jlo.clickMouse();
        Assert.assertTrue("still Selected from setSelected ", jcbo.isSelected());
        
        // Ask to close window
        jfo.requestClose();
        
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
