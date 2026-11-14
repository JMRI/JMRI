package jmri.util.zeroconf;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerPreferences;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the ZeroConfService class
 *
 * @author Paul Bender Copyright (C) 2014
 * @author Randall Wood Copyright (C) 2016
 */
public class ZeroConfServiceManagerTest {

    private static final String HTTP = "_http._tcp.local.";
    private ZeroConfServiceManager manager;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
        JUnitUtil.resetProfileManager();
        // ensure the manager used for tests is also the default manager should
        // some other element involved invoke the default manager
        manager = InstanceManager.setDefault(ZeroConfServiceManager.class, new ZeroConfServiceManager());
    }

    @AfterEach
    public void tearDown() {
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
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
        assertNotNull(result);
        assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), result.getName());
    }

    /**
     * Test of create method, of class ZeroConfServiceManager.
     */
    @Test
    public void testCreate_3args() {
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = manager.create(HTTP, 9999, properties);
        assertNotNull(result);
        assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), result.getName());
    }

    /**
     * Test of create method, of class ZeroConfServiceManager.
     */
    @Test
    public void testCreate_6args() {
        String name = "my name"; // NOI18N
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = manager.create(HTTP, name, 9999, 1, 1, properties);
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    /**
     * Test of getKey method, of class ZeroConfServiceManager.
     */
    @Test
    public void testKey_String_String() {
        assertEquals("this.that", manager.key("THAT", "THIS"));
    }

    /**
     * Test of isPublished method, of class ZeroConfServiceManager.
     */
    @Test
    public void testIsPublished() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of publish method, of class ZeroConfServiceManager.
     */
    @Test
    public void testPublish() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        assertFalse(instance.isPublished());
        assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assumptions.assumeTrue( JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }), "Timed out publishing ZeroConf Service");
        assertTrue(manager.isPublished(instance));
    }

    /**
     * Test of stop method, of class ZeroConfServiceManager.
     */
    @Test
    public void testStop() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assumptions.assumeTrue( JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }), "Timed out publishing ZeroConf Service");
        assertTrue(manager.isPublished(instance));
        manager.stop(instance);
        JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == false;
        }, "Stopping ZeroConf Service");
        assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of stopAll method, of class ZeroConfServiceManager.
     */
    @Test
    public void testStopAll() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assumptions.assumeTrue( JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }), "Timed out publishing ZeroConf Service");
        assertTrue(manager.isPublished(instance));
        manager.stopAll();
        JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == false;
        }, "Stopping ZeroConf Service");
        assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of allServices method, of class ZeroConfServiceManager.
     */
    @Test
    public void testAllServices() {
        assertEquals(0, manager.allServices().size());
        ZeroConfService instance = manager.create(HTTP, 9999);
        assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getDefaultRailroadName(), instance.getName());
        assertEquals(0, manager.allServices().size());
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assumptions.assumeTrue( JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }), "Timed out publishing ZeroConf Service");
        assertEquals(1, manager.allServices().size());
    }

    /**
     * Stopping a service unregisters it from every address at the same time.
     * JmDNS blocks in unregisterService while it announces that the service
     * has gone; each mock instance here waits in unregisterService until the
     * other has also started, which only happens if both run at once.
     *
     * @throws IOException if a mock is not set up
     */
    @Test
    public void testStopUnregistersFromEveryAddressAtOnce() throws IOException {
        List<JmDNS> dnses = twoBlockingDnses();
        HashMap<InetAddress, JmDNS> saved = useDnses(dnses);
        try {
            ZeroConfService instance = manager.create(HTTP, 9999);
            List<ZeroConfServiceEvent> unpublished = new ArrayList<>();
            instance.addEventListener(new UnpublishedListener(unpublished));
            manager.publish(instance);
            assertTrue(manager.isPublished(instance));

            manager.stop(instance);

            assertTrue(failures.isEmpty(), () -> "unregistered one after another: " + failures);
            for (JmDNS dns : dnses) {
                verify(dns).unregisterService(any(ServiceInfo.class));
                assertFalse(instance.containsServiceInfo(dns.getInetAddress()));
            }
            assertEquals(2, unpublished.size(), "one serviceUnpublished per address");
            assertFalse(manager.isPublished(instance));
        } finally {
            restoreDnses(saved);
        }
    }

    /**
     * Stopping all services unregisters them from every address at the same
     * time, and stopAll() does not close the JmDNS instances.
     *
     * @throws IOException if a mock is not set up
     */
    @Test
    public void testStopAllUnregistersFromEveryAddressAtOnce() throws IOException {
        List<JmDNS> dnses = twoBlockingDnses();
        HashMap<InetAddress, JmDNS> saved = useDnses(dnses);
        try {
            ZeroConfService instance = manager.create(HTTP, 9999);
            List<ZeroConfServiceEvent> unpublished = new ArrayList<>();
            instance.addEventListener(new UnpublishedListener(unpublished));
            manager.publish(instance);
            assertTrue(manager.isPublished(instance));

            manager.stopAll();

            assertTrue(failures.isEmpty(), () -> "unregistered one after another: " + failures);
            for (JmDNS dns : dnses) {
                verify(dns).unregisterAllServices();
                verify(dns, never()).close();
                assertFalse(instance.containsServiceInfo(dns.getInetAddress()));
            }
            assertEquals(2, unpublished.size(), "one serviceUnpublished per address");
            assertTrue(manager.allServices().isEmpty());
        } finally {
            restoreDnses(saved);
        }
    }

    private final List<Exception> failures = Collections.synchronizedList(new ArrayList<>());

    /**
     * Two mock JmDNS instances. Each waits in unregisterService and
     * unregisterAllServices until the other has started its call; the wait
     * ends early only if the calls run one after another, after 30 seconds,
     * and that is recorded in {@link #failures}.
     */
    private List<JmDNS> twoBlockingDnses() throws IOException {
        CyclicBarrier bothStarted = new CyclicBarrier(2);
        List<JmDNS> dnses = new ArrayList<>();
        for (String address : new String[]{"192.0.2.10", "192.0.2.11"}) {
            JmDNS dns = mock(JmDNS.class);
            when(dns.getInetAddress()).thenReturn(InetAddress.getByName(address));
            doAnswer(invocation -> {
                waitForTheOther(bothStarted);
                return null;
            }).when(dns).unregisterService(any(ServiceInfo.class));
            doAnswer(invocation -> {
                waitForTheOther(bothStarted);
                return null;
            }).when(dns).unregisterAllServices();
            dnses.add(dns);
        }
        return dnses;
    }

    private void waitForTheOther(CyclicBarrier bothStarted) {
        try {
            bothStarted.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            failures.add(ex);
        } catch (BrokenBarrierException | TimeoutException ex) {
            failures.add(ex);
        }
    }

    private HashMap<InetAddress, JmDNS> useDnses(List<JmDNS> dnses) throws IOException {
        HashMap<InetAddress, JmDNS> saved = new HashMap<>(ZeroConfServiceManager.JMDNS_SERVICES);
        ZeroConfServiceManager.JMDNS_SERVICES.clear();
        for (JmDNS dns : dnses) {
            ZeroConfServiceManager.JMDNS_SERVICES.put(dns.getInetAddress(), dns);
        }
        return saved;
    }

    private void restoreDnses(HashMap<InetAddress, JmDNS> saved) {
        ZeroConfServiceManager.JMDNS_SERVICES.clear();
        ZeroConfServiceManager.JMDNS_SERVICES.putAll(saved);
    }

    private static class UnpublishedListener implements ZeroConfServiceListener {

        private final List<ZeroConfServiceEvent> unpublished;

        UnpublishedListener(List<ZeroConfServiceEvent> unpublished) {
            this.unpublished = unpublished;
        }

        @Override
        public void serviceQueued(ZeroConfServiceEvent se) {
        }

        @Override
        public void servicePublished(ZeroConfServiceEvent se) {
        }

        @Override
        public void serviceUnpublished(ZeroConfServiceEvent se) {
            unpublished.add(se);
        }
    }

    @Test
    public void testHostNameString() {
        assertEquals( NodeIdentity.networkIdentity(), ZeroConfServiceManager.hostName(""),
            "Empty string to punycode");
        assertEquals( NodeIdentity.networkIdentity(), ZeroConfServiceManager.hostName(" "),
            "Whitespace to punycode");
        assertEquals( "a-b", ZeroConfServiceManager.hostName("a b"), "a b");
        assertEquals( "a-b", ZeroConfServiceManager.hostName(".a.b"), ".a.b");
        assertEquals( "a-b", ZeroConfServiceManager.hostName("_a_b"), "_a_b");
        assertEquals( "my-jmri-railroad", ZeroConfServiceManager.hostName("My JMRI Railroad"), "My JMRI Railroad");
        assertEquals(
                "my-jmri-railroad-my-jmri-railroad-my-jmri-railroad-my-jmri-rail",
                ZeroConfServiceManager.hostName("My JMRI Railroad My JMRI Railroad My JMRI Railroad My JMRI Railroad My JMRI Railroad My JMRI Railroad"),
                "Very long name");
        assertEquals( "xn--w68h", ZeroConfServiceManager.hostName("🚞"), "Single emojii is name");
        assertEquals( "xn--my--railroad-je87k", ZeroConfServiceManager.hostName("My 🚞 Railroad"), "Single emojii in name");
        assertEquals( "xn--my--railroad-4277khl", ZeroConfServiceManager.hostName("My 🚂🚞 Railroad"), "Multiple emojii in name");
        assertEquals( "xn--358haaaa8nbbbb", ZeroConfServiceManager.hostName("🚂🚞🚂🚞🚂🚞🚂🚞🚂🚞"), "Lots of emojii");
        assertEquals(
                "xn--my--railroad-my--railroad-my--railroad-my-5g025bnan64joao",
                ZeroConfServiceManager.hostName("My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad My 🚂🚞 Railroad"),
                "Very long name with emojii");
    }
}
