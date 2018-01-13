package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import jmri.jmrit.vsdecoder.VSDConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class VSDConfigDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDConfigDialog t = new VSDConfigDialog(new JPanel(),"test",new VSDConfig());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
