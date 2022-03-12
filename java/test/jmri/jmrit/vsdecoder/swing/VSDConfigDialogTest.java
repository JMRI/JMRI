package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.*;
import jmri.jmrit.vsdecoder.VSDConfig;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDConfigDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDConfigDialog t = new VSDConfigDialog(new JPanel(), "test", new VSDConfig(), false, false);
        Assert.assertNotNull("exists", t);

        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("VSDecoderManagerThread");
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
