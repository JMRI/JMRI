package jmri.jmrit.z21server;

import jmri.util.JUnitUtil;

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
        Assertions.assertNotNull( s, "exists" );
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
