package jmri.jmrit.beantable;

import jmri.Memory;
import jmri.MemoryManager;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Steve Young (C) 2021
 */
public class MemoryTableDataModelTest extends AbstractBeanTableDataModelBase<Memory>{

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testPrefsCTor() {
        Assert.assertNotNull("exists",new MemoryTableDataModel());
    }

    @Override
    public int getModelColumnCount(){
        return 5;
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

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        t = new MemoryTableDataModel(InstanceManager.getDefault(MemoryManager.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        JUnitUtil.tearDown();
    }

}
