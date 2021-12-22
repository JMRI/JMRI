package jmri.jmrit.logixng.actions.swing;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLight;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.jmrit.logixng.tools.swing.TreeEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test ActionLightSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionLightSwingTest
        extends SwingConfiguratorInterfaceTestBase
        implements PropertyChangeListener {

    private final AtomicBoolean propertyChanged = new AtomicBoolean();

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ActionLightSwing t = new ActionLightSwing();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("panel is not null",
            null != new ActionLightSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new ActionLightSwing().getConfigPanel(new ActionLight("IQDA1", null), new JPanel()));
    }

    ConditionalNG conditionalNG = null;
    ActionLight action = null;

    @Test
    public void testDialogUseExistingLight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Light l1 = InstanceManager.getDefault(LightManager.class).provide("IL1");
        InstanceManager.getDefault(LightManager.class).provide("IL2");

        ThreadingUtil.runOnGUI(() -> {
            try {
                jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                        .createLogixNG("A logixNG with an empty conditionlNG");
                conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC789", null);

                action = new ActionLight("IQDA1", null);
                MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
                conditionalNG.getChild(0).connect(maleSocket);
            } catch (SocketAlreadyConnectedException e) {
                Assert.fail("SocketAlreadyConnectedException");
            }
        });

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC789", "Edit ! ", 0);
        TreeEditor treeEditor = (TreeEditor)JFrameOperator.findJFrame("Edit ConditionalNG IQC789", true, true);
        treeEditor.addPropertyChangeListener(this);

        new JComboBoxOperator(jdo, 0).setSelectedItem(l1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(ActionLight.LightState.Off);
        propertyChanged.set(false);
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        // Wait for the dialog to be closed
        Assert.assertTrue(JUnitUtil.waitFor(() -> {return propertyChanged.get();}));

        JUnitUtil.waitFor(() -> {return action.getLight() != null;});
        JUnitUtil.waitFor(() -> {return ActionLight.LightState.Off == action.getBeanState();});

        Assert.assertEquals("IL1", action.getLight().getBean().getSystemName());
        Assert.assertEquals(ActionLight.LightState.Off, action.getBeanState());
        
        ThreadingUtil.runOnGUI(() -> { treeEditor.dispose(); });

        Assert.assertTrue(JUnitUtil.waitFor(() -> {return !JUnitUtil.hasOpenFrames();}));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        propertyChanged.set(true);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        InstanceManager.getDefault(LogixNGPreferences.class).setShowSystemUserNames(true);
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        Assert.assertTrue(JUnitUtil.waitFor(() -> {return !JUnitUtil.hasOpenFrames();}));
        JUnitUtil.tearDown(true);
    }

}
