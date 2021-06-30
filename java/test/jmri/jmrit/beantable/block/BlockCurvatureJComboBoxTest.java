package jmri.jmrit.beantable.block;

import jmri.Block;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for BlockCurvatureJComboBox.
 * @author Steve Young Copyright (C) 2021
 */
public class BlockCurvatureJComboBoxTest {
    
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorWithCurve() {
        BlockCurvatureJComboBox tt = new BlockCurvatureJComboBox(Block.TIGHT);
        Assert.assertNotNull("exists",tt);
        assertEquals("set constructor curve tight", Block.TIGHT, tt.getCurvature());
    }
    
    @Test
    public void testSetCurvature() {
        
        assertEquals("get curve default", Block.NONE, t.getCurvature());
        
        t.setCurvature(Block.TIGHT);
        assertEquals("set get curve tight", Block.TIGHT, t.getCurvature());
        
        t.setCurvature(999);
        assertEquals("set get curve unknown value", Block.NONE, t.getCurvature());
        
        t.setCurvature(Block.SEVERE);
        assertEquals("set get curve severe", Block.SEVERE, t.getCurvature());
        
        t.setCurvature(Block.NONE);
        assertEquals("set get curve none", Block.NONE, t.getCurvature());
        
        t.setCurvature(Block.GRADUAL);
        assertEquals("set get curve gradual", Block.GRADUAL, t.getCurvature());
        
    }

    @Test
    public void testGetStringFromCurvature() {
        
        assertEquals("unknown none", Bundle.getMessage("BlockNone"), 
            BlockCurvatureJComboBox.getStringFromCurvature(999));
        assertEquals("BlockNone", Bundle.getMessage("BlockNone"), 
            BlockCurvatureJComboBox.getStringFromCurvature(Block.NONE));
        assertEquals("BlockGradual", Bundle.getMessage("BlockGradual"), 
            BlockCurvatureJComboBox.getStringFromCurvature(Block.GRADUAL));
        assertEquals("BlockTight", Bundle.getMessage("BlockTight"), 
            BlockCurvatureJComboBox.getStringFromCurvature(Block.TIGHT));
        assertEquals("BlockSevere", Bundle.getMessage("BlockSevere"), 
            BlockCurvatureJComboBox.getStringFromCurvature(Block.SEVERE));
        
    }

    @Test
    public void testGetCurvatureFromString() {
        assertEquals("empty none", Block.NONE, 
            BlockCurvatureJComboBox.getCurvatureFromString(""));
        assertEquals("null none", Block.NONE, 
            BlockCurvatureJComboBox.getCurvatureFromString(null));
        assertEquals("unmatched none", Block.NONE, 
            BlockCurvatureJComboBox.getCurvatureFromString("Not a Block Curvature"));
        assertEquals("BlockNone", Block.NONE, 
            BlockCurvatureJComboBox.getCurvatureFromString(Bundle.getMessage("BlockNone")));
        assertEquals("BlockGradual", Block.GRADUAL, 
            BlockCurvatureJComboBox.getCurvatureFromString(Bundle.getMessage("BlockGradual")));
        assertEquals("BlockTight", Block.TIGHT, 
            BlockCurvatureJComboBox.getCurvatureFromString(Bundle.getMessage("BlockTight")));
        assertEquals("BlockSevere", Block.SEVERE, 
            BlockCurvatureJComboBox.getCurvatureFromString(Bundle.getMessage("BlockSevere")));
    }
    
    @Test
    public void testGetCurvatureFromObject() {
        
        t.setCurvature(Block.SEVERE);
        assertEquals("BlockSevere found", Block.SEVERE, 
            BlockCurvatureJComboBox.getCurvatureFromObject(t));
        assertEquals("Block none not a combobox", Block.NONE, 
            BlockCurvatureJComboBox.getCurvatureFromObject("String, not a Curvature ComboBox"));
        
    }
    
    private BlockCurvatureJComboBox t;
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new BlockCurvatureJComboBox();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
