package jmri.jmrix.loconet.bluetooth;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoNetBluetoothAdapterTest {

    @Test
    public void testCTor() {
        LocoNetBluetoothAdapter t = new LocoNetBluetoothAdapter();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoNetBluetoothAdapterTest.class);

}
