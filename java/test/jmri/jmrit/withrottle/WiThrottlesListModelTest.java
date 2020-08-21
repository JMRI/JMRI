package jmri.jmrit.withrottle;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of WiThrottlesListModel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class WiThrottlesListModelTest {

    @Test
    public void testCtor() {
        java.util.ArrayList<DeviceServer> al = new java.util.ArrayList<DeviceServer>(); 
        WiThrottlesListModel panel = new WiThrottlesListModel(al);
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
