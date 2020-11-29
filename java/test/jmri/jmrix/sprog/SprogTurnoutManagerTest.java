package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogTurnoutManager.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private SprogSystemConnectionMemo m = null;

    @Override
    public String getSystemName(int i) {
        return "ST" + i;
    }

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull(l);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new SprogSystemConnectionMemo();
        l = new SprogTurnoutManager(m);
    }

    @AfterEach
    public void tearDown() {
        m = null;
        l = null;
        JUnitUtil.tearDown();
    }

}
