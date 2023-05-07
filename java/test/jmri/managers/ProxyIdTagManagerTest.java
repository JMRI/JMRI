package jmri.managers;

import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Steve
 */
public class ProxyIdTagManagerTest extends AbstractProxyManagerTestBase<ProxyIdTagManager, IdTag> {
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        IdTagManager itm = new DefaultIdTagManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setIdTagManager(itm);
        IdTagManager pl = InstanceManager.getDefault(IdTagManager.class);
        if ( pl instanceof ProxyIdTagManager ) {
            l = (ProxyIdTagManager) pl;
        } else {
            Assertions.fail("IdTagManager is not a ProxyIdTagManager");
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
