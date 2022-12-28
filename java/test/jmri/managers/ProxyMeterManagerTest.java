package jmri.managers;

import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Steve Young Copyright(c) 2022
 */
public class ProxyMeterManagerTest extends AbstractProxyManagerTestBase<ProxyMeterManager, Meter> {
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        MeterManager itm = new jmri.jmrix.internal.InternalMeterManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setMeterManager(itm);
        MeterManager pl = InstanceManager.getDefault(MeterManager.class);
        if ( pl instanceof ProxyMeterManager ) {
            l = (ProxyMeterManager) pl;
        } else {
            Assertions.fail("IdTagManager is not a ProxyIdTagManager");
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
