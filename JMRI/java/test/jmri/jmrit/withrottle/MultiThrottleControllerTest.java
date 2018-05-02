package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MultiThrottleController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MultiThrottleControllerTest {
    
    private FacelessServer f;
    private java.net.Socket s;

    @Test
    public void testCtor() {
        DeviceServer ds = new DeviceServer(s,f);
        jmri.util.JUnitAppender.assertErrorMessage("Stream creation failed (DeviceServer)");
        MultiThrottleController panel = new MultiThrottleController('A',"test",ds,ds);
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
        InstanceManager.setDefault(WiThrottlePreferences.class, new WiThrottlePreferences());
        s = new java.net.Socket();
        f = new FacelessServer(){
            @Override
            public void listen(){
            }
        };
    }
    
    @After
    public void tearDown() throws Exception {
        try {
          f.disableServer();
          JUnitUtil.waitFor( () -> { return f.isListen; });
        } catch(java.lang.NullPointerException npe) {
          // not all tests fully configure the server, so an
          // NPE here is ok.
        }
        JUnitUtil.tearDown();
    }
}
