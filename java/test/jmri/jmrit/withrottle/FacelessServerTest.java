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

    FacelessServer panel;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        panel = new FacelessServer(){
            @Override
            public void listen() {
               // don't actually open the server port for this test.
            }
        };
    }
    
    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
        //panel.disableServer();
    }
}
