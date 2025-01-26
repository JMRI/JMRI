package jmri.jmrit.z21server;

import java.net.InetAddress;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of AppClient
 *
 * @author Eckart Meyer (C) 2025
 */
public class AppClientTest {

    @Test
    public void testCtor() {
        InetAddress addr = InetAddress.getLoopbackAddress();
        AppClient s = new AppClient(addr, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        Assert.assertNotNull("exists", s );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
