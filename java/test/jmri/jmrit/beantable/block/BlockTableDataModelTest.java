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
        return 18;
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
    public void testTrainAndWhyColumns() {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        jmri.TrainRecord r = InstanceManager.getDefault(jmri.TrainRecordManager.class).newRecord("47012 Coal empties");
        jmri.util.ThreadingUtil.runOnGUI(() -> r.place(java.util.List.of(b), false));
        int row = 0;
        assertEquals(Bundle.getMessage("BlockTrainColName"), t.getColumnName(BlockTableDataModel.TRAINCOL));
        assertEquals(Bundle.getMessage("BlockWhyColName"), t.getColumnName(BlockTableDataModel.WHYCOL));
        jmri.util.JUnitUtil.waitFor(() -> "47012 Coal empties".equals(t.getValueAt(row, BlockTableDataModel.TRAINCOL)),
                "the train in the Train column");
        Assert.assertTrue(t.getValueAt(row, BlockTableDataModel.WHYCOL).toString().startsWith("Placed in IB1"));
        Assert.assertFalse("read only", t.isCellEditable(row, BlockTableDataModel.TRAINCOL));
        Assert.assertFalse("read only", t.isCellEditable(row, BlockTableDataModel.WHYCOL));
        Assert.assertNull("the Block's value is unchanged", b.getValue());
    }

    @Test
    public void testTrainColumnFollowsRecordsMadeLater() {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        jmri.TrainRecordManager trm = InstanceManager.getDefault(jmri.TrainRecordManager.class);
        jmri.TrainRecord r = trm.newRecord("Goods");
        java.util.concurrent.atomic.AtomicInteger changes = new java.util.concurrent.atomic.AtomicInteger();
        t.addTableModelListener(e -> changes.incrementAndGet());
        jmri.util.ThreadingUtil.runOnGUI(() -> r.place(java.util.List.of(b), false));
        jmri.util.JUnitUtil.waitFor(() -> "Goods".equals(t.getValueAt(0, BlockTableDataModel.TRAINCOL)),
                "a record made after the table, placed in the Block, in the Train column");
        jmri.TrainRecord r2 = trm.newRecord("Light engine");
        jmri.util.ThreadingUtil.runOnGUI(() -> r2.place(java.util.List.of(b), false));
        jmri.util.JUnitUtil.waitFor(() -> "Goods, Light engine".equals(t.getValueAt(0, BlockTableDataModel.TRAINCOL)),
                "two trains in one Block");
        Assert.assertEquals("why for each: " + t.getValueAt(0, BlockTableDataModel.WHYCOL), 2,
                t.getValueAt(0, BlockTableDataModel.WHYCOL).toString().split("Placed in IB1", -1).length - 1);
        jmri.util.ThreadingUtil.runOnGUI(() -> trm.remove(r2));
        jmri.util.ThreadingUtil.runOnGUI(() -> trm.remove(r));
        jmri.util.JUnitUtil.waitFor(() -> "".equals(t.getValueAt(0, BlockTableDataModel.TRAINCOL)),
                "gone when the record is removed");
        jmri.util.JUnitUtil.waitFor(() -> changes.get() > 0, "the table told");
    }

    @Test
    public void testTrainColumnFollowsRecordsMadeBefore() {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        jmri.TrainRecord r = InstanceManager.getDefault(jmri.TrainRecordManager.class).newRecord("Goods");
        BlockTableDataModel later = new BlockTableDataModel(InstanceManager.getDefault(BlockManager.class));
        jmri.util.ThreadingUtil.runOnGUI(() -> r.place(java.util.List.of(b), false));
        jmri.util.JUnitUtil.waitFor(() -> "Goods".equals(later.getValueAt(0, BlockTableDataModel.TRAINCOL)),
                "a record made before the table, placed in the Block afterwards");
        later.dispose();
    }

    @Test
    public void testTrainAndWhyColumnsHiddenUntilChosen() {
        javax.swing.JTable table = new javax.swing.JTable(t);
        jmri.util.swing.XTableColumnModel columns = new jmri.util.swing.XTableColumnModel();
        table.setColumnModel(columns);
        table.createDefaultColumnsFromModel();
        t.configureTable(table);
        Assert.assertFalse("Train column hidden", columns.isColumnVisible(columns.getColumnByModelIndex(BlockTableDataModel.TRAINCOL)));
        Assert.assertFalse("Why? column hidden", columns.isColumnVisible(columns.getColumnByModelIndex(BlockTableDataModel.WHYCOL)));
        Assert.assertTrue("Value column still shown", columns.isColumnVisible(columns.getColumnByModelIndex(BlockTableDataModel.VALUECOL)));
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
