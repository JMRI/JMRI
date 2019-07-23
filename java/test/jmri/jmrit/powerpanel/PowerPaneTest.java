package jmri.jmrit.powerpanel;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the Jmrit PowerPanel
 *
 * @author	Bob Jacobsen
 */
public class PowerPaneTest extends jmri.util.swing.JmriPanelTest {

    // setup a default PowerManager interface
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDebugPowerManager();
        panel = new PowerPane();
        helpTarget="package.jmri.jmrit.powerpanel.PowerPanelFrame";
        title=Bundle.getMessage("TitlePowerPanel");
    }
    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // test on button routine
    @Test
    public void testPushOn() {
        ((PowerPane) panel).onButtonPushed();
        Assert.assertEquals("Testing shown on/off", "On", ((PowerPane) panel).onOffStatus.getText());
    }

    // test off button routine
    @Test
    public void testPushOff() {
        ((PowerPane) panel).offButtonPushed();
        Assert.assertEquals("Testing shown on/off", "Off", ((PowerPane) panel).onOffStatus.getText());
    }

    // click on button
    @Test
    public void testOnClicked() {
        ((PowerPane) panel).onButton.doClick();
        Assert.assertEquals("Testing shown on/off", "On", ((PowerPane) panel).onOffStatus.getText());
    }

    // click off button
    @Test
    public void testOffClicked() {
        ((PowerPane) panel).offButton.doClick();
        Assert.assertEquals("Testing shown on/off", "Off", ((PowerPane) panel).onOffStatus.getText());
    }


}
