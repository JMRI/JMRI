package jmri.jmrix.can.cbus.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Steve
 */
public class CbusSimulatedModuleProviderTest {

    @Test
    public void testGetProviderByName() {
        CbusSimulatedModuleProvider result = CbusSimulatedModuleProvider.getProviderByName("NOT A MODULE");
        Assertions.assertNull(result,"Module does not match");
    }

    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
