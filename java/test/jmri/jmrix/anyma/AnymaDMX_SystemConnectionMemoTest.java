package jmri.jmrix.anyma;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for AnymaDMX_SystemConnectionMemo class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_SystemConnectionMemoTest extends SystemConnectionMemoTestBase<AnymaDMX_SystemConnectionMemo> {

    @Test
    public void testDefaultCtor() {
        Assert.assertNotNull("exists", scm);
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new AnymaDMX_SystemConnectionMemo();
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.dispose();
        scm = null;
        JUnitUtil.tearDown();
    }
}
