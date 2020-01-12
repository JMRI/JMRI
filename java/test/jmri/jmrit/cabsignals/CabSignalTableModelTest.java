package jmri.jmrit.cabsignals;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CabSignalTableModel
 *
 * @author Paul Bender Copyright (C) 2019
 * @author Steve Young Copyright (c) 2019
 */
public class CabSignalTableModelTest {
        
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CabSignalTableModel model = new CabSignalTableModel(5,CabSignalTableModel.MAX_COLUMN);
        Assert.assertNotNull("exists", model);
    }
    
    public void testColumns() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CabSignalTableModel t = new CabSignalTableModel(5,CabSignalTableModel.MAX_COLUMN);
        
        Assert.assertEquals("column count",CabSignalTableModel.MAX_COLUMN,t.getColumnCount());
        Assert.assertEquals("default row count",0,t.getRowCount());
        
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.LOCO_ID_COLUMN).contains("loco"));
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.CURRENT_BLOCK).contains("loco"));
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.BLOCK_DIR).contains("block"));
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN).contains("block"));
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.NEXT_BLOCK).contains("next bl"));
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.NEXT_SIGNAL).contains("next sig"));
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.NEXT_ASPECT).contains("next as"));
        Assert.assertTrue(t.getColumnName(CabSignalTableModel.SEND_CABSIG_COLUMN).contains("signal data"));
        Assert.assertTrue(t.getColumnName(999).contains("unknown"));
        
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.LOCO_ID_COLUMN)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.CURRENT_BLOCK)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.BLOCK_DIR)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.NEXT_BLOCK)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.NEXT_SIGNAL)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.NEXT_ASPECT)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth( CabSignalTableModel.SEND_CABSIG_COLUMN)> 0);
        Assert.assertTrue(CabSignalTableModel.getPreferredWidth(999)> 0);
        JUnitAppender.assertErrorMessageStartsWith("no width found col 999");
        
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.LOCO_ID_COLUMN) == jmri.LocoAddress.class );
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.CURRENT_BLOCK) == String.class );
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.BLOCK_DIR) == String.class );
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN) == javax.swing.JButton.class );
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.NEXT_BLOCK) == String.class );
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.NEXT_SIGNAL) == String.class );
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.NEXT_ASPECT) == String.class );
        Assert.assertTrue(t.getColumnClass(CabSignalTableModel.SEND_CABSIG_COLUMN) == Boolean.class );
        Assert.assertTrue(t.getColumnClass(999) == null );
        JUnitAppender.assertErrorMessageStartsWith("no column class");
        
        t.dispose();
        t = null;
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
