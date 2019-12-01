package jmri.jmrix.ecos.swing.monitor;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.AbstractMonPaneScaffold;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


/**
 * Test simple functioning of EcosMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class EcosMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    jmri.jmrix.ecos.EcosSystemConnectionMemo memo = null;

    // Test checking the AutoScroll checkbox.
    // for some reason the EcosMonPane has the checkbox value reversed on
    // startup compared to other AbstractMonPane derivatives.
    @Override
    @Test
    public void checkAutoScrollCheckBox(){
         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
         AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

         // for Jemmy to work, we need the pane inside of a frame
         JmriJFrame f = new JmriJFrame();
         try{
            pane.initComponents();
         } catch(Exception ex) {
           Assert.fail("Could not load pane: " + ex);
         }
         f.add(pane);
         // set title if available
         if (pane.getTitle() != null) {
             f.setTitle(pane.getTitle());
         }
         f.pack();
         f.setVisible(true);
         Assert.assertTrue(s.getAutoScrollCheckBoxValue());
         s.checkAutoScrollCheckBox();
         Assert.assertFalse(s.getAutoScrollCheckBoxValue());
         f.setVisible(false);
         f.dispose();
    }

    @Override
    @Before
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
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
