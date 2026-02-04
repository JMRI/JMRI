package jmri.jmrix.marklin.simulation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
 
/**
 * Tests for the MarklinSimDriverAdapter class.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimDriverAdapterTest {

    @Test
    public void testCTor() {
        MarklinSimDriverAdapter t = new MarklinSimDriverAdapter();
        Assertions.assertNotNull( t, "exists");
        Assertions.assertNull(t.getInputStream());
        Assertions.assertNull(t.getOutputStream());
        Assertions.assertTrue(t.status());
    }

    @Test
    public void testConfigure() {
        MarklinSimDriverAdapter t = new MarklinSimDriverAdapter();
        t.configure();
        var memo = jmri.InstanceManager.getNullableDefault(jmri.jmrix.marklin.MarklinSystemConnectionMemo.class);
        Assertions.assertNotNull(memo);
        var tc = memo.getTrafficController();
        Assertions.assertNotNull(tc);
        tc.terminateThreads();
        memo.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
