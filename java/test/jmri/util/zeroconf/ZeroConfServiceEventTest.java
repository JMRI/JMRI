package jmri.util.zeroconf;

import javax.jmdns.JmDNS;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ZeroConfServiceEventTest {

    private static final String HTTP = "_http._tcp.local.";

    @Test
    public void testCTor() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        JmDNS jmdns[] = InstanceManager.getDefault(ZeroConfServiceManager.class).getDNSes().values().toArray(new JmDNS[0]);
        assertNotNull(jmdns);
        ZeroConfServiceEvent t = new ZeroConfServiceEvent(instance, jmdns[0]);
        assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initZeroConfServiceManager();
    }

    @AfterEach
    public void tearDown() {
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
        
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

}
