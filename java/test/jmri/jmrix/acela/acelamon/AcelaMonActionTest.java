package jmri.jmrix.acela.acelamon;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of AcelaMonAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AcelaMonActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AcelaMonAction action = new AcelaMonAction("Acela test Action", new AcelaSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AcelaSystemConnectionMemo memo = new AcelaSystemConnectionMemo();
        InstanceManager.setDefault(AcelaSystemConnectionMemo.class, memo);
        AcelaMonAction action = new AcelaMonAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
