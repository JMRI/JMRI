// PowerPaneTest.java
package jmri.jmrit.powerpanel;

import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Jmri package
 *
 * @author	Bob Jacobsen
 */
public class PowerPaneTest extends TestCase {

    // setup a default PowerManager interface
    @Override
    public void setUp() {
        JUnitUtil.initDebugPowerManager();
    }

    // test creation
    public void testCreate() {
        PowerPane p = new PowerPane();
        Assert.assertNotNull("exists", p);
    }

    // test on button routine
    public void testPushOn() {
        PowerPane p = new PowerPane();
        p.onButtonPushed();
        Assert.assertEquals("Testing shown on/off", "On", p.onOffStatus.getText());
    }

    // test off button routine
    public void testPushOff() {
        PowerPane p = new PowerPane();
        p.offButtonPushed();
        Assert.assertEquals("Testing shown on/off", "Off", p.onOffStatus.getText());
    }

    // click on button
    public void testOnClicked() {
        PowerPane p = new PowerPane();
        p.onButton.doClick();
        Assert.assertEquals("Testing shown on/off", "On", p.onOffStatus.getText());
    }

    // click off button
    public void testOffClicked() {
        PowerPane p = new PowerPane();
        p.offButton.doClick();
        Assert.assertEquals("Testing shown on/off", "Off", p.onOffStatus.getText());
    }

    // from here down is testing infrastructure
    public PowerPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PowerPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PowerPaneTest.class);
        return suite;
    }

}
