package jmri.jmrit.z21server;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ClientManager
 *
 * @author Eckart Meyer (C) 2025
 */
public class ClientManagerTest {

    @Test
    public void testCtor() {
        ClientManager s = ClientManager.getInstance();
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
