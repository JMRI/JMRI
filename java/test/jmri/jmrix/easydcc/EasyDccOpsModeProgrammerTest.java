package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EasyDccOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    private EasyDccSystemConnectionMemo _memo;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        _memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        _memo.setEasyDccTrafficController(new EasyDccTrafficControlScaffold(_memo));
        EasyDccOpsModeProgrammer p = new EasyDccOpsModeProgrammer(100, false, _memo);
        programmer = p;
    }

    @After
    @Override
    public void tearDown() {
        _memo.getTrafficController().terminateThreads();
        _memo = null;
        programmer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccOpsModeProgrammerTest.class);

}
