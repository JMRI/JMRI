package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.EventDataConstants;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.slf4j.LoggerFactory;

/**
 * tests for the LayoutEditor Editor view
 *
 * @author	George Warner Copyright 2017
 */
public class LayoutEditorEditorTest extends jmri.util.SwingTestCase {

    protected void pressMenuItem(JMenuBar inMenuBar, String menuItemName) {
        JMenuBarOperator mbo = new JMenuBarOperator(inMenuBar);
        mbo.pushMenu(menuItemName);
    }

    protected void pressMenuItem1(JMenuBar inMenuBar, String menuItemName) {
        JMenuBarOperator mbo = new JMenuBarOperator(inMenuBar);
        NameComponentChooser ncc = new NameComponentChooser(menuItemName);
        JMenuOperator pmo = new JMenuOperator(mbo, ncc);
        // Click menu
        pmo.pushMenu(menuItemName);
    }

    protected void pressPopupMenuItem(JmriJFrame f, String menuItemName) {
        JFrameOperator jfo = new JFrameOperator(f);
        JDialogOperator jdo = new JDialogOperator(jfo, 1); // wait for the first dialog.
        NameComponentChooser ncc = new NameComponentChooser(menuItemName);
        JPopupMenuOperator pmo = new JPopupMenuOperator(jdo, ncc);
        // Click menu
        pmo.pushMenu(menuItemName);
    }

    @SuppressWarnings("unchecked")
    public void testShowAndClose() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // Can't Assume in TestCase
        }

        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        Assert.assertNotNull("ConfigXmlManager exists", cm);

        // load and display sample file
        java.io.File leFile = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
        cm.load(leFile);
        sleep(100); // time for internal listeners to calm down

        // Find new window by name (should be more distinctive, comes from sample file)
        LayoutEditor le = (LayoutEditor) jmri.util.JmriJFrame.getFrame("My Layout");
        Assert.assertNotNull("LayoutEditor exists", le);

        JComponent leTargetPanel = le.getTargetPanel();
        Assert.assertNotNull("Target Panel exists", le);

        LayoutEditorFindItems leFinder = le.getFinder();
        Assert.assertNotNull("LayoutEditorFindItems exists", leFinder);

        // It's up at this point, and can be manipulated
        // make it editable
        le.setAllEditable(true);
        Assert.assertTrue("isEditable after setAllEditable(true)", le.isEditable());

        // setHighlightSelectedBlock(true)
        le.setHighlightSelectedBlock(true);
        Assert.assertTrue("getHighlightSelectedBlockafter after setHighlightSelectedBlock(true)", le.getHighlightSelectedBlock());

        // find End Bumper
        PositionablePoint ppEB1 = leFinder.findPositionablePointByName("EB1");
        Assert.assertNotNull("End Bumper EB1 exists", ppEB1);

        // Click to show popup
        java.awt.Point location = MathUtil.PointForPoint2D(MathUtil.center(ppEB1.getBounds()));
        getHelper().enterClickAndLeave(new MouseEventData(this,
                leTargetPanel, // component
                1, // number clicks
                EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                true, // isPopUpTrigger
                10, // sleeptime
                EventDataConstants.CUSTOM, // position
                location
        ));

        // find last anchor
        PositionablePoint ppA7 = leFinder.findPositionablePointByName("A7");
        Assert.assertNotNull("Anchor A7 exists", leFinder);

        // Click to show popup
        location = MathUtil.PointForPoint2D(MathUtil.center(ppA7.getBounds()));
        getHelper().enterClickAndLeave(new MouseEventData(this,
                leTargetPanel, // component
                1, // number clicks
                EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                true, // isPopUpTrigger
                10, // sleeptime
                EventDataConstants.CUSTOM, // position
                location
        ));

        // find LayoutTurnout TO1
        LayoutTurnout ltTO1 = leFinder.findLayoutTurnoutByName("TO1");
        Assert.assertNotNull("LayoutTurnout TO1 exists", leFinder);
        int s = ltTO1.getState();

        Assert.assertTrue("LayoutTurnout TO1 in unknown state", ltTO1.getState() == Turnout.UNKNOWN);

        // Click turnout to toggle state
        location = MathUtil.PointForPoint2D(MathUtil.center(ltTO1.getBounds()));

        getHelper().enterClickAndLeave(new MouseEventData(this,
                leTargetPanel, // component
                1, // number clicks
                EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                false, // isPopUpTrigger
                10, // sleeptime
                EventDataConstants.CUSTOM, // position
                location
        ));

        // this will wait for WAITFOR_MAX_DELAY (15 seconds) max 
        // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
        // if it's still false after max wait it throws an assert.
        JUnitUtil.waitFor(() -> {
            return ltTO1.getState() != Turnout.UNKNOWN;
        }, "state not unknown after one click");

        Assert.assertTrue("LayoutTurnout TO1 closed after one click", ltTO1.getState() == Turnout.CLOSED);

        getHelper().enterClickAndLeave(new MouseEventData(this,
                leTargetPanel, // component
                1, // number clicks
                EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                false, // isPopUpTrigger
                10, // sleeptime
                EventDataConstants.CUSTOM, // position
                location
        ));

        JUnitUtil.waitFor(() -> {
            return ltTO1.getState() == Turnout.THROWN;
        }, "LayoutTurnout TO1 thrown after two clicks");

        getHelper().enterClickAndLeave(new MouseEventData(this,
                leTargetPanel, // component
                1, // number clicks
                EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                true, // isPopUpTrigger
                10, // sleeptime
                EventDataConstants.CUSTOM, // position
                location
        ));

        // Ask to close window
        TestHelper.disposeWindow(le, this);

        // Dialog has popped up, so handle that. First, locate it.
        List<JDialog> dialogList = new DialogFinder(null).findAll(le);
        JDialog d = dialogList.get(0);

        // Find the button that deletes the panel
        AbstractButtonFinder finder = new AbstractButtonFinder("Delete Panel");
        JButton button = (JButton) finder.find(d, 0);
        Assert.assertNotNull(button);

        // Click button to delete panel and close window
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

        // another dialog has popped up, so handle that by finding the "Yes - Delete" button.
        dialogList = new DialogFinder(null).findAll(le);
        d = dialogList.get(0);
        finder = new AbstractButtonFinder("Yes - Delete");
        button = (JButton) finder.find(d, 0);
        Assert.assertNotNull(button);

        // Click to say yes, I really mean it.
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
        le.dispose();
    }

    // from here down is testing infrastructure
    public LayoutEditorEditorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LayoutEditorEditorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutEditorEditorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initShutDownManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetWindows(false, false);
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
        super.tearDown();
    }

    //initialize logging
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LayoutEditorEditorTest.class.getName());
}
