package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the LnReporterManager class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class LnReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "LR" + i;
    }

    private LnTrafficController tc = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new LocoNetInterfaceScaffold(new LocoNetSystemConnectionMemo());
        l = new LnReporterManager(tc.getSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        tc = null;
        JUnitUtil.tearDown();
    }

    @Override
    protected int maxN() { return 1; }

}
