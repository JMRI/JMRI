package jmri.jmrit.beantable.block;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.jmrit.beantable.AbstractBeanTableDataModelBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Steve Young (C) 2021
 */
public class BlockTableDataModelTest extends AbstractBeanTableDataModelBase<Block> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 17;
    }
    
    @Test
    @Override
    public void testGetBaseColumnNames() {
        assertEquals("Column0 - Bean toString",Bundle.getMessage("ColumnSystemName"), t.getColumnName(0));
        assertEquals("Column1 - UserName",Bundle.getMessage("ColumnUserName"), t.getColumnName(1));
        assertEquals("Column2 - Bean value",Bundle.getMessage("BlockValue"), t.getColumnName(2));
        assertEquals("Column3 - User Comment",Bundle.getMessage("ColumnComment"), t.getColumnName(3));
        assertEquals("Column4 - Delete button","", t.getColumnName(4));
        
    }

    @Test
    public void testAllocatedValueColumn() {
        Block b = InstanceManager.getDefault(BlockManager.class).provide("IB1");
        BlockTableDataModel m = (BlockTableDataModel) t;
        assertEquals("only IB1", 1, m.getRowCount());
        int row = 0;
        assertEquals("Allocated Value", m.getColumnName(BlockTableDataModel.ALLOCATEDVALUECOL));
        assertEquals("", m.getValueAt(row, BlockTableDataModel.ALLOCATEDVALUECOL));
        b.setAllocatedValue("AAA");
        assertEquals("AAA", m.getValueAt(row, BlockTableDataModel.ALLOCATEDVALUECOL));
        assertEquals("the value is separate", "", m.getValueAt(row, BlockTableDataModel.VALUECOL));
        Assert.assertFalse("read only", m.isCellEditable(row, BlockTableDataModel.ALLOCATEDVALUECOL));
        // an unknown column logs an error, which fails the test
        Assert.assertTrue("has a width", m.getPreferredWidth(BlockTableDataModel.ALLOCATEDVALUECOL) > 0);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        t = new BlockTableDataModel(InstanceManager.getDefault(BlockManager.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(BlockTableDataModelTest.class);

}
