package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test simple functioning of LayoutEditorDialogs
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorDialogsTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule    // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    /**
     * This is called once before all tests
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();
            layoutEditorDialogs = layoutEditor.getLEDialogs();
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
        }
    }

    /**
     * This is called once after all tests
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() {
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
            layoutEditorDialogs = null;
        }
        JUnitUtil.tearDown();
    }

    /**
     * This is called before each test
     *
     * @throws Exception
     */
    @Before
    public void setUpEach() throws Exception {
        //JUnitUtil.setUp();
        //if (!GraphicsEnvironment.isHeadless()) {
        //    JUnitUtil.resetProfileManager();
        //}
    }

    /**
     * This is called after each test
     *
     * @throws Exception
     */
    @After
    public void tearDownEach() throws Exception {
        //JUnitUtil.tearDown();
    }

    private static LayoutEditor layoutEditor = null;
    private static LayoutEditorDialogs layoutEditorDialogs = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor exists", layoutEditor);
        Assert.assertNotNull("layoutEditorDialogs exists", layoutEditorDialogs);
    }

    @Test
    public void testEnterGridSizes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        layoutEditorDialogs.enterGridSizes();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SetGridSizes"));
        Assert.assertNotNull("jFrameOperator 1st exists", jFrameOperator);

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N

        layoutEditorDialogs.enterGridSizes();
        jFrameOperator = new JFrameOperator(Bundle.getMessage("SetGridSizes"));
        Assert.assertNotNull("jFrameOperator 2nd exists", jFrameOperator);

        JTextFieldOperator primaryGridSizeTextFieldOperator = new JTextFieldOperator(jFrameOperator, 0);
        Assert.assertNotNull("primaryGridSizeTextFieldOperator exists", primaryGridSizeTextFieldOperator);

        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("ButtonDone"));
        Assert.assertNotNull("doneButtonOperator exists", doneButtonOperator);

        // try to enter an invalid value in the primary grid size text field
        primaryGridSizeTextFieldOperator.setText("NumberFormatException string");

        Thread misc1 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        //restore valid value
        int oldGridSize1st = layoutEditor.getGridSize();
        primaryGridSizeTextFieldOperator.setText(Integer.toString(oldGridSize1st));

        // try to enter an invalid value in the secondary grid size text field
        JTextFieldOperator secondaryGridSizeTextFieldOperator = new JTextFieldOperator(jFrameOperator, 1);
        Assert.assertNotNull("secondaryGridSizeTextFieldOperator exists", secondaryGridSizeTextFieldOperator);

        secondaryGridSizeTextFieldOperator.setText("NumberFormatException string");

        Thread misc2 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc2");  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        //put in new (valid) values
        primaryGridSizeTextFieldOperator.setText(Integer.toString(oldGridSize1st ^ 1));
        int oldGridSize2nd = layoutEditor.getGridSize2nd();
        secondaryGridSizeTextFieldOperator.setText(Integer.toString(oldGridSize2nd ^ 1));

        doneButtonOperator.doClick();

        Assert.assertEquals("new grid size 1st", oldGridSize1st ^ 1, layoutEditor.getGridSize());
        Assert.assertEquals("new grid size 2nd", oldGridSize2nd ^ 1, layoutEditor.getGridSize2nd());

        layoutEditor.setGridSize(oldGridSize1st);
        Assert.assertEquals("old grid size 1st", oldGridSize1st, layoutEditor.getGridSize());

        layoutEditor.setGridSize2nd(oldGridSize2nd);
        Assert.assertEquals("old grid size 2nd", oldGridSize2nd, layoutEditor.getGridSize2nd());
    }

    @Test
    public void testEnterReporter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        layoutEditorDialogs.enterReporter(150, 200);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("AddReporter"));
        Assert.assertNotNull("jFrameOperator 1st exists", jFrameOperator);

        // cancel the dialog
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        //TODO: any way to verify that the dialog is closed?

        layoutEditorDialogs.enterReporter(150, 200);
        jFrameOperator = new JFrameOperator(Bundle.getMessage("AddReporter"));

        // Try to press done with reporter name blank... should get an error dialog
        JTextFieldOperator reporterNameTextFieldOperator = new JTextFieldOperator(jFrameOperator, 0);
        Assert.assertNotNull("reporterNameTextFieldOperator exists", reporterNameTextFieldOperator);
        reporterNameTextFieldOperator.clearText();

        JButtonOperator addNewLabelButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("AddNewLabel"));
        Assert.assertNotNull("addNewLabelButtonOperator exists", addNewLabelButtonOperator);

        Thread misc1 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        addNewLabelButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        // ok, now set the reporter name to an invalid (doesn't start with IB) value
        reporterNameTextFieldOperator.setText("ClarkKent");
        addNewLabelButtonOperator.doClick();
        jmri.util.JUnitAppender.assertErrorMessage("Invalid system name for Reporter: System name must start with \"IR\".");
        //TODO: any way to verify that the dialog is closed?

        layoutEditorDialogs.enterReporter(150, 200);
        jFrameOperator = new JFrameOperator(Bundle.getMessage("AddReporter"));

        // ok, now set the reporter name to an valid (starts with IB) value
        reporterNameTextFieldOperator = new JTextFieldOperator(jFrameOperator, 0);
        Assert.assertNotNull("reporterNameTextFieldOperator exists", reporterNameTextFieldOperator);
        reporterNameTextFieldOperator.setText("IBClarkKent");

        // try to enter an invalid value in X location text field
        JTextFieldOperator xLocationTextFieldOperator = new JTextFieldOperator(jFrameOperator, 1);
        Assert.assertNotNull("xLocationTextFieldOperator exists", xLocationTextFieldOperator);
        xLocationTextFieldOperator.setText("NumberFormatException string");

        Thread misc2 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        addNewLabelButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        // now set the X location text field to a valid value
        xLocationTextFieldOperator.setText("50");

        // try to enter an invalid value in Y location text field
        JTextFieldOperator yLocationTextFieldOperator = new JTextFieldOperator(jFrameOperator, 1);
        Assert.assertNotNull("yLocationTextFieldOperator exists", yLocationTextFieldOperator);
        yLocationTextFieldOperator.setText("NumberFormatException string");

        Thread misc3 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        addNewLabelButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc3.isAlive());
        }, "misc3 finished");

        // now set the Y location text field to a valid value
        yLocationTextFieldOperator.setText("100");

        // and everything should work!
        addNewLabelButtonOperator.doClick();
    }

    @Test
    public void testScaleTrackDiagram() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        layoutEditorDialogs.scaleTrackDiagram();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("ScaleTrackDiagram"));
        Assert.assertNotNull("jFrameOperator 1st exists", jFrameOperator);

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        //TODO: any way to verify that the dialog is closed?

        // reopen scale track diagram
        layoutEditorDialogs.scaleTrackDiagram();
        jFrameOperator = new JFrameOperator(Bundle.getMessage("ScaleTrackDiagram"));

        // get ScaleTranslate button
        JButtonOperator scaleTranslateButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("ScaleTranslate"));
        Assert.assertNotNull("addNewLabelButtonOperator exists", scaleTranslateButtonOperator);

        // try to enter an invalid value in horizontal (x) translation text field
        JTextFieldOperator horizontalTranslationTextFieldOperator = new JTextFieldOperator(jFrameOperator, 0);
        Assert.assertNotNull("horizontalTranslationTextFieldOperator exists", horizontalTranslationTextFieldOperator);
        horizontalTranslationTextFieldOperator.setText("NumberFormatException string");

        Thread misc1 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        // now set the horizontal (x) translation text field
        horizontalTranslationTextFieldOperator.setText("50");

        // try to enter an invalid value in vertical (y) translation text field
        JTextFieldOperator verticalTranslationTextFieldOperator = new JTextFieldOperator(jFrameOperator, 1);
        Assert.assertNotNull("verticalTranslationTextFieldOperator exists", verticalTranslationTextFieldOperator);
        verticalTranslationTextFieldOperator.setText("NumberFormatException string");

        Thread misc2 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        // now set the vertical (y) translation text field
        verticalTranslationTextFieldOperator.setText("100");

        // try to enter an invalid value in the horizontal (x) scale factor text field
        JTextFieldOperator horizontalScaleFactorTextFieldOperator = new JTextFieldOperator(jFrameOperator, 1);
        Assert.assertNotNull("horizontalScaleFactorTextFieldOperatorexists", horizontalScaleFactorTextFieldOperator);
        horizontalScaleFactorTextFieldOperator.setText("NumberFormatException string");

        Thread misc3 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc3.isAlive());
        }, "misc3 finished");

        // now set the horizontal (x) scale factor text field
        horizontalScaleFactorTextFieldOperator.setText("2");

        // try to enter an invalid value in vertical (y) scale factor text field
        JTextFieldOperator verticalScaleFactorTextFieldOperator = new JTextFieldOperator(jFrameOperator, 1);
        Assert.assertNotNull("verticalScaleFactorTextFieldOperator exists", verticalScaleFactorTextFieldOperator);
        verticalScaleFactorTextFieldOperator.setText("NumberFormatException string");

        Thread misc4 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc4.isAlive());
        }, "misc4 finished");

        // now set the Y  (vertical) translation text field
        verticalScaleFactorTextFieldOperator.setText("2");

        // and everything should work!
        scaleTranslateButtonOperator.doClick();
    }

    @Test
    public void testMoveSelection() {

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        layoutEditorDialogs.moveSelection();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("TranslateSelection"));
        Assert.assertNotNull("jFrameOperator 1st exists", jFrameOperator);

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        //TODO: any way to verify that the dialog is closed?

        // reopen scale track diagram
        layoutEditorDialogs.moveSelection();
        jFrameOperator = new JFrameOperator(Bundle.getMessage("TranslateSelection"));

        // get MoveSelection button
        JButtonOperator moveSelectionButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("MoveSelection"));
        Assert.assertNotNull("addNewLabelButtonOperator exists", moveSelectionButtonOperator);

        // try to enter an invalid value in horizontal (x) translation text field
        JTextFieldOperator horizontalTranslationTextFieldOperator = new JTextFieldOperator(jFrameOperator, 0);
        Assert.assertNotNull("horizontalTranslationTextFieldOperator exists", horizontalTranslationTextFieldOperator);
        horizontalTranslationTextFieldOperator.setText("NumberFormatException string");

        Thread misc1 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        moveSelectionButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        // now set the horizontal (x) translation text field
        horizontalTranslationTextFieldOperator.setText("50");

        // try to enter an invalid value in vertical (y) translation text field
        JTextFieldOperator verticalTranslationTextFieldOperator = new JTextFieldOperator(jFrameOperator, 1);
        Assert.assertNotNull("verticalTranslationTextFieldOperator exists", verticalTranslationTextFieldOperator);
        verticalTranslationTextFieldOperator.setText("NumberFormatException string");

        Thread misc2 = createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        moveSelectionButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        // now set the vertical (y) translation text field
        verticalTranslationTextFieldOperator.setText("100");

        // and everything should work!
        moveSelectionButtonOperator.doClick();
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);  // NOI18N
        t.start();
        return t;
    }
}
