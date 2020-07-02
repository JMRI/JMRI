package jmri.jmrix.rps.aligntable;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Test simple functioning of AlignTableAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AlignTableActionTest {

    private RpsSystemConnectionMemo memo = null;

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction("RPS test Action",memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction(memo);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
