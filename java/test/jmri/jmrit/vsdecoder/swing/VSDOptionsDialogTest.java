package jmri.jmrit.vsdecoder.swing;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import javax.swing.JPanel;

import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDOptionsDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JPanel parent = new JPanel();
        VSDOptionsDialog t = new VSDOptionsDialog(parent,"test");
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetWindows(false,false);
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDOptionsDialogTest.class.getName());

}
