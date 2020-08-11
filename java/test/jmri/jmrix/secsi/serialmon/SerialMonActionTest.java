package jmri.jmrix.secsi.serialmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of SerialMonAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialMonActionTest {

    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialMonAction action = new SerialMonAction("Secsi test Action",memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialMonAction action = new SerialMonAction(memo);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
