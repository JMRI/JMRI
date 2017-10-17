package jmri.jmrix.cmri.serial.assignment;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ListFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ListFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ListFrame action = new ListFrame(new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
