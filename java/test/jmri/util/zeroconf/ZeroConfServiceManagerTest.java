package jmri.util.zeroconf;

import java.util.HashMap;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerPreferences;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the ZeroConfService class
 *
 * @author Paul Bender Copyright (C) 2014
 * @author Randall Wood Copyright (C) 2016
 */
public class ZeroConfServiceManagerTest {

    private static final String HTTP = "_http._tcp.local.";
    private ZeroConfServiceManager manager;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetZeroConfServiceManager();
        JUnitUtil.resetProfileManager();
        // ensure the manager used for tests is also the default manager should
        // some other element involved invoke the default manager
        manager = InstanceManager.setDefault(ZeroConfServiceManager.class, new ZeroConfServiceManager());
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetZeroConfServiceManager();
        manager = null;
        
        // wait for dns threads to end
        Thread.getAllStackTraces().keySet().forEach((t) -> 
            {
                String name = t.getName();
                if (! name.equals("dns.close in ZerConfServiceManager#stopAll")) return; // skip
                
                try {
                    t.join(5000); // wait up to 35 seconds for that thread to end; 
                } catch (InterruptedException e) {
                    // nothing, just means that thread was terminated externally
                }
            }
        );        
        
        JUnitUtil.tearDown();
    }
    
    /**
     * Test of create method, of class ZeroConfServiceManager.
     */
    @Test
    public void testCreate_String_int() {
        ZeroConfService result = manager.create(HTTP, 9999);
        Assert.assertNotNull(result);
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), result.getName());
    }

    /**
     * Test of create method, of class ZeroConfServiceManager.
     */
    @Test
    public void testCreate_3args() {
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = manager.create(HTTP, 9999, properties);
        Assert.assertNotNull(result);
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), result.getName());
    }

    /**
     * Test of create method, of class ZeroConfServiceManager.
     */
    @Test
    public void testCreate_6args() {
        String name = "my name"; // NOI18N
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = manager.create(HTTP, name, 9999, 1, 1, properties);
        Assert.assertNotNull(result);
        Assert.assertEquals(name, result.getName());
    }

    /**
     * Test of getKey method, of class ZeroConfServiceManager.
     */
    @Test
    public void testKey_String_String() {
        Assert.assertEquals("this.that", manager.key("THAT", "THIS"));
    }

    /**
     * Test of isPublished method, of class ZeroConfServiceManager.
     */
    @Test
    public void testIsPublished() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of publish method, of class ZeroConfServiceManager.
     */
    @Test
    public void testPublish() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(instance.isPublished());
        Assert.assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertTrue(manager.isPublished(instance));
    }

    /**
     * Test of stop method, of class ZeroConfServiceManager.
     */
    @Test
    public void testStop() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertTrue(manager.isPublished(instance));
        manager.stop(instance);
        JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == false;
        }, "Stopping ZeroConf Service");
        Assert.assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of stopAll method, of class ZeroConfServiceManager.
     */
    @Test
    public void testStopAll() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertTrue(manager.isPublished(instance));
        manager.stopAll();
        JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == false;
        }, "Stopping ZeroConf Service");
        Assert.assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of allServices method, of class ZeroConfServiceManager.
     */
    @Test
    public void testAllServices() {
        Assert.assertEquals(0, manager.allServices().size());
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getDefaultRailroadName(), instance.getName());
        Assert.assertEquals(0, manager.allServices().size());
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertEquals(1, manager.allServices().size());
    }

    @Test
    public void testHostNameString() {
        Assert.assertEquals("Empty string to punycode", NodeIdentity.networkIdentity(), ZeroConfServiceManager.hostName(""));
        Assert.assertEquals("Whitespace to punycode", NodeIdentity.networkIdentity(), ZeroConfServiceManager.hostName(""));
        Assert.assertEquals("a b", "a-b", ZeroConfServiceManager.hostName("a b"));
        Assert.assertEquals(".a.b", "a-b", ZeroConfServiceManager.hostName(".a.b"));
        Assert.assertEquals("_a_b", "a-b", ZeroConfServiceManager.hostName("_a_b"));
        Assert.assertEquals("My JMRI Railroad", "my-jmri-railroad", ZeroConfServiceManager.hostName("My JMRI Railroad"));
        Assert.assertEquals("Very long name",
                "my-jmri-railroad-my-jmri-railroad-my-jmri-railroad-my-jmri-rail",
                ZeroConfServiceManager.hostName("My JMRI Railroad My JMRI Railroad My JMRI Railroad My JMRI Railroad My JMRI Railroad My JMRI Railroad"));
        Assert.assertEquals("Single emojii is name", "xn--w68h", ZeroConfServiceManager.hostName("🚞"));
        Assert.assertEquals("Single emojii in name", "xn--my--railroad-je87k", ZeroConfServiceManager.hostName("My 🚞 Railroad"));
        Assert.assertEquals("Multiple emojii in name", "xn--my--railroad-4277khl", ZeroConfServiceManager.hostName("My 🚂🚞 Railroad"));
        Assert.assertEquals("Lots of emojii", "xn--358haaaa8nbbbb", ZeroConfServiceManager.hostName("🚂🚞🚂🚞🚂🚞🚂🚞🚂🚞"));
        Assert.assertEquals("Very long name with emojii",
                "xn--my--railroad-my--railroad-my--railroad-my-5g025bnan64joao",
                ZeroConfServiceManager.hostName("My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad"));
    }
}
