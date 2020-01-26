package jmri.util.zeroconf;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.util.JUnitUtil;
import javax.jmdns.JmDNS;
import jmri.InstanceManager;

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
        ZeroConfServiceEvent t = new ZeroConfServiceEvent(instance, jmdns[0]);
        Assert.assertNotNull("exists", t);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initZeroConfServiceManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetZeroConfServiceManager();
        
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
