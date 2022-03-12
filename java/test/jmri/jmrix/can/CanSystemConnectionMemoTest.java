package jmri.jmrix.can;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CanSystemConnectionMemoTest extends SystemConnectionMemoTestBase<CanSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        // without knowing which system is connected via can, there is no
        // way to provide a consist manager.
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new CanSystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CanSystemConnectionMemoTest.class);
}
