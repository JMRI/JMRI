package jmri.jmrit.cabsignals;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CabSignalTableModel
 *
 * @author Paul Bender Copyright (C) 2019
 * @author Steve Young Copyright (c) 2019
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class CabSignalTableModelTest {
        
 
    @Test
    public void testCtor() {
        CabSignalTableModel model = new CabSignalTableModel(5,CabSignalTableModel.MAX_COLUMN);
        Assert.assertNotNull("exists", model);
        model.dispose();
    }

    @Test
    public void testColumns() {
        CabSignalTableModel t = new CabSignalTableModel(5,CabSignalTableModel.MAX_COLUMN);

        Assert.assertEquals("column count",CabSignalTableModel.MAX_COLUMN,t.getColumnCount());
        Assert.assertEquals("default row count",0,t.getRowCount());

        Assert.assertEquals(Bundle.getMessage("LocoID"), t.getColumnName(CabSignalTableModel.LOCO_ID_COLUMN));
        Assert.assertEquals(Bundle.getMessage("Block"), t.getColumnName(CabSignalTableModel.CURRENT_BLOCK));
        Assert.assertEquals(Bundle.getMessage("BlockDirection"), t.getColumnName(CabSignalTableModel.BLOCK_DIR));
        Assert.assertEquals(Bundle.getMessage("BlockButton"), t.getColumnName(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN));
        Assert.assertEquals(Bundle.getMessage("NextBlock"), t.getColumnName(CabSignalTableModel.NEXT_BLOCK));
        Assert.assertEquals(Bundle.getMessage("NextSignal"), t.getColumnName(CabSignalTableModel.NEXT_SIGNAL));
        Assert.assertEquals(Bundle.getMessage("NextAspect"), t.getColumnName(CabSignalTableModel.NEXT_ASPECT));
        Assert.assertEquals(Bundle.getMessage("SigDataOn"), t.getColumnName(CabSignalTableModel.SEND_CABSIG_COLUMN));
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
        JUnitAppender.assertWarnMessageStartsWith("no width found col 999");

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

    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
