package jmri.jmrit.dispatcher;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.JMenuItem;

import jmri.InstanceManager;
import jmri.jmrit.dispatcher.DispatcherFrame.TrainsFrom;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OptionsMenuTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        OptionsMenu t = new OptionsMenu(d);
        Assert.assertNotNull("exists",t);
        Assert.assertEquals("Stopping Speed Name", "Restricted", d.getStoppingSpeedName());
        Assert.assertEquals("Use Connectivity Option", false, d.getUseConnectivity());
        Assert.assertEquals("Trains From Roster",TrainsFrom.TRAINSFROMROSTER, d.getTrainsFrom());
        Assert.assertEquals("AutoAllocate", false, d.getAutoAllocate());
        Assert.assertEquals("Auto Turnouts", true, d.getAutoTurnouts());
        Assert.assertEquals("Occupancy detection", true, d.getHasOccupancyDetection());
        Assert.assertEquals("Short Active Train Name", false, d.getShortActiveTrainNames());
        Assert.assertEquals("Short Train Name in Block", true, d.getShortNameInBlock());
        Assert.assertEquals("Extra Colour for allocate", false, d.getExtraColorForAllocated());
        Assert.assertEquals("Name In Allocated Block", false, d.getNameInAllocatedBlock());
        Assert.assertEquals("Layout Scale", "G", d.getScale().getScaleName());
        Assert.assertEquals("Use Metres", true, d.getUseScaleMeters());
        JUnitUtil.dispose(d);
    }

    @Test
    public void testWaitWhereTrainsCanStopCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        JUnitUtil.initDefaultUserMessagePreferences();

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        d.setAutoTurnouts(false);  // or Apply warns that there is no panel to set turnouts from
        OptionsMenu t = new OptionsMenu(d);
        Assert.assertFalse("off to start with", d.getWaitWhereTrainsCanStop());

        // the window shows the option as Dispatcher has it, and Apply sets it
        d.setWaitWhereTrainsCanStop(true);
        openOptionsWindow(t);
        JFrameOperator window = new JFrameOperator(Bundle.getMessage("OptionWindowItem"));
        JCheckBoxOperator box = new JCheckBoxOperator(window, Bundle.getMessage("waitWhereTrainsCanStop"));
        Assert.assertTrue("shown on", box.isSelected());
        box.changeSelection(false);
        new JButtonOperator(window, Bundle.getMessage("ButtonApply")).push();
        window.waitClosed();
        Assert.assertFalse("Apply turns it off", d.getWaitWhereTrainsCanStop());

        openOptionsWindow(t);
        window = new JFrameOperator(Bundle.getMessage("OptionWindowItem"));
        box = new JCheckBoxOperator(window, Bundle.getMessage("waitWhereTrainsCanStop"));
        Assert.assertFalse("shown off", box.isSelected());
        box.changeSelection(true);
        new JButtonOperator(window, Bundle.getMessage("ButtonApply")).push();
        window.waitClosed();
        Assert.assertTrue("Apply turns it on", d.getWaitWhereTrainsCanStop());

        JUnitUtil.dispose(d);
    }

    private static void openOptionsWindow(OptionsMenu menu) {
        String text = Bundle.getMessage("OptionWindowItem") + "...";
        for (Component c : menu.getMenuComponents()) {
            if (c instanceof JMenuItem && text.equals(((JMenuItem) c).getText())) {
                ThreadingUtil.runOnGUI(() -> ((JMenuItem) c).doClick());
                return;
            }
        }
        Assert.fail("no menu item " + text);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(OptionsMenuTest.class);

}
