package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

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
        
        TriStateJCheckBox t = new TriStateJCheckBox();

        t.setSelected(true);
        Assert.assertEquals("selected", true, t.isSelected());
        Assert.assertEquals("checked", TriStateJCheckBox.State.CHECKED, t.getState());

        t.setSelected(false);
        Assert.assertEquals("not selected", false, t.isSelected());
        Assert.assertEquals("unchecked", TriStateJCheckBox.State.UNCHECKED, t.getState());
        
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
