package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
