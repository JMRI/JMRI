 package jmri.jmrit.progsupport;

import java.awt.GraphicsEnvironment;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import jmri.*;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the ProgServiceModePane
 *
 * @author	Bob Jacobsen 2008
 */
public class ProgServiceModePaneTest {

    @Test
    public void testCreateHorizontalNone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create and show
        JmriJFrame f = new JmriJFrame("Horizontal None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 0);
        f.setVisible(true);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCreateHorizontalDIRECTBYTEMODE() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // add dummy DCC
        InstanceManager.store(new DefaultProgrammerManager(
                (new ProgrammerScaffold(ProgrammingMode.DIRECTBYTEMODE))), AddressedProgrammerManager.class);
        Assert.assertNotNull("programer manager available", InstanceManager.getDefault(jmri.AddressedProgrammerManager.class));
        // create and show
        JmriJFrame f = new JmriJFrame("Horizontal DIRECTBYTEMODE");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 100);
        f.setVisible(true);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCreateVerticalNone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create and show
        JmriJFrame f = new JmriJFrame("Vertical None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.Y_AXIS,
                        new ButtonGroup()));
        f.pack();
        f.setLocation(0, 200);
        f.setVisible(true);
        JUnitUtil.dispose(f);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
