package jmri.swing;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import jmri.profile.Profile;
import jmri.swing.JmriJTablePersistenceManager.TableColumnPreferences;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the {@link jmri.swing.JmriJTablePersistenceManager}. Some tests use a
 * {@link jmri.swing.JmriJTablePersistenceManagerTest.JmriJTablePersistenceManagerSpy}
 * class that exposes some protected properties for testing purposes.
 *
 * @author Randall Wood (C) 2016
 */
public class JmriJTablePersistenceManagerTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of persist method, of class JmriJTablePersistenceManager. This tests
     * persistence by verifying that tables get the expected listeners attached.
     */
    @Test
    public void testPersist() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        // null table
        JTable table = null;
        try {
            instance.persist(table);
            Assert.fail("did not throw NPE on null table");
        } catch (NullPointerException ex) {
            // passes
        }
        // null table name
        table = testTable(null);
        try {
            instance.persist(table);
            Assert.fail("did not throw NPE on table with null name");
        } catch (NullPointerException ex) {
            // passes
        }
        // correct table
        table.setName("test name");
        try {
            instance.persist(table);
            // passes
        } catch (NullPointerException ex) {
            Assert.fail("threw unexpected NPE");
        }
        int managers = 0;
        int tableListeners = 0;
        int columnListeners = 0;
        for (PropertyChangeListener listener : table.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table))) {
                tableListeners++;
            }
        }
        for (PropertyChangeListener listener : table.getColumn("c0").getPropertyChangeListeners()) {
            if (listener.equals(instance.getListener(table))) {
                columnListeners++;
            }
        }
        Assert.assertEquals(1, managers);
        Assert.assertEquals(1, tableListeners);
        Assert.assertEquals(1, columnListeners);
        // allow table twice
        try {
            instance.persist(table);
            // passes
        } catch (IllegalArgumentException ex) {
            Assert.fail("threw unexpected IllegalArgumentException");
        }
        managers = 0;
        tableListeners = 0;
        columnListeners = 0;
        for (PropertyChangeListener listener : table.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table))) {
                tableListeners++;
            }
        }
        for (PropertyChangeListener listener : table.getColumn("c0").getPropertyChangeListeners()) {
            if (listener.equals(instance.getListener(table))) {
                columnListeners++;
            }
        }
        Assert.assertEquals(1, managers);
        Assert.assertEquals(1, tableListeners);
        Assert.assertEquals(1, columnListeners);
        // duplicate table name
        JTable table2 = testTable("test name");
        table2.setRowSorter(new TableRowSorter<>(table2.getModel()));
        try {
            instance.persist(table2);
            Assert.fail("Accepted duplicate name");
        } catch (IllegalArgumentException ex) {
            // passes
        }
        // a second table
        table2.setName("test name 2");
        try {
            instance.persist(table2);
            // passes
        } catch (IllegalArgumentException ex) {
            Assert.fail("threw unexpected IllegalArgumentException");
        }
        managers = 0;
        tableListeners = 0;
        columnListeners = 0;
        for (PropertyChangeListener listener : table2.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table2))) {
                tableListeners++;
            }
        }
        for (PropertyChangeListener listener : table2.getColumn("c0").getPropertyChangeListeners()) {
            if (listener.equals(instance.getListener(table2))) {
                columnListeners++;
            }
        }
        Assert.assertEquals(1, managers);
        Assert.assertEquals(1, tableListeners);
        Assert.assertEquals(1, columnListeners);
    }

    /**
     * Test of stopPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testStopPersisting() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        JTable table = testTable("test name");
        try {
            instance.persist(table);
            // passes
        } catch (NullPointerException ex) {
            Assert.fail("threw unexpected NPE");
        }
        int managers = 0;
        int tableListeners = 0;
        int columnListeners = 0;
        for (PropertyChangeListener listener : table.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table))) {
                tableListeners++;
            }
        }
        for (PropertyChangeListener listener : table.getColumn("c0").getPropertyChangeListeners()) {
            if (listener.equals(instance.getListener(table))) {
                columnListeners++;
            }
        }
        Assert.assertEquals(1, managers);
        Assert.assertEquals(1, tableListeners);
        Assert.assertEquals(1, columnListeners);
        try {
            instance.stopPersisting(table);
            // passes
        } catch (NullPointerException ex) {
            Assert.fail("threw unexpected NPE");
        }
        for (PropertyChangeListener listener : table.getColumn("c0").getPropertyChangeListeners()) {
            if (listener.equals(instance.getListener(table))) {
                columnListeners++;
            }
        }
        managers = 0;
        tableListeners = 0;
        columnListeners = 0;
        for (PropertyChangeListener listener : table.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table))) {
                tableListeners++;
            }
        }
        for (PropertyChangeListener listener : table.getColumn("c0").getPropertyChangeListeners()) {
            if (listener.equals(instance.getListener(table))) {
                columnListeners++;
            }
        }
        Assert.assertEquals(0, managers);
        Assert.assertEquals(0, tableListeners);
        Assert.assertEquals(0, columnListeners);
    }

    /**
     * Test of clearState method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testClearState() {
        String name = "test name";
        JTable table = testTable(name);
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        Assert.assertFalse(instance.isDirty());
        instance.cacheState(table);
        Assert.assertTrue(instance.isDirty());
        Assert.assertNotNull(instance.getColumnsMap(name));
        instance.setDirty(false);
        instance.clearState(table);
        Assert.assertNull(instance.getColumnsMap(name));
        Assert.assertTrue(instance.isDirty());
    }

    /**
     * Test of cacheState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testCacheState() {
        System.out.println("cacheState");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.cacheState(table);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of resetState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testResetState() {
        System.out.println("resetState");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.resetState(table);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of setPaused method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testSetPaused() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse(instance.isPaused());
        instance.setPaused(true);
        Assert.assertTrue(instance.isPaused());
        instance.setPaused(false);
        Assert.assertFalse(instance.isPaused());
    }

    /**
     * Test of isPaused method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPaused() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse(instance.isPaused());
        instance.setPaused(true);
        Assert.assertTrue(instance.isPaused());
        instance.setPaused(false);
        Assert.assertFalse(instance.isPaused());
    }

    /**
     * Test of initialize method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testInitialize() throws Exception {
        System.out.println("initialize");
        Profile profile = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.initialize(profile);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of savePreferences method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testSavePreferences() {
        System.out.println("savePreferences");
        Profile profile = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.savePreferences(profile);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of getProvides method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testGetProvides() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Set<Class<?>> expected = new HashSet<>();
        expected.add(JTablePersistenceManager.class);
        expected.add(JmriJTablePersistenceManager.class);
        Assert.assertEquals(expected, instance.getProvides());
    }

    /**
     * Test of setPersistedState method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testSetPersistedState() {
        JTable table = testTable("test");
        String column = "c1";
        int order = 0;
        int width = 0;
        SortOrder sort = null;
        boolean hidden = false;
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        Map<String, TableColumnPreferences> map = instance.getColumnsMap(table.getName());
        Assert.assertNull("No columns persisted", map);
        Assert.assertFalse(instance.isPersisting(table));
        instance.setPersistedState(table.getName(), column, order, width, sort, hidden);
        Assert.assertTrue(instance.isPersisting(table));
        map = instance.getColumnsMap(table.getName());
        Assert.assertNotNull("Columns persisted", map);
        Assert.assertEquals("Persisting 1 column", 1, map.size());
        TableColumnPreferences prefs = map.get("c1");
        Assert.assertNotNull("Persisting column c1", prefs);
        Assert.assertFalse("Column c1 is visible", prefs.getHidden());
        Assert.assertNull("Column c1 is not sorted", prefs.getSort());
        Assert.assertEquals("Column c1 is first", order, prefs.getOrder());
        Assert.assertEquals("Column c1 is 0 width", width, prefs.getWidth());
        order = 1;
        width = 1;
        instance.setPersistedState(table.getName(), column, order, width, sort, hidden);
        prefs = map.get("c1");
        Assert.assertNotNull("Persisting column c1", prefs);
        Assert.assertFalse("Column c1 is visible", prefs.getHidden());
        Assert.assertNull("Column c1 is not sorted", prefs.getSort());
        Assert.assertEquals("Column c1 is first", order, prefs.getOrder());
        Assert.assertEquals("Column c1 is 0 width", width, prefs.getWidth());
    }

    /**
     * Test of propertyChange method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testPropertyChange() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        String name1 = "test name";
        String name2 = "name test";
        JTable table = testTable(name1);
        try {
            instance.persist(table);
            // passes
        } catch (NullPointerException ex) {
            Assert.fail("threw unexpected NPE");
        }
        Assert.assertNotNull(instance.getListener(name1));
        Assert.assertNull(instance.getListener(name2));
        table.setName(name2);
        Assert.assertNull(instance.getListener(name1));
        Assert.assertNotNull(instance.getListener(name2));
    }

    /**
     * Test of setDirty method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testSetDirty() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        Assert.assertFalse("new manager w/o tables is clean", instance.isDirty());
        instance.setDirty(true);
        Assert.assertTrue("dirty flag set", instance.isDirty());
        instance.setDirty(false);
        Assert.assertFalse("dirty flag reset", instance.isDirty());
    }

    /**
     * Test of isDirty method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsDirty() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        JTable test = testTable("test");
        Assert.assertFalse("new manager w/o tables is clean", instance.isDirty());
        instance.persist(test);
        Assert.assertTrue("table added, not saved", instance.isDirty());
        instance.setDirty(false);
        Assert.assertFalse("set to clean for test", instance.isDirty());
        instance.setPersistedState(test.getName(), "c1", 0, 0, SortOrder.ASCENDING, false);
        Assert.assertTrue("column changed", instance.isDirty());
    }

    /**
     * Test of getDirty method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testGetDirty() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        JTable test = testTable("test");
        Assert.assertFalse("new manager w/o tables is clean", instance.getDirty());
        instance.persist(test);
        Assert.assertTrue("table added, not saved", instance.getDirty());
        instance.setDirty(false);
        Assert.assertFalse("set to clean for test", instance.getDirty());
        instance.setPersistedState(test.getName(), "c1", 0, 0, SortOrder.ASCENDING, false);
        Assert.assertTrue("column changed", instance.getDirty());
    }

    /**
     * Create a simple table with some columns and rows. Use only defaults for
     * all other values.
     *
     * @param name the name of the table; can be null
     * @return a new table
     */
    private JTable testTable(String name) {
        JTable table = new JTable(2, 2);
        table.getColumnModel().getColumn(0).setHeaderValue("c0");
        table.getColumnModel().getColumn(1).setHeaderValue("c1");
        table.setName(name);
        return table;
    }

    private final static class JmriJTablePersistenceManagerSpy extends JmriJTablePersistenceManager {

        @Override
        public boolean isDirty() {
            return super.isDirty();
        }

        @Override
        public void setDirty(boolean state) {
            super.setDirty(state);
        }

        public JTableListener getListener(JTable table) {
            return this.listeners.get(table.getName());
        }

        public JTableListener getListener(String name) {
            return this.listeners.get(name);
        }

        public Map<String, TableColumnPreferences> getColumnsMap(String table) {
            return this.columns.get(table);
        }
    }
}
