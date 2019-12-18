package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Note: Standalone only allows _one_ NamedBean, named e.g. RR1, which means
 * certain tests are defaulted away.
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class StandaloneReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "RR" + i;
    }

    StandaloneTrafficController tc = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        StandaloneSystemConnectionMemo memo = new StandaloneSystemConnectionMemo();
        tc = new StandaloneTrafficController(memo);
        memo.setSystemPrefix("R");
        memo.setRfidTrafficController(tc);
        l = new StandaloneReporterManager(tc.getAdapterMemo()) {
            @Override
            public void message(jmri.jmrix.rfid.RfidMessage m) {
            }

            @Override
            public void reply(jmri.jmrix.rfid.RfidReply m) {
            }

        };
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
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
