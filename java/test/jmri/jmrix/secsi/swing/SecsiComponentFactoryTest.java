package jmri.jmrix.secsi.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of SecsiComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SecsiComponentFactoryTest {


    // private SerialTrafficController tc = null;
    private SecsiSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SecsiComponentFactory action = new SecsiComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new SecsiSystemConnectionMemo();
        // tc = new SerialTrafficControlScaffold(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        // tc = null;
    }
}
