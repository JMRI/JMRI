package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EasyDccOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    private EasyDccSystemConnectionMemo _memo;

    // The minimal setup for log4J
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
