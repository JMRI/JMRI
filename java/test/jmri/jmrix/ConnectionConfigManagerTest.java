package jmri.jmrix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jmri.jmrix.internal.InternalConnectionTypeList;
import jmri.profile.Profile;
import jmri.spi.PreferencesManager;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for ConnectionConfigManager.
 *
 * @author Randall Wood
 */
public class ConnectionConfigManagerTest {

    private Path workspace;
    public final static String MFG1 = "Mfg1";
    public final static String MFG2 = "Mfg2";
    public final static String MFG3 = "Mfg3";
    public final static String TYPE_A = "TypeA";
    public final static String TYPE_B = "TypeB";
    public final static String TYPE_C = "TypeC";
    public final static String TYPE_D = "TypeD";
    // private final static Logger log = LoggerFactory.getLogger(ConnectionConfigManagerTest.class);

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        this.workspace = Files.createTempDirectory(this.getClass().getSimpleName());
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetFileUtilSupport();
        FileUtil.delete(this.workspace.toFile());
    }

    @Test
    public void testGetConnectionManufacturers() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        String[] result = instance.getConnectionManufacturers();
        Assert.assertTrue(result.length > 1);
        Assert.assertEquals(InternalConnectionTypeList.NONE, result[0]);
        List<String> types = Arrays.asList(result);
        Assert.assertTrue(types.contains(MFG1));
        Assert.assertTrue(types.contains(MFG2));
        JUnitUtil.resetInstanceManager();
    }

    @Test
    public void testGetConnectionManufacturers_String() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        String[] result = instance.getConnectionManufacturers(TYPE_A);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals(MFG1, result[0]);
        result = instance.getConnectionManufacturers(TYPE_D);
        Assert.assertEquals(2, result.length);
        List<String> types = Arrays.asList(result);
        Assert.assertTrue(types.contains(MFG2));
        Assert.assertTrue(types.contains(MFG3));
        JUnitUtil.resetInstanceManager();
    }

    @Test
    public void testGetConnectionTypes() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        // Internal
        String[] result = instance.getConnectionTypes(InternalConnectionTypeList.NONE);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("jmri.jmrix.internal.ConnectionConfig", result[0]);
        // MFG3
        result = instance.getConnectionTypes(MFG3);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals(TYPE_D, result[0]);
        // MFG2
        result = instance.getConnectionTypes(MFG2);
        Assert.assertEquals(2, result.length);
        Assert.assertEquals(TYPE_C, result[0]);
        Assert.assertEquals(TYPE_D, result[1]);
        // MFG1
        result = instance.getConnectionTypes(MFG1);
        Assert.assertEquals(2, result.length);
        Assert.assertEquals(TYPE_A, result[0]);
        Assert.assertEquals(TYPE_B, result[1]);
        JUnitUtil.resetInstanceManager();
    }

    /**
     * Test of initialize method, of class ConnectionConfigManager.
     *
     * @throws java.io.IOException if untested condition (most likely inability
     *                             to write to temp space) fails
     */
    @Test
    public void testInitialize_EmptyProfile() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile profile = new Profile(this.getClass().getSimpleName(), id, new File(this.workspace.toFile(), id));
        ConnectionConfigManager instance = new ConnectionConfigManager();
        try {
            instance.initialize(profile);
        } catch (InitializationException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Test of getRequires method, of class ConnectionConfigManager.
     */
    @Test
    public void testGetRequires() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Set<Class<? extends PreferencesManager>> result = instance.getRequires();
        Assert.assertEquals(0, result.size());
    }

    /**
     * Test of add method, of class ConnectionConfigManager.
     */
    @Test
    public void testAdd() {
        ConnectionConfig c = new TestConnectionConfig();
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Assert.assertEquals(0, instance.getConnections().length);
        Assert.assertTrue(instance.add(c));
        Assert.assertEquals(1, instance.getConnections().length);
        Assert.assertFalse(instance.add(c));
        Assert.assertEquals(1, instance.getConnections().length);
        NullPointerException npe = null;
        try {
            // deliberately passing invalid value
            instance.add(null);
        } catch (NullPointerException ex) {
            npe = ex;
        }
        Assert.assertNotNull(npe);
    }

    /**
     * Test of remove method, of class ConnectionConfigManager.
     */
    @Test
    public void testRemove() {
        ConnectionConfig c = new TestConnectionConfig();
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Assert.assertEquals(0, instance.getConnections().length);
        Assert.assertFalse(instance.remove(c));
        instance.add(c);
        Assert.assertEquals(1, instance.getConnections().length);
        Assert.assertTrue(instance.remove(c));
        Assert.assertEquals(0, instance.getConnections().length);
        Assert.assertFalse(instance.remove(c));
        Assert.assertEquals(0, instance.getConnections().length);
    }

    /**
     * Test of getConnections method, of class ConnectionConfigManager.
     */
    @Test
    public void testGetConnections_0args() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Assert.assertEquals(0, instance.getConnections().length);
        instance.add(new TestConnectionConfig());
        Assert.assertEquals(1, instance.getConnections().length);
    }

    /**
     * Test of getConnections method, of class ConnectionConfigManager.
     */
    @Test
    public void testGetConnections_int() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        ConnectionConfig c = new TestConnectionConfig();
        Assert.assertTrue(instance.add(c));
        Assert.assertEquals(c, instance.getConnections(0));
        IndexOutOfBoundsException ioobe = null;
        try {
            // don't care about returned result - expecting exception to be thrown
            instance.getConnections(42);
        } catch (IndexOutOfBoundsException ex) {
            ioobe = ex;
        }
        Assert.assertNotNull(ioobe);
    }

    /**
     * Test of iterator method, of class ConnectionConfigManager.
     */
    @Test
    public void testIterator() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        ConnectionConfig c = new TestConnectionConfig();
        instance.add(c);
        Iterator<ConnectionConfig> iterator = instance.iterator();
        Assert.assertNotNull(iterator);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(c, iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    /**
     * Test of getConnectionManufacturer method, of class
     * ConnectionConfigManager.
     */
    @Test
    public void testGetConnectionManufacturer() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        String result = instance.getConnectionManufacturer(TYPE_A);
        Assert.assertEquals(MFG1, result);
        result = instance.getConnectionManufacturer(TYPE_B);
        Assert.assertEquals(MFG1, result);
        result = instance.getConnectionManufacturer(TYPE_C);
        Assert.assertEquals(MFG2, result);
        result = instance.getConnectionManufacturer("jmri.jmrix.internal.ConnectionConfig");
        Assert.assertEquals(InternalConnectionTypeList.NONE, result);
        result = instance.getConnectionManufacturer("not-a-connection-config-class");
        Assert.assertNull(result);
    }

    private static class TestConnectionConfig extends AbstractSimulatorConnectionConfig {

        @Override
        protected void setInstance() {
            // do nothing
        }

        @Override
        public String name() {
            return this.getClass().getName();
        }
    }
}
