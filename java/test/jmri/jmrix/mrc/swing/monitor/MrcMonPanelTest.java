package jmri.jmrix.mrc.swing.monitor;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import jmri.util.JmriJFrame;
import jmri.jmrix.AbstractMonPaneScaffold;


/**
 * Test simple functioning of MrcMonPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MrcMonPanelTest extends jmri.jmrix.AbstractMonPaneTestBase {

    jmri.jmrix.mrc.MrcSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", pane);
    }

    // Test checking the AutoScroll checkbox.
    // for some reason the MrcMonPanel has the checkbox value reversed on
    // startup compared to other AbstractMonPane derivatives.
    @Override
    @Test
    public void checkAutoScrollCheckBox(){
         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
         AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);
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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.mrc.MrcSystemConnectionMemo();
        jmri.jmrix.mrc.MrcInterfaceScaffold tc = new jmri.jmrix.mrc.MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        jmri.InstanceManager.store(memo, jmri.jmrix.mrc.MrcSystemConnectionMemo.class);
        pane = new MrcMonPanel();
        ((MrcMonPanel)pane).initContext(memo); 
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
