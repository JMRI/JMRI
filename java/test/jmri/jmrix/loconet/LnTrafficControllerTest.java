package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bob Jacobsen Copyright (c) 2002
 */
public class LnTrafficControllerTest {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", memo.getLnTrafficController() );
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);
        lnis.setSystemConnectionMemo(memo);
    }

    @After
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
