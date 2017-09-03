package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.Scale;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.WindowOperator;

/**
 * Swing jfcUnit tests for dispatcher options
 *
 * @author	Dave Duchamp
 */
public class DispatcherFrameTest {

    @Test
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }

        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // test some options from file
        Assert.assertTrue("File AutoTurnouts", d.getAutoTurnouts());
        Assert.assertTrue("File HasOccupancyDetection", d.getHasOccupancyDetection());
        // set all options
        d.setLayoutEditor(null);
        d.setUseConnectivity(false);
        d.setTrainsFromRoster(true);
        d.setTrainsFromTrains(false);
        d.setTrainsFromUser(false);
        d.setAutoAllocate(false);
        d.setAutoTurnouts(false);
        d.setHasOccupancyDetection(false);
        d.setUseScaleMeters(false);
        d.setShortActiveTrainNames(false);
        d.setShortNameInBlock(true);
        d.setExtraColorForAllocated(false);
        d.setNameInAllocatedBlock(false);
        d.setScale(Scale.HO);
        // test all options
        Assert.assertNull("LayoutEditor", d.getLayoutEditor());
        Assert.assertFalse("UseConnectivity", d.getUseConnectivity());
        Assert.assertTrue("TrainsFromRoster", d.getTrainsFromRoster());
        Assert.assertFalse("TrainsFromTrains", d.getTrainsFromTrains());
        Assert.assertFalse("TrainsFromUser", d.getTrainsFromUser());
        Assert.assertFalse("AutoAllocate", d.getAutoAllocate());
        Assert.assertFalse("AutoTurnouts", d.getAutoTurnouts());
        Assert.assertFalse("HasOccupancyDetection", d.getHasOccupancyDetection());
        Assert.assertFalse("UseScaleMeters", d.getUseScaleMeters());
        Assert.assertFalse("ShortActiveTrainNames", d.getShortActiveTrainNames());
        Assert.assertTrue("ShortNameInBlock", d.getShortNameInBlock());
        Assert.assertFalse("ExtraColorForAllocated", d.getExtraColorForAllocated());
        Assert.assertFalse("NameInAllocatedBlock", d.getNameInAllocatedBlock());
        Assert.assertEquals("Scale", Scale.HO, d.getScale());
        // check changing some options
        d.setAutoTurnouts(true);
        Assert.assertTrue("New AutoTurnouts", d.getAutoTurnouts());
        d.setHasOccupancyDetection(true);
        Assert.assertTrue("New HasOccupancyDetection", d.getHasOccupancyDetection());
        d.setShortNameInBlock(false);
        Assert.assertFalse("New ShortNameInBlock", d.getShortNameInBlock());
        d.setScale(Scale.N);
        Assert.assertEquals("New Scale", Scale.N, d.getScale());

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator("Dispatcher");
        // Ask to close Dispatcher window
        dw.requestClose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
