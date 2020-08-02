package jmri.jmrix.cmri.serial.diagnostic;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of DiagnosticAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DiagnosticActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DiagnosticAction action = new DiagnosticAction("CMRI test Action",new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DiagnosticAction action = new DiagnosticAction(new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
