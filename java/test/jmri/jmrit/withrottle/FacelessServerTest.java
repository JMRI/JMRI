package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of FacelessServer
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class FacelessServerTest {

    private FacelessServer panel;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", panel );
    }

    @Test
    public void testGetDeviceList() {
        Assert.assertNotNull("exists", panel.getDeviceList() );
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        panel = new FacelessServer(){
           @Override
           public void listen(){
           }
        };
    }
    
    @After
    public void tearDown() throws Exception {
        try {
          panel.disableServer();
          JUnitUtil.waitFor( () -> { return panel.isListen; });
        } catch(java.lang.NullPointerException npe) {
          // not all tests fully configure the server, so an
          // NPE here is ok.
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
