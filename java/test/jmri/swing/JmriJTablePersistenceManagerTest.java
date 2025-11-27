package jmri.swing;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import jmri.profile.*;
import jmri.swing.JmriJTablePersistenceManager.TableColumnPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;

/**
 * Tests the {@link jmri.swing.JmriJTablePersistenceManager}. Some tests use a
 * {@link jmri.swing.JmriJTablePersistenceManagerTest.JmriJTablePersistenceManagerSpy}
 * class that exposes some protected properties for testing purposes.
 *
 * @author Randall Wood (C) 2016
 */
@SuppressWarnings("javadoc")
public class JmriJTablePersistenceManagerTest {

    /**
     * Test of persist method, of class JmriJTablePersistenceManager. This tests
     * persistence by verifying that tables get the expected listeners attached.
     */
    @Test
    public void testPersist() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();

        Exception exc = assertThrows(NullPointerException.class,() -> {
            persistNullTable(instance);
        });
        assertNotNull(exc);

        // null table name
        JTable table = testTable(null);
        exc = assertThrows(NullPointerException.class,() -> {
            instance.persist(table);
        });
        assertNotNull(exc, "did not throw NPE on table with null name");

        // correct table
        table.setName("test name");
        assertDoesNotThrow(() -> {
            instance.persist(table);
        });

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
        assertEquals(1, managers);
        assertEquals(1, tableListeners);
        assertEquals(1, columnListeners);
        // allow table twice
        Assertions.assertDoesNotThrow(() -> {
            instance.persist(table);
        });

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
        assertEquals(1, managers);
        assertEquals(1, tableListeners);
        assertEquals(1, columnListeners);
        // duplicate table name
        JTable table2 = testTable("test name");
        table2.setRowSorter(new TableRowSorter<>(table2.getModel()));
        
        exc = assertThrows(IllegalArgumentException.class,() -> {
            instance.persist(table2);
        },"Accepted duplicate name");
        assertNotNull(exc, "did not throw NPE on table with null name");

        // a second table
        table2.setName("test name 2");
        assertDoesNotThrow(() -> {
            instance.persist(table2);
        });
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
        assertEquals(1, managers);
        assertEquals(1, tableListeners);
        assertEquals(1, columnListeners);
    }

    @SuppressWarnings("null")
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = {"NP_NONNULL_PARAM_VIOLATION","NP_LOAD_OF_KNOWN_NULL_VALUE"},
        justification = "testing exception when null passed")
    private void persistNullTable(JmriJTablePersistenceManagerSpy instance) throws Exception {
        JTable nullTable = null;
        instance.persist(nullTable);
    }
    
    /**
     * Test of stopPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testStopPersisting() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        JTable table = testTable("test name");
        assertDoesNotThrow(() -> {
            instance.persist(table);
        });
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
        assertEquals(1, managers);
        assertEquals(1, tableListeners);
        assertEquals(1, columnListeners);
        Assertions.assertDoesNotThrow(() -> {
            instance.stopPersisting(table);
        });

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
        assertEquals(0, managers);
        assertEquals(0, tableListeners);
        assertEquals(0, columnListeners);
    }

    /**
     * Test of clearState method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testClearState() {
        String name = "test name";
        JTable table = testTable(name);
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        assertFalse(instance.isDirty());
        instance.cacheState(table);
        assertTrue(instance.isDirty());
        assertNotNull(instance.getColumnsMap(name));
        instance.setDirty(false);
        instance.clearState(table);
        assertNull(instance.getColumnsMap(name));
        assertTrue(instance.isDirty());
    }

    /**
     * Test of cacheState method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testCacheState() {
        JTable table = testTable("test");
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        assertFalse( instance.isPersisting(table), "Not persisting table");
        assertFalse( instance.isDirty(), "Clean manager");
        instance.cacheState(table);
        assertFalse( instance.isPersisting(table), "Persisting table");
        assertTrue( instance.isDirty(), "Dirty manager");
        assertEquals( table.getColumnModel().getColumn(1).getWidth(),
            instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth(),
            "Column c1 is default width");
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        var columnc1 = instance.getColumnsMap(table.getName()).get("c1");
        Assertions.assertNotNull(columnc1);
        assertNotEquals( table.getColumnModel().getColumn(1).getPreferredWidth(),
            columnc1.getPreferredWidth(), "Column c1 width not persisted width");
        instance.cacheState(table);
        assertEquals( table.getColumnModel().getColumn(1).getPreferredWidth(),
            instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth(),
            "Column c1 is 100 width");
    }

    /**
     * Test of resetState method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testResetState() {
        JTable table = testTable("test");
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        assertFalse( instance.isPersisting(table), "Not persisting table");
        assertFalse( instance.isDirty(), "Clean manager");
        TableColumn c0 = table.getColumnModel().getColumn(0);
        TableColumn c1 = table.getColumnModel().getColumn(1);
        c0.setPreferredWidth(75);
        c1.setPreferredWidth(75);
        // set widths to other than table's widths for test
        instance.setPersistedState(table.getName(), c0.getHeaderValue().toString(), 0, 50, SortOrder.UNSORTED, false);
        instance.setPersistedState(table.getName(), c1.getHeaderValue().toString(), 0, 100, SortOrder.UNSORTED, false);
        assertFalse( instance.isPersisting(table), "Persisting table");
        instance.setDirty(false);
        assertFalse( instance.isDirty(), "Clean manager");
        assertEquals( 50, instance.getColumnsMap(table.getName()).get("c0").getPreferredWidth(),
            "State for column c0 is narrow");
        assertEquals( 100, instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth(),
            "State for column c1 is wide");

        var columnc0 = instance.getColumnsMap(table.getName()).get("c0");
        var columnc1 = instance.getColumnsMap(table.getName()).get("c1");
        assertNotNull(columnc0);
        assertNotNull(columnc1);

        assertNotEquals( table.getColumnModel().getColumn(0).getPreferredWidth(),
            columnc0.getPreferredWidth(), "Column c0 width not persisted width");
        assertNotEquals( table.getColumnModel().getColumn(1).getPreferredWidth(),
            columnc1.getPreferredWidth(), "Column c1 width not persisted width");
        instance.resetState(table);
        assertEquals( 50, c0.getPreferredWidth(), "Column c0 is 50 width");
        assertEquals( 100, c1.getPreferredWidth(), "Column c1 is 100 width");
    }

    /**
     * Test of setPaused method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testSetPaused() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        assertFalse(instance.isPaused());
        instance.setPaused(true);
        assertTrue(instance.isPaused());
        instance.setPaused(false);
        assertFalse(instance.isPaused());
    }

    /**
     * Test of isPaused method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPaused() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        assertFalse(instance.isPaused());
        instance.setPaused(true);
        assertTrue(instance.isPaused());
        instance.setPaused(false);
        assertFalse(instance.isPaused());
    }

    /**
     * Test of initialize method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testInitialize_EmptyProfile() {
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        assertNotNull( profile, "Profile is not null");
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        assertDoesNotThrow( () -> instance.initialize(profile), "Unable to initialize");
        assertEquals( 0, instance.columns.size(), "No tables persisted");
        assertEquals( 0, instance.listeners.size(), "No tables listened to");
        assertEquals( 0, instance.sortKeys.size(), "No tables sorted");
    }

    /**
     * Test of initialize method, of class JmriJTablePersistenceManager.
     *
     * @throws java.net.URISyntaxException if test resource URL cannot be
     *                                     converted to URI
     * @throws java.io.IOException         if unable to access test resource as
     *                                     file
     */
    @Test
    public void testInitialize_ExistingProfile() throws URISyntaxException, IOException {
        String name1 = "Test1";
        String name2 = "Test2";
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        assertNotNull(profile); // test requires non-null profile
        // copy preferences into profile
        var classLoaderUIxml = ClassLoader.getSystemResource("jmri/swing/JmriJTablePersistenceManagerTest-user-interface.xml");
        assertNotNull(classLoaderUIxml);
        File source = new File(classLoaderUIxml.toURI());
        File target = new File(new File(new File(profile.getPath(), Profile.PROFILE), NodeIdentity.storageIdentity()), Profile.UI_CONFIG);
        assertNotNull(target);
        var parentFile = target.getParentFile();
        assertNotNull(parentFile);
        FileUtil.createDirectory(parentFile);
        FileUtil.copy(source, target);
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        assertDoesNotThrow( () -> instance.initialize(profile), "Unable to initialize");
        // verify collections
        assertEquals( 2, instance.columns.size(), "Two tables persisted");
        assertEquals( 0, instance.listeners.size(), "No tables listened to");
        assertEquals( 2, instance.sortKeys.size(), "Two tables sorted");
        // verify table Test1
        assertTrue( instance.isPersistenceDataRetained(name1), "Has data for Test1");
        assertEquals( 2, instance.getColumnsMap(name1).size(), "Test1 has two columns");
        assertNotNull( instance.sortKeys.get(name1), "Test1 is sorted");
        assertEquals( 0, instance.getColumnsMap(name1).get("c0").getOrder(), "Test1/c0 is the first column");
        assertEquals( 100, instance.getColumnsMap(name1).get("c0").getPreferredWidth(), "Test1/c0 is 100 px wide");
        assertFalse( instance.getColumnsMap(name1).get("c0").getHidden(), "Test1/c0 is visible");
        assertEquals( SortOrder.UNSORTED, instance.getColumnsMap(name1).get("c0").getSort(), "Test1/c0 is unsorted");
        assertEquals( 1, instance.getColumnsMap(name1).get("c1").getOrder(), "Test1/c1 is the second column");
        assertEquals( 50, instance.getColumnsMap(name1).get("c1").getPreferredWidth(), "Test1/c1 is 50 px wide");
        assertFalse( instance.getColumnsMap(name1).get("c1").getHidden(), "Test1/c1 is visible");
        assertEquals( SortOrder.ASCENDING, instance.getColumnsMap(name1).get("c1").getSort(), "Test1/c1 is sorted ascending");
        // verify table Test2
        assertTrue( instance.isPersistenceDataRetained(name2), "Has data for Test2");
        assertEquals( 2, instance.getColumnsMap(name2).size(), "Test2 has two columns");
        assertNotNull( instance.sortKeys.get(name2), "Test2 is sorted");
        assertEquals( 1, instance.getColumnsMap(name2).get("c0").getOrder(), "Test2/c0 is the second column");
        assertEquals( 75, instance.getColumnsMap(name2).get("c0").getPreferredWidth(), "Test2/c0 is 75 px wide");
        assertFalse( instance.getColumnsMap(name2).get("c0").getHidden(), "Test2/c0 is visible");
        assertEquals( SortOrder.UNSORTED, instance.getColumnsMap(name2).get("c0").getSort(), "Test2/c0 is unsorted");
        assertEquals( 0, instance.getColumnsMap(name2).get("c1").getOrder(), "Test2/c1 is the first column");
        assertEquals( 50, instance.getColumnsMap(name2).get("c1").getPreferredWidth(), "Test2/c1 is 50 px wide");
        assertTrue( instance.getColumnsMap(name2).get("c1").getHidden(), "Test2/c1 is hidden");
        assertEquals( SortOrder.DESCENDING, instance.getColumnsMap(name2).get("c1").getSort(), "Test2/c1 is sorted descending");
    }

    /**
     * Test of savePreferences method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Disabled("test code is incomplete prototype")
    public void testSavePreferences() {
        // System.out.println("savePreferences");
        // Profile profile = null;
        // JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        // instance.savePreferences(profile);
        // TODO review the generated test code and remove the default call to fail.
        // Assert.fail("The test case is a prototype.");
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
        assertEquals(expected, instance.getProvides());
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
        assertNull( map, "No columns persisted");
        assertFalse(instance.isPersistenceDataRetained(table));
        instance.setPersistedState(table.getName(), column, order, width, sort, hidden);
        assertTrue(instance.isPersistenceDataRetained(table));
        map = instance.getColumnsMap(table.getName());
        assertNotNull( map, "Columns persisted");
        assertEquals( 1, map.size(), "Persisting 1 column");
        TableColumnPreferences prefs = map.get("c1");
        assertNotNull( prefs, "Persisting column c1");
        assertFalse( prefs.getHidden(), "Column c1 is visible");
        assertNull( prefs.getSort(), "Column c1 is not sorted");
        assertEquals( order, prefs.getOrder(), "Column c1 is first");
        assertEquals( width, prefs.getPreferredWidth(), "Column c1 is 0 width");
        order = 1;
        width = 1;
        instance.setPersistedState(table.getName(), column, order, width, sort, hidden);
        prefs = map.get("c1");
        assertNotNull( prefs, "Persisting column c1");
        assertFalse( prefs.getHidden(), "Column c1 is visible");
        assertNull( prefs.getSort(), "Column c1 is not sorted");
        assertEquals( order, prefs.getOrder(), "Column c1 is first");
        assertEquals( width, prefs.getPreferredWidth(), "Column c1 is 0 width");
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
        assertDoesNotThrow(() -> {
            instance.persist(table);
        });

        assertNotNull(instance.getListener(name1));
        assertNull(instance.getListener(name2));
        table.setName(name2);
        assertNull(instance.getListener(name1));
        assertNotNull(instance.getListener(name2));
    }

    /**
     * Test of setDirty method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testSetDirty() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        assertFalse( instance.isDirty(), "new manager w/o tables is clean");
        instance.setDirty(true);
        assertTrue( instance.isDirty(), "dirty flag set");
        instance.setDirty(false);
        assertFalse( instance.isDirty(), "dirty flag reset");
    }

    /**
     * Test of isDirty method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsDirty() {
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        JTable test = testTable("test");
        assertFalse( instance.isDirty(), "new manager w/o tables is clean");
        instance.persist(test);
        assertTrue( instance.isDirty(), "table added, not saved");
        instance.setDirty(false);
        assertFalse( instance.isDirty(), "set to clean for test");
        instance.setPersistedState(test.getName(), "c1", 0, 0, SortOrder.ASCENDING, false);
        assertTrue( instance.isDirty(), "column changed");
    }

    /**
     * Test of isPersistenceDataRetained method, of class
     * JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersistenceDataRetained_JTable() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        assertFalse( instance.isPersistenceDataRetained(table), "Not persisting, not retaining table");
        instance.persist(table);
        assertTrue( instance.isPersistenceDataRetained(table), "Persisting");
        instance.stopPersisting(table);
        assertTrue( instance.isPersistenceDataRetained(table), "Not Persisting, retaining table");
    }

    /**
     * Test of isPersistenceDataRetained method, of class
     * JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersistenceDataRetained_String() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        assertFalse( instance.isPersistenceDataRetained(table.getName()), "Not persisting, not retaining table");
        instance.persist(table);
        assertTrue( instance.isPersistenceDataRetained(table.getName()), "Persisting");
        instance.stopPersisting(table);
        assertTrue( instance.isPersistenceDataRetained(table.getName()), "Not Persisting, retaining table");
    }

    /**
     * Test of isPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersisting_JTable() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        assertFalse( instance.isPersisting(table), "Not persisting");
        instance.persist(table);
        assertTrue( instance.isPersisting(table), "Persist");
        instance.stopPersisting(table);
        assertFalse( instance.isPersisting(table), "Not persisting");
        instance.persist(table);
        assertTrue( instance.isPersisting(table), "Persist");
    }

    /**
     * Test of isPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersisting_String() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        assertFalse( instance.isPersisting(table.getName()), "Not persisting");
        instance.persist(table);
        assertTrue( instance.isPersisting(table.getName()), "Persist");
        instance.stopPersisting(table);
        assertFalse( instance.isPersisting(table.getName()), "Not persisting");
        instance.persist(table);
        assertTrue( instance.isPersisting(table.getName()), "Persist");
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

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile(folder));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
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

        public JmriJTablePersistenceManager.JTableListener getListener(JTable table) {
            return this.listeners.get(table.getName());
        }

        public JmriJTablePersistenceManager.JTableListener getListener(String name) {
            return this.listeners.get(name);
        }

        public Map<String, TableColumnPreferences> getColumnsMap(String table) {
            return this.columns.get(table);
        }

        @Override
        public void setPersistedState(String table, String column, int order, int width, SortOrder sort, boolean hidden) {
            super.setPersistedState(table, column, order, width, sort, hidden);
        }
    }
}
