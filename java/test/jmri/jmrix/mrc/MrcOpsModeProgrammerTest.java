package jmri.jmrix.mrc;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MrcOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    @Override
    @Test
    public void testGetCanWriteAddress() {
        Assert.assertFalse("can write address", programmer.getCanWrite("1234"));
    }    

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        MrcSystemConnectionMemo memo = new MrcSystemConnectionMemo();
        MrcInterfaceScaffold tc = new MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        jmri.InstanceManager.store(memo, MrcSystemConnectionMemo.class);
        MrcOpsModeProgrammer t = new MrcOpsModeProgrammer(memo,5,false);
        programmer = t;
    }

    @AfterEach
    @Override
    public void tearDown() {
        programmer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MrcOpsModeProgrammerTest.class);

}
