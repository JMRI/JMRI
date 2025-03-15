package jmri.jmrit.beantable;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

import jmri.time.TimeProvider;
import jmri.time.TimeProviderManager;

/**
 *
 * @author Steve Young       (C) 2021 (MemoryTableDataModelTest)
 * @author Daniel Bergqvist  (C) 2025
 */
public class TimeProviderTableDataModelTest extends AbstractBeanTableDataModelBase<TimeProvider>{

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testPrefsCTor() {
        Assert.assertNotNull("exists",new TimeProviderTableDataModel());
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

    @Test
    @Override
    public void testGetRowCount(){
        // The rows are "IUInternalTimeProvider" and "IUSystemClock"
        assertEquals("0 Rows when model created",2, t.getRowCount());
        createBean();   // No bean is created by this method
        assertEquals("1 Row when NamedBean created",2, t.getRowCount());
    }

    @Test
    public void testGetValueClassToolTip() {
//        Clock m = (Clock)createBean();
        javax.swing.JTable tbl = new javax.swing.JTable();
        tbl.setName("Test Clock Value ToolTip Table");
//        Assertions.assertNull( t.getCellToolTip(tbl, 0, 0));
//        Assertions.assertNull( t.getCellToolTip(tbl, 0, ClockTableDataModel.VALUECOL));
//        m.setValue("A java.lang.String value");
//        Assertions.assertEquals("java.lang.String", t.getCellToolTip(tbl, 0, ClockTableDataModel.VALUECOL));

//        t.setValueAt(123, 0, ClockTableDataModel.VALUECOL);
//        Assertions.assertEquals("java.lang.Integer", t.getCellToolTip(tbl, 0, ClockTableDataModel.VALUECOL));

    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        t = new TimeProviderTableDataModel(InstanceManager.getDefault(TimeProviderManager.class));
    }

    /**
     * Create a NamedBean to use in the Table.
     * This method does not create a bean, it returns an existing bean.
     * @return the NamedBean.
     */
    @Override
    protected NamedBean createBean() {
        Manager<?> mgr = t.getManager();
        Assert.assertNotNull("Table Bean Manager exists",mgr);
        NamedBean b = mgr.getNamedBeanSet().first();
        Assert.assertNotNull("Bean exists", b);
        return b;
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
