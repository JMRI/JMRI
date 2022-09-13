package jmri.jmrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.SystemConnectionMemo;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2019
 */
public class CaptiveSystemConnectionMemoTest extends SystemConnectionMemoTestBase<CaptiveSystemConnectionMemo> {

    @Override
    @Test
    public void testMultipleMemosSamePrefix() {
        assertTrue(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("c"));
        scm.register();
        assertTrue(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("c"));
        SystemConnectionMemo i = new InternalSystemConnectionMemo("i", "internal");
        assertFalse(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("i"));
        i.register();
        assertFalse(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("i"));
        SystemConnectionMemo c = new CaptiveSystemConnectionMemo("i", "internal");
        assertEquals("i", c.getSystemPrefix());
        assertEquals("internal", c.getUserName());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new CaptiveSystemConnectionMemo("c", "conflicting");
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
