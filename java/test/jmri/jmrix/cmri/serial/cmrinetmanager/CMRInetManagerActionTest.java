package jmri.jmrix.cmri.serial.cmrinetmanager;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CMRInetManagerAction
 * Copied from Diagnostic
 * @author Chuck Catania Copyright (C) 2017
 */
public class CMRInetManagerActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetManagerAction action = new CMRInetManagerAction("CMRI test Action",new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetManagerAction action = new CMRInetManagerAction(new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
