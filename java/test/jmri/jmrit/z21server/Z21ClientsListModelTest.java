package jmri.jmrit.z21server;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of WiThrottlesListModel
 *
 * @author Eckart Meyer (C) 2025
 */
public class Z21ClientsListModelTest {

    @Test
    public void testCtor() {
        Z21ClientsListModel panel = new Z21ClientsListModel();
        Assert.assertNotNull("exists", panel );
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }
}
