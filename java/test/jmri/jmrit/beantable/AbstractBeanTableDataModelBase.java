package jmri.jmrit.beantable;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import jmri.Manager;
import jmri.ProvidingManager;
import jmri.NamedBean;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import static org.junit.Assert.*;

/**
 * This is an abstract base class for testing bean table data models.
 * This contains a base level of testing for these objects.
 *
 * @param <B> supported type of NamedBean
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public abstract class AbstractBeanTableDataModelBase<B extends NamedBean> {

    /**
     * The Bean Table Model under test.
     * Should be constructed in the setUp() of the tested class.
     */
    protected BeanTableDataModel<B> t;
    
    /**
     * Get Number of Columns in tested model.
     * To be overridden by tested class.
     * @return number of cols.
     */
    abstract protected int getModelColumnCount();
    
    /**
     * Create a NamedBean to use in the Table.
     * @return the NamedBean, hardware address normally 1, e.g. IS1.
     */
    protected NamedBean createBean(){
        Manager<?> mgr = t.getManager(); // Internal Proxy Bean<?> Manager
        Assert.assertNotNull("Table Bean Manager exists",mgr);
        if (mgr instanceof ProvidingManager){
            NamedBean b = ((ProvidingManager<?>) mgr).provide(mgr.getSystemNamePrefix()+"1");
            Assert.assertNotNull("Bean created",b);
            return b;
        } else {
            Assert.fail("Manager is not a providing manager, this test should be overridden to create a bean");
            return null;
        }
    }
    
    @Test
    public void testTableNotNull() {
        Assert.assertNotNull("Table exists in tested class",t);
    }
    
    @Test
    public void testGetRowCount(){
        assertEquals("0 Rows when model created",0, t.getRowCount());
        createBean();
        assertEquals("1 Row when NamedBean created",1, t.getRowCount());
    }
    
    /**
     * Test of getColumnCount method.
     * Should NOT overridden by implementing Tests
     */
    @Test
    public void testGetColumnCount() {
        assertEquals("Correct Number Columns",getModelColumnCount(), t.getColumnCount());
    }

    /**
     * Test of getColumnName method, of base class BeanTableDataModel.
     * Should NOT overridden by implementing Tests as these columns should
     * be present on all Bean Tables.
     */
    @Test
    public void testGetBaseColumnNames() {
        assertEquals("Column0 - Bean toString",Bundle.getMessage("ColumnSystemName"), t.getColumnName(0));
        assertEquals("Column1 - UserName",Bundle.getMessage("ColumnUserName"), t.getColumnName(1));
        assertEquals("Column2 - Bean value",Bundle.getMessage("ColumnState"), t.getColumnName(2));
        assertEquals("Column3 - User Comment",Bundle.getMessage("ColumnComment"), t.getColumnName(3));
        assertEquals("Column4 - Delete button","", t.getColumnName(4));
        
    }
    
    /**
     * Test of getColumn Name method with no column in model.
     * Should NOT be overridden by implementing Tests
     */
    @Test
    public void testGetColumnNameNone() {
        assertEquals("No column, no name via super.","btm unknown",t.getColumnName(t.getColumnCount()) );
    }
    
    /**
     * Test of isCellEditable Name method with no column in model.
     * Should NOT be overridden by implementing Tests
     */
    @Test
    public void testCellEditableColumnNone() {
        createBean(); // ensure there is a row 0
        assertEquals("No column, not editable",false,t.isCellEditable(0,t.getColumnCount()) );
    }
    
    /**
     * Test of getValueAt method with no column in model.
     * Should NOT be overridden by implementing Tests
     */
    @Test
    public void testGetValueAtNoColumn() {
        createBean(); // ensure there is a row 0
        assertEquals("No column, no value",null,t.getValueAt(0,t.getColumnCount()) );
        JUnitAppender.assertErrorMessage("internal state inconsistent with table requst for getValueAt 0 "+t.getColumnCount());
    }
    
    /**
     * Test of setValueAt method with no column in model.
     * Should NOT be overridden by implementing Tests.
     */
    @Test
    public void testSetValueAtNoColumn() {
        createBean(); // ensure there is a row 0
        t.setValueAt(null,0,t.getColumnCount());
        JUnitAppender.assertErrorMessage("btdm setvalueat 0 " + t.getColumnCount());
    }
    
    /**
     * Test of getValueAt method with no column in model.
     * Should NOT be overridden by implementing Tests
     */
    @Test
    public void testGetWidthNoColumn() {
        Assert.assertTrue("No column, no value",t.getPreferredWidth(t.getColumnCount())>0);
        JUnitAppender.assertErrorMessageStartsWith("Unexpected column in getPreferredWidth: "+t.getColumnCount());
    }
    
    /**
     * Test of getColumnClass method with no column in model.
     * Should NOT be overridden by implementing Tests
     */
    @Test
    public void testGetColumnClassNone() {
        assertEquals("No column, no class",null,t.getColumnClass(t.getColumnCount()) );
    }

    /**
     * Test of getColumnClass method, of base class BeanTableDataModel.
     * Should NOT overridden by implementing Tests.
     */
    @Test
    public void testGetBaseColumnClasses() {
        assertEquals("Col 0 Bean",NamedBean.class,t.getColumnClass(0) );
        assertEquals("Col 1 UserName",String.class,t.getColumnClass(1) );
        // col 2 could be graphic of current bean state or button to cange state
        assertEquals("Col 3 User Comment",String.class,t.getColumnClass(3) );
        assertEquals("Col 4 Delete Button",javax.swing.JButton.class,t.getColumnClass(4) );
    }
    
    @Test
    public void testNBFromUserSystemNames(){
        assertNull("UserName empty not found",t.getByUserName(""));
        assertNull("UserName not found",t.getByUserName("NOT A USER NAME"));
        assertNull("SystemName not found",t.getBySystemName("NOT A SYSTEM NAME"));
        
        NamedBean nb = createBean();
        nb.setUserName("NB UserName");
        assertEquals("NB Not Found via getByUserName",nb,t.getByUserName("NB UserName"));
        assertEquals("NB Not Found via getBySystemName",nb,t.getBySystemName(nb.getSystemName()));
    }

    /**
     * A bean the table does not list must not produce a row update: its index
     * in the name list is -1, which as a row number means TableModelEvent.HEADER_ROW.
     */
    @Test
    public void testPropertyChangeBeanNotInTable() {
        createBean(); // ensure there is a row 0
        AtomicInteger headerEvents = new AtomicInteger();
        t.addTableModelListener(e -> {
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                headerEvents.incrementAndGet();
            }
        });

        NamedBean notInTable = new jmri.implementation.DefaultIdTag("ID_NOT_IN_TABLE");
        t.propertyChange(new PropertyChangeEvent(notInTable, "KnownState", 0, 1));

        assertEquals("no header row update for a bean not in the table", 0, headerEvents.get());
    }

    /**
     * Bean notifications arrive on whichever thread changed the bean, but the
     * table model may only be updated on the GUI thread.
     */
    @Test
    public void testPropertyChangeUpdatesOnGuiThread() throws InterruptedException {
        NamedBean nb = createBean();
        AtomicBoolean notified = new AtomicBoolean(false);
        AtomicBoolean onGuiThread = new AtomicBoolean(false);
        t.addTableModelListener(e -> {
            onGuiThread.set(SwingUtilities.isEventDispatchThread());
            notified.set(true);
        });

        Thread other = new Thread(() -> t.propertyChange(
                new PropertyChangeEvent(nb, "KnownState", 0, 1)), "not the GUI thread");
        other.start();
        other.join();

        JUnitUtil.waitFor(notified::get, "table model notified of the change");
        assertTrue("table model updated on the GUI thread", onGuiThread.get());
    }

    /**
     * Derived classes should use this method to set t.
     */
    abstract public void setUp();

    /**
     * Derived classes should use this method to clean up after tests.
     */
    abstract public void tearDown();

    // private static final Logger log = LoggerFactory.getLogger(AbstractBeanTableDataModelBase.class);
}
