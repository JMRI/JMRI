package jmri.jmrit.z21server;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MainServer
 *
 * @author Eckart Meyer (C) 2025
 */
public class MainServerTest {

    @Test
    public void testCtor() {
        MainServer s = new MainServer();
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
