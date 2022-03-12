package jmri.jmrix.mrc.swing.monitor;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.AbstractMonPaneScaffold;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * Test simple functioning of MrcMonPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MrcMonPanelTest extends jmri.jmrix.AbstractMonPaneTestBase {

    jmri.jmrix.mrc.MrcSystemConnectionMemo memo = null;

    // Test checking the AutoScroll checkbox.
    // for some reason the MrcMonPanel has the checkbox value reversed on
    // startup compared to other AbstractMonPane derivatives.
    @Override
    @Test
    public void checkAutoScrollCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable(() -> ThreadingUtil.runOnGUI(() -> pane.initComponents()));
        assertThat(thrown).withFailMessage("could not load pane: " + thrown).isNull();

        ThreadingUtil.runOnGUI(() -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
        Assert.assertTrue(s.getAutoScrollCheckBoxValue());
        s.checkAutoScrollCheckBox();
        Assert.assertFalse(s.getAutoScrollCheckBoxValue());
        f.setVisible(false);
        f.dispose();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.mrc.MrcSystemConnectionMemo();
        jmri.jmrix.mrc.MrcInterfaceScaffold tc = new jmri.jmrix.mrc.MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        jmri.InstanceManager.store(memo, jmri.jmrix.mrc.MrcSystemConnectionMemo.class);
        // pane for AbstractMonPaneTestBase, panel for JmriPanelTest
        panel = pane = new MrcMonPanel();
        ((MrcMonPanel) pane).initContext(memo);
        helpTarget = "package.jmri.jmrix.mrc.swing.monitor.MrcMonPanel";
        title = "Open MRC Monitor";
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.tearDown();
    }

}
