package jmri.jmrix.ecos.swing.monitor;

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
 * Test simple functioning of EcosMonPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EcosMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    jmri.jmrix.ecos.EcosSystemConnectionMemo memo = null;

    // Test checking the AutoScroll checkbox.
    // for some reason the EcosMonPane has the checkbox value reversed on
    // startup compared to other AbstractMonPane derivatives.
    @Override
    @Test
    public void checkAutoScrollCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable(() -> ThreadingUtil.runOnGUI(() -> pane.initComponents()));
        assertThat(thrown).withFailMessage("could not load pane " + thrown).isNull();

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
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrix.ecos.EcosInterfaceScaffold tc = new jmri.jmrix.ecos.EcosInterfaceScaffold();
        memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo(tc);
        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
        // pane for AbstactMonPaneBase, panel for JmriJPanel
        panel = pane = new EcosMonPane();
        ((EcosMonPane)pane).initContext(memo); 
        title = "ECoS Command Monitor";
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
