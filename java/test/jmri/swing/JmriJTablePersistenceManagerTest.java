package jmri.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import apps.tests.Log4JFixture;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import jmri.profile.Profile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

    public JmriJTablePersistenceManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Log4JFixture.setUp();
    }

    @AfterClass
    public static void tearDownClass() {
        Log4JFixture.tearDown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
        table = new JTable();
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
        int listeners = 0;
        for (PropertyChangeListener listener : table.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table))) {
                listeners++;
            }
        }
        Assert.assertEquals(1, managers);
        Assert.assertEquals(1, listeners);
        // allow table twice
        try {
            instance.persist(table);
            // passes
        } catch (IllegalArgumentException ex) {
            Assert.fail("threw unexpected IllegalArgumentException");
        }
        managers = 0;
        listeners = 0;
        for (PropertyChangeListener listener : table.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table))) {
                listeners++;
            }
        }
        Assert.assertEquals(1, managers);
        Assert.assertEquals(1, listeners);
        // duplicate table name
        JTable table2 = new JTable();
        table2.setRowSorter(new TableRowSorter<>(table2.getModel()));
        table2.setName("test name");
        try {
            instance.persist(table2);
            Assert.fail("Accepted duplicate name");
        } catch (IllegalArgumentException ex) {
            // passes
        }
        // a second table
        table2.setName("test name 2");
        try {
            instance.persist(table);
            // passes
        } catch (IllegalArgumentException ex) {
            Assert.fail("threw unexpected IllegalArgumentException");
        }
        managers = 0;
        listeners = 0;
        for (PropertyChangeListener listener : table.getPropertyChangeListeners()) {
            if (listener.equals(instance)) {
                managers++;
            }
            if (listener.equals(instance.getListener(table))) {
                listeners++;
            }
        }
        Assert.assertEquals(1, managers);
        Assert.assertEquals(1, listeners);
    }

    /**
     * Test of stopPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testStopPersisting() {
        System.out.println("stopPersisting");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.stopPersisting(table);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testClearState() {
        System.out.println("clearState");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.clearState(table);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
        fail("The test case is a prototype.");
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
        fail("The test case is a prototype.");
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
        fail("The test case is a prototype.");
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
        fail("The test case is a prototype.");
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
     * Test of getPersistedState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testGetPersistedState() {
        System.out.println("getPersistedState");
        String table = "";
        String column = "";
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        JmriJTablePersistenceManager.TableColumnPreferences expResult = null;
        JmriJTablePersistenceManager.TableColumnPreferences result = instance.getPersistedState(table, column);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPersistedState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testSetPersistedState() {
        System.out.println("setPersistedState");
        String table = "";
        String column = "";
        int order = 0;
        int width = 0;
        SortOrder sort = null;
        boolean hidden = false;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.setPersistedState(table, column, order, width, sort, hidden);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of propertyChange method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testPropertyChange() {
        System.out.println("propertyChange");
        PropertyChangeEvent evt = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.propertyChange(evt);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    private final static class JmriJTablePersistenceManagerSpy extends JmriJTablePersistenceManager {

        //default access
        JTableListener getListener(JTable table) {
            return this.listeners.get(table.getName());
        }

    }
}
