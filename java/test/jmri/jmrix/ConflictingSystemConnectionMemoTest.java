package jmri.jmrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2019
 */
public class ConflictingSystemConnectionMemoTest extends SystemConnectionMemoTestBase {

    @Override
    @Test
    @SuppressWarnings("deprecation")
    public void testMultipleMemosSamePrefix() {
        assertTrue(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("c"));
        scm.register();
        assertTrue(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("c"));
        SystemConnectionMemo i = new InternalSystemConnectionMemo("i", "internal");
        assertFalse(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("i"));
        i.register();
        assertFalse(SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable("i"));
        SystemConnectionMemo c = new ConflictingSystemConnectionMemo("i", "internal");
        assertEquals("i", c.getSystemPrefix());
        assertEquals("internal", c.getUserName());
    }

    @Before
    @Override
    @SuppressWarnings("deprecation")
    public void setUp() {
        JUnitUtil.setUp();
        scm = new ConflictingSystemConnectionMemo("c", "conflicting");
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
