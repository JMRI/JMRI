package jmri.jmrit.powerpanel;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Jmrit PowerPanel
 *
 * @author	Bob Jacobsen
 */
public class PowerPaneTest {

    // setup a default PowerManager interface
    @Before
    public void setUp() {
        JUnitUtil.initDebugPowerManager();
    }

    // test creation
    @Test
    public void testCreate() {
        PowerPane p = new PowerPane();
        Assert.assertNotNull("exists", p);
    }

    // test on button routine
    @Test
    public void testPushOn() {
        PowerPane p = new PowerPane();
        p.onButtonPushed();
        Assert.assertEquals("Testing shown on/off", "On", p.onOffStatus.getText());
    }

    // test off button routine
    @Test
    public void testPushOff() {
        PowerPane p = new PowerPane();
        p.offButtonPushed();
        Assert.assertEquals("Testing shown on/off", "Off", p.onOffStatus.getText());
    }

    // click on button
    @Test
    public void testOnClicked() {
        PowerPane p = new PowerPane();
        p.onButton.doClick();
        Assert.assertEquals("Testing shown on/off", "On", p.onOffStatus.getText());
    }

    // click off button
    @Test
    public void testOffClicked() {
        PowerPane p = new PowerPane();
        p.offButton.doClick();
        Assert.assertEquals("Testing shown on/off", "Off", p.onOffStatus.getText());
    }

    @Test
    public void testGetHelpTarget() {
        PowerPane t = new PowerPane();
        Assert.assertEquals("help target","package.jmri.jmrit.powerpanel.PowerPanelFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        PowerPane t = new PowerPane();
        Assert.assertEquals("title",Bundle.getMessage("TitlePowerPanel"),t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        PowerPane t = new PowerPane();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

}
