package jmri.jmrix.rfid.merg.concentrator;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

/**
 * ConcentratorReporterManagerTest.java
 * <p>
 * Test for the ConcentratorReporterManager class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "RR" + "A";
    }

    @Override
    protected int maxN() {
        return 1;
    }

    @Override
    protected String getNameToTest1() {
        return "A";
    }

    @Override
    protected String getNameToTest2() {
        return "C";
    }

    @Override
    @Test
    @NotApplicable("Not supported by this manager at this time")
    public void testReporterProvideByNumber() {
    }

    @Test
    @Override
    @NotApplicable("No test for manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Test
    @Override
    @NotApplicable("No test for manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    ConcentratorTrafficController tc = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        RfidSystemConnectionMemo memo = new RfidSystemConnectionMemo();
        tc = new ConcentratorTrafficController(memo, "A-H") {
            @Override
            public void sendInitString() {
            }
        };
        memo.setRfidTrafficController(tc);
        memo.setSystemPrefix("R");
        l = new ConcentratorReporterManager(tc.getAdapterMemo()) {
            @Override
            public void message(jmri.jmrix.rfid.RfidMessage m) {
            }

            @Override
            public synchronized void reply(jmri.jmrix.rfid.RfidReply m) {
            }

        };
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }

}
