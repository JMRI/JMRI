package jmri.swing;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.swing.JmriJTablePersistenceManager.TableColumnPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import jmri.util.prefs.InitializationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests the {@link jmri.swing.JmriJTablePersistenceManager}. Some tests use a
 * {@link jmri.swing.JmriJTablePersistenceManagerTest.JmriJTablePersistenceManagerSpy}
 * class that exposes some protected properties for testing purposes.
 *
 * @author Randall Wood (C) 2016
 */
public class JmriJTablePersistenceManagerTest {

    @Rule
    public TemporaryFolder profileFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile(profileFolder.newFolder(Profile.PROFILE)));
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
    public void testCacheState() {
        JTable table = testTable("test");
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        Assert.assertFalse("Not persisting table", instance.isPersisting(table));
        Assert.assertFalse("Clean manager", instance.isDirty());
        instance.cacheState(table);
        Assert.assertFalse("Persisting table", instance.isPersisting(table));
        Assert.assertTrue("Dirty manager", instance.isDirty());
        Assert.assertEquals("Column c1 is default width", table.getColumnModel().getColumn(1).getWidth(), instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        Assert.assertNotEquals("Column c1 width not persisted width",
                table.getColumnModel().getColumn(1).getPreferredWidth(),
                instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
        instance.cacheState(table);
        Assert.assertEquals("Column c1 is 100 width", table.getColumnModel().getColumn(1).getPreferredWidth(), instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
    }

    /**
     * Test of resetState method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testResetState() {
        JTable table = testTable("test");
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        Assert.assertFalse("Not persisting table", instance.isPersisting(table));
        Assert.assertFalse("Clean manager", instance.isDirty());
        TableColumn c0 = table.getColumnModel().getColumn(0);
        TableColumn c1 = table.getColumnModel().getColumn(1);
        c0.setPreferredWidth(75);
        c1.setPreferredWidth(75);
        // set widths to other than table's widths for test
        instance.setPersistedState(table.getName(), c0.getHeaderValue().toString(), 0, 50, SortOrder.UNSORTED, false);
        instance.setPersistedState(table.getName(), c1.getHeaderValue().toString(), 0, 100, SortOrder.UNSORTED, false);
        Assert.assertFalse("Persisting table", instance.isPersisting(table));
        instance.setDirty(false);
        Assert.assertFalse("Clean manager", instance.isDirty());
        Assert.assertEquals("State for column c0 is narrow", 50, instance.getColumnsMap(table.getName()).get("c0").getPreferredWidth());
        Assert.assertEquals("State for column c1 is wide", 100, instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
        Assert.assertNotEquals("Column c0 width not persisted width",
                table.getColumnModel().getColumn(0).getPreferredWidth(),
                instance.getColumnsMap(table.getName()).get("c0").getPreferredWidth());
        Assert.assertNotEquals("Column c1 width not persisted width",
                table.getColumnModel().getColumn(1).getPreferredWidth(),
                instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
        instance.resetState(table);
        Assert.assertEquals("Column c0 is 50 width", 50, c0.getPreferredWidth());
        Assert.assertEquals("Column c1 is 100 width", 100, c1.getPreferredWidth());
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
    public void testInitialize_EmptyProfile() {
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        Assume.assumeNotNull("Profile is not null", profile);
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        try {
            instance.initialize(profile);
        } catch (InitializationException ex) {
            Assert.fail("Unable to initialize due to " + ex.getMessage());
        }
        Assert.assertEquals("No tables persisted", 0, instance.columns.size());
        Assert.assertEquals("No tables listened to", 0, instance.listeners.size());
        Assert.assertEquals("No tables sorted", 0, instance.sortKeys.size());
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
        Assert.assertNotNull(profile); // test requires non-null profile
        // copy preferences into profile
        File source = new File(ClassLoader.getSystemResource("jmri/swing/JmriJTablePersistenceManagerTest-user-interface.xml").toURI());
        File target = new File(new File(new File(profile.getPath(), Profile.PROFILE), NodeIdentity.storageIdentity()), Profile.UI_CONFIG);
        FileUtil.createDirectory(target.getParentFile());
        FileUtil.copy(source, target);
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        try {
            instance.initialize(profile);
        } catch (InitializationException ex) {
            Assert.fail("Unable to initialize due to " + ex.getMessage());
        }
        // verify collections
        Assert.assertEquals("Two tables persisted", 2, instance.columns.size());
        Assert.assertEquals("No tables listened to", 0, instance.listeners.size());
        Assert.assertEquals("Two tables sorted", 2, instance.sortKeys.size());
        // verify table Test1
        Assert.assertTrue("Has data for Test1", instance.isPersistenceDataRetained(name1));
        Assert.assertEquals("Test1 has two columns", 2, instance.getColumnsMap(name1).size());
        Assert.assertNotNull("Test1 is sorted", instance.sortKeys.get(name1));
        Assert.assertEquals("Test1/c0 is the first column", 0, instance.getColumnsMap(name1).get("c0").getOrder());
        Assert.assertEquals("Test1/c0 is 100 px wide", 100, instance.getColumnsMap(name1).get("c0").getPreferredWidth());
        Assert.assertFalse("Test1/c0 is visible", instance.getColumnsMap(name1).get("c0").getHidden());
        Assert.assertEquals("Test1/c0 is unsorted", SortOrder.UNSORTED, instance.getColumnsMap(name1).get("c0").getSort());
        Assert.assertEquals("Test1/c1 is the second column", 1, instance.getColumnsMap(name1).get("c1").getOrder());
        Assert.assertEquals("Test1/c1 is 50 px wide", 50, instance.getColumnsMap(name1).get("c1").getPreferredWidth());
        Assert.assertFalse("Test1/c1 is visible", instance.getColumnsMap(name1).get("c1").getHidden());
        Assert.assertEquals("Test1/c1 is sorted ascending", SortOrder.ASCENDING, instance.getColumnsMap(name1).get("c1").getSort());
        // verify table Test2
        Assert.assertTrue("Has data for Test2", instance.isPersistenceDataRetained(name2));
        Assert.assertEquals("Test2 has two columns", 2, instance.getColumnsMap(name2).size());
        Assert.assertNotNull("Test2 is sorted", instance.sortKeys.get(name2));
        Assert.assertEquals("Test2/c0 is the second column", 1, instance.getColumnsMap(name2).get("c0").getOrder());
        Assert.assertEquals("Test2/c0 is 75 px wide", 75, instance.getColumnsMap(name2).get("c0").getPreferredWidth());
        Assert.assertFalse("Test2/c0 is visible", instance.getColumnsMap(name2).get("c0").getHidden());
        Assert.assertEquals("Test2/c0 is unsorted", SortOrder.UNSORTED, instance.getColumnsMap(name2).get("c0").getSort());
        Assert.assertEquals("Test2/c1 is the first column", 0, instance.getColumnsMap(name2).get("c1").getOrder());
        Assert.assertEquals("Test2/c1 is 50 px wide", 50, instance.getColumnsMap(name2).get("c1").getPreferredWidth());
        Assert.assertTrue("Test2/c1 is hidden", instance.getColumnsMap(name2).get("c1").getHidden());
        Assert.assertEquals("Test2/c1 is sorted descending", SortOrder.DESCENDING, instance.getColumnsMap(name2).get("c1").getSort());
    }

    /**
     * Test of savePreferences method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore("test code is incomplete prototype")
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
        Assert.assertFalse(instance.isPersistenceDataRetained(table));
        instance.setPersistedState(table.getName(), column, order, width, sort, hidden);
        Assert.assertTrue(instance.isPersistenceDataRetained(table));
        map = instance.getColumnsMap(table.getName());
        Assert.assertNotNull("Columns persisted", map);
        Assert.assertEquals("Persisting 1 column", 1, map.size());
        TableColumnPreferences prefs = map.get("c1");
        Assert.assertNotNull("Persisting column c1", prefs);
        Assert.assertFalse("Column c1 is visible", prefs.getHidden());
        Assert.assertNull("Column c1 is not sorted", prefs.getSort());
        Assert.assertEquals("Column c1 is first", order, prefs.getOrder());
        Assert.assertEquals("Column c1 is 0 width", width, prefs.getPreferredWidth());
        order = 1;
        width = 1;
        instance.setPersistedState(table.getName(), column, order, width, sort, hidden);
        prefs = map.get("c1");
        Assert.assertNotNull("Persisting column c1", prefs);
        Assert.assertFalse("Column c1 is visible", prefs.getHidden());
        Assert.assertNull("Column c1 is not sorted", prefs.getSort());
        Assert.assertEquals("Column c1 is first", order, prefs.getOrder());
        Assert.assertEquals("Column c1 is 0 width", width, prefs.getPreferredWidth());
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
    @SuppressWarnings("deprecation")
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
     * Test of setTableColumnPreferences method, of class
     * JmriJTablePersistenceManager.
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testSetTableColumnPreferences() {
        JTable table = testTable("test");
        JmriJTablePersistenceManagerSpy instance = new JmriJTablePersistenceManagerSpy();
        Assert.assertFalse("Not persisting table", instance.isPersisting(table));
        Assert.assertFalse("Clean manager", instance.isDirty());
        TableColumn c0 = table.getColumnModel().getColumn(0);
        TableColumn c1 = table.getColumnModel().getColumn(1);
        instance.setTableColumnPreferences(table.getName(), c0.getHeaderValue().toString(), 0, c0.getPreferredWidth(), SortOrder.UNSORTED, false);
        instance.setTableColumnPreferences(table.getName(), c1.getHeaderValue().toString(), 0, c1.getPreferredWidth(), SortOrder.UNSORTED, false);
        Assert.assertFalse("Persisting table", instance.isPersisting(table));
        Assert.assertTrue("Dirty manager", instance.isDirty());
        Assert.assertEquals("Column c1 is default width", c1.getWidth(), instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
        c1.setPreferredWidth(100);
        Assert.assertNotEquals("Column c1 width not persisted width",
                c1.getPreferredWidth(),
                instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
        instance.setTableColumnPreferences(table.getName(), c1.getHeaderValue().toString(), 0, c1.getPreferredWidth(), SortOrder.UNSORTED, false);
        Assert.assertEquals("Column c1 is 100 width", c1.getPreferredWidth(), instance.getColumnsMap(table.getName()).get("c1").getPreferredWidth());
    }

    /**
     * Test of isPersistenceDataRetained method, of class
     * JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersistenceDataRetained_JTable() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse("Not persisting, not retaining table", instance.isPersistenceDataRetained(table));
        instance.persist(table);
        Assert.assertTrue("Persisting", instance.isPersistenceDataRetained(table));
        instance.stopPersisting(table);
        Assert.assertTrue("Not Persisting, retaining table", instance.isPersistenceDataRetained(table));
    }

    /**
     * Test of isPersistenceDataRetained method, of class
     * JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersistenceDataRetained_String() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse("Not persisting, not retaining table", instance.isPersistenceDataRetained(table.getName()));
        instance.persist(table);
        Assert.assertTrue("Persisting", instance.isPersistenceDataRetained(table.getName()));
        instance.stopPersisting(table);
        Assert.assertTrue("Not Persisting, retaining table", instance.isPersistenceDataRetained(table.getName()));
    }

    /**
     * Test of isPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersisting_JTable() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse("Not persisting", instance.isPersisting(table));
        instance.persist(table);
        Assert.assertTrue("Persist", instance.isPersisting(table));
        instance.stopPersisting(table);
        Assert.assertFalse("Not persisting", instance.isPersisting(table));
        instance.persist(table);
        Assert.assertTrue("Persist", instance.isPersisting(table));
    }

    /**
     * Test of isPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPersisting_String() {
        JTable table = testTable("test");
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse("Not persisting", instance.isPersisting(table.getName()));
        instance.persist(table);
        Assert.assertTrue("Persist", instance.isPersisting(table.getName()));
        instance.stopPersisting(table);
        Assert.assertFalse("Not persisting", instance.isPersisting(table.getName()));
        instance.persist(table);
        Assert.assertTrue("Persist", instance.isPersisting(table.getName()));
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
