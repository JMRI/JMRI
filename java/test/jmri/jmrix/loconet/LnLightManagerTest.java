package jmri.jmrix.loconet;

import jmri.Light;
import jmri.LightManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LnLightManagerTest {

    @Test
    public void testCTor() {
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        LnTrafficController lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        LnLightManager t = new LnLightManager(memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testManager() {
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        LnTrafficController lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        LnLightManager lm = new LnLightManager(memo);
        Assert.assertNotNull("exists",lm);
        LnLight light1 = new LnLight("LL1", lnis, lm);
        LnLight light2 = new LnLight("LL01", lnis, lm);
        lm.register(light1);
        lm.register(light2);
        for (Light l : lm.getNamedBeanSet()) {
            System.out.format("Light: %s%n", l.getSystemName());
        }
        
        System.out.format("Light light1: %s%n", lm.getBySystemName("LL1"));
        System.out.format("Light light2: %s%n", lm.getBySystemName("LL01"));
        
        Assert.assertEquals("lights are equal",
                lm.getBySystemName("LL1"),
                lm.getBySystemName("LL01"));
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnLightManagerTest.class);

}
