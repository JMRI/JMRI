package jmri.jmrix.oaktree.serialmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrix.oaktree.SerialTrafficControlScaffold;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;
import jmri.jmrix.oaktree.SerialTrafficController;

/**
 * Test simple functioning of SerialMonFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialMonFrameTest {

    private SerialTrafficController tc = null;
    private OakTreeSystemConnectionMemo m = null;
 
    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialMonFrame action = new SerialMonFrame(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new OakTreeSystemConnectionMemo();
        tc = new SerialTrafficControlScaffold(m);
        m.setSystemPrefix("ABC");
        m.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
