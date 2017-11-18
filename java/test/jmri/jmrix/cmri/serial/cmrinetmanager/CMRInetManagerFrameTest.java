package jmri.jmrix.cmri.serial.cmrinetmanager;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CMRInetManagerFrame
 *
 * @author	Chuck Catania Copyright (C) 2017
 */
public class CMRInetManagerFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetManagerFrame action = new CMRInetManagerFrame(new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetManagerFrame frame = new CMRInetManagerFrame(new CMRISystemConnectionMemo()); 
        // verify that initCompoents doesn't cause an exception
        frame.initComponents();
        // close
        frame.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
