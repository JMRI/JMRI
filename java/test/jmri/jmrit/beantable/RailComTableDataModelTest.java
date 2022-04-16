package jmri.jmrit.beantable;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.RailComManager;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Steve Young (C) 2021
 */
public class RailComTableDataModelTest extends AbstractBeanTableDataModelBase<IdTag>{
    
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 12;
    }
    
    @Test
    @Override
    public void testGetWidthNoColumn() {
        Assert.assertTrue("Default column 5",t.getPreferredWidth(t.getColumnCount())>0);
    }
    
    @Test
    @Override
    public void testSetValueAtNoColumn() {
        createBean(); // ensure there is a row 0
        t.setValueAt(null,0,t.getColumnCount());
    }
    
    @Test
    @Override
    public void testGetBaseColumnClasses() {
    }
    
    @Test
    @Override
    public void testGetBaseColumnNames() {
    }
    
    @Test
    @Override
    public void testGetColumnClassNone() {
        assertEquals("Default column class String",String.class,t.getColumnClass(t.getColumnCount()) );
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        t = new RailComTableDataModel(InstanceManager.getDefault(RailComManager.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        InstanceManager.getDefault(IdTagManager.class).dispose(); // kills shutdown task
        JUnitUtil.tearDown();
    }
    
}
