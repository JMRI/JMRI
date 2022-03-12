package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Note: Standalone only allows _one_ NamedBean, named e.g. RR1, which means
 * certain tests are defaulted away.
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class StandaloneReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "RR" + i;
    }

    StandaloneTrafficController tc = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        RfidSystemConnectionMemo memo = new RfidSystemConnectionMemo();
        tc = new StandaloneTrafficController(memo);
        memo.setSystemPrefix("R");
        memo.setRfidTrafficController(tc);
        l = new StandaloneReporterManager(tc.getAdapterMemo()) {
            @Override
            public void message(jmri.jmrix.rfid.RfidMessage m) {
            }

            @Override
            public synchronized void reply(jmri.jmrix.rfid.RfidReply m) {
            }

        };
    }

    // No test for manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    // No test for manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }

    @Override
    protected int maxN() {
        return 1;
    }

    @Override
    protected String getNameToTest1() {
        return "1";
    }
}
