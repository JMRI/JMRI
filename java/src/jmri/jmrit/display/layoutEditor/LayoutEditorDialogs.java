package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout Editor Dialogs implements some dialogs for the Layout Editor
 *
 * @author George Warner Copyright (c) 2019
 */
public class LayoutEditorDialogs {

    // operational instance variables shared between tools
    private LayoutEditor layoutEditor = null;

    // constructor method
    public LayoutEditorDialogs(@Nonnull LayoutEditor thePanel) {
        layoutEditor = thePanel;
    }

    /*====================================*\
    |* Dialog box to enter new grid sizes *|
    \*====================================*/
    //operational variables for enter grid sizes pane
    private transient JmriJFrame enterGridSizesFrame = null;
    private boolean enterGridSizesOpen = false;
    private boolean gridSizesChange = false;
    private transient JTextField primaryGridSizeField = new JTextField(6);
    private transient JTextField secondaryGridSizeField = new JTextField(6);
    private transient JButton gridSizesDone;
    private transient JButton gridSizesCancel;

    //display dialog for entering grid sizes
    @InvokeOnGuiThread
    protected void enterGridSizes() {
        if (enterGridSizesOpen) {
            enterGridSizesFrame.setVisible(true);
            return;
        }

        //Initialize if needed
        if (enterGridSizesFrame == null) {
            enterGridSizesFrame = new JmriJFrame(Bundle.getMessage("SetGridSizes"));
            enterGridSizesFrame.addHelpMenu("package.jmri.jmrit.display.EnterGridSizes", true);
            enterGridSizesFrame.setLocation(70, 30);
            Container theContentPane = enterGridSizesFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup primary grid sizes
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel primaryGridSIzeLabel = new JLabel(Bundle.getMessage("PrimaryGridSize"));
            panel3.add(primaryGridSIzeLabel);
            panel3.add(primaryGridSizeField);
            primaryGridSizeField.setToolTipText(Bundle.getMessage("PrimaryGridSizeHint"));
            theContentPane.add(panel3);

            //setup side track width
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel secondaryGridSizeLabel = new JLabel(Bundle.getMessage("SecondaryGridSize"));
            panel2.add(secondaryGridSizeLabel);
            panel2.add(secondaryGridSizeField);
            secondaryGridSizeField.setToolTipText(Bundle.getMessage("SecondaryGridSizeHint"));
            theContentPane.add(panel2);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(gridSizesDone = new JButton(Bundle.getMessage("ButtonDone")));
            gridSizesDone.addActionListener((ActionEvent event) -> {
                gridSizesDonePressed(event);
            });
            gridSizesDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(gridSizesDone);
                rootPane.setDefaultButton(gridSizesDone);
            });

            //Cancel
            panel5.add(gridSizesCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            gridSizesCancel.addActionListener((ActionEvent event) -> {
                gridSizesCancelPressed(event);
            });
            gridSizesCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Track Widths
        primaryGridSizeField.setText(Integer.toString(layoutEditor.getGridSize()));
        secondaryGridSizeField.setText(Integer.toString(layoutEditor.getGridSize2nd()));
        enterGridSizesFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                gridSizesCancelPressed(null);
            }
        });
        enterGridSizesFrame.pack();
        enterGridSizesFrame.setVisible(true);
        gridSizesChange = false;
        enterGridSizesOpen = true;
    }

    private void gridSizesDonePressed(@Nonnull ActionEvent event) {
        String newGridSize = "";
        float siz = 0.0F;

        //get secondary grid size
        newGridSize = secondaryGridSizeField.getText().trim();
        try {
            siz = Float.parseFloat(newGridSize);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(enterGridSizesFrame, e);
            return;
        }

        if ((siz < 5.0) || (siz > 100.0)) {
            JOptionPane.showMessageDialog(enterGridSizesFrame,
                    MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", siz)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (!MathUtil.equals(layoutEditor.getGridSize2nd(), siz)) {
            layoutEditor.setGridSize2nd((int) siz);
            gridSizesChange = true;
        }

        //get mainline track width
        newGridSize = primaryGridSizeField.getText().trim();
        try {
            siz = Float.parseFloat(newGridSize);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(enterGridSizesFrame, e);
            return;
        }

        if ((siz < 5) || (siz > 100.0)) {
            JOptionPane.showMessageDialog(enterGridSizesFrame,
                    MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", siz)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            if (!MathUtil.equals(layoutEditor.getGridSize(), siz)) {
                layoutEditor.setGridSize((int) siz);
                gridSizesChange = true;
            }
            gridSizesCancelPressed(null);
        }
    }

    private void gridSizesCancelPressed(ActionEvent event) {
        enterGridSizesOpen = false;
        enterGridSizesFrame.setVisible(false);
        enterGridSizesFrame.dispose();
        enterGridSizesFrame = null;

        if (gridSizesChange) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    /*=======================================*\
    |* Dialog box to enter new reporter info *|
    \*=======================================*/
    //operational variables for enter reporter pane
    private transient JmriJFrame enterReporterFrame = null;
    private boolean reporterOpen = false;
    private transient JTextField xPositionField = new JTextField(6);
    private transient JTextField yPositionField = new JTextField(6);
    private transient JTextField reporterNameField = new JTextField(6);
    private transient JButton reporterDone;
    private transient JButton reporterCancel;

    //display dialog for entering Reporters
    @InvokeOnGuiThread
    protected void enterReporter(int defaultX, int defaultY) {
        if (reporterOpen) {
            enterReporterFrame.setVisible(true);
            return;
        }

        //Initialize if needed
        if (enterReporterFrame == null) {
            enterReporterFrame = new JmriJFrame(Bundle.getMessage("AddReporter"));

//enterReporterFrame.addHelpMenu("package.jmri.jmrit.display.AddReporterLabel", true);
            enterReporterFrame.setLocation(70, 30);
            Container theContentPane = enterReporterFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup reporter entry
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel reporterLabel = new JLabel(Bundle.getMessage("ReporterName"));
            panel2.add(reporterLabel);
            panel2.add(reporterNameField);
            reporterNameField.setToolTipText(Bundle.getMessage("ReporterNameHint"));
            theContentPane.add(panel2);

            //setup coordinates entry
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel xCoordLabel = new JLabel(Bundle.getMessage("ReporterLocationX"));
            panel3.add(xCoordLabel);
            panel3.add(xPositionField);
            xPositionField.setToolTipText(Bundle.getMessage("ReporterLocationXHint"));
            JLabel yCoordLabel = new JLabel(Bundle.getMessage("ReporterLocationY"));
            panel3.add(yCoordLabel);
            panel3.add(yPositionField);
            yPositionField.setToolTipText(Bundle.getMessage("ReporterLocationYHint"));
            theContentPane.add(panel3);

            //set up Add and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(reporterDone = new JButton(Bundle.getMessage("AddNewLabel")));
            reporterDone.addActionListener((ActionEvent event) -> {
                reporterDonePressed(event);
            });
            reporterDone.setToolTipText(Bundle.getMessage("ReporterDoneHint"));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(reporterDone);
                rootPane.setDefaultButton(reporterDone);
            });

            //Cancel
            panel5.add(reporterCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            reporterCancel.addActionListener((ActionEvent event) -> {
                reporterCancelPressed();
            });
            reporterCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Reporter Icon
        xPositionField.setText(Integer.toString(defaultX));
        yPositionField.setText(Integer.toString(defaultY));
        enterReporterFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                reporterCancelPressed();
            }
        });
        enterReporterFrame.pack();
        enterReporterFrame.setVisible(true);
        reporterOpen = true;
    }

    private void reporterDonePressed(@Nonnull ActionEvent event) {
        //get x coordinate
        String newX = "";
        int xx = 0;

        newX = xPositionField.getText().trim();
        try {
            xx = Integer.parseInt(newX);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(enterReporterFrame, e);
            return;
        }

        if ((xx <= 0) || (xx > layoutEditor.getLayoutWidth())) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", xx)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        // get y coordinate
        String newY = "";
        int yy = 0;
        newY = yPositionField.getText().trim();
        try {
            yy = Integer.parseInt(newY);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(enterReporterFrame, e);
            return;
        }

        if ((yy <= 0) || (yy > layoutEditor.getLayoutHeight())) {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    MessageFormat.format(Bundle.getMessage("Error2a"),
                            new Object[]{String.format(" %s ", yy)}),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        // get reporter name
        Reporter reporter = null;
        String rName = reporterNameField.getText();

        if (InstanceManager.getNullableDefault(ReporterManager.class) != null) {
            try {
                reporter = InstanceManager.getDefault(ReporterManager.class).provideReporter(rName);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(enterReporterFrame,
                        MessageFormat.format(Bundle.getMessage("Error18"),
                                new Object[]{rName}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            JOptionPane.showMessageDialog(enterReporterFrame,
                    Bundle.getMessage("Error17"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        //add the reporter icon
        layoutEditor.addReporter(reporter, xx, yy);

        reporterCancelPressed();
    }

    private void reporterCancelPressed() {
        reporterOpen = false;
        enterReporterFrame.setVisible(false);
        enterReporterFrame.dispose();
        enterReporterFrame = null;
        layoutEditor.redrawPanel();
    }

    /*===============================*\
    |*  Dialog box to enter scale /  *|
    |*  translate track diagram info *|
    \*===============================*/
    //operational variables for scale/translate track diagram pane
    private transient JmriJFrame scaleTrackDiagramFrame = null;
    private boolean scaleTrackDiagramOpen = false;
    private transient JTextField xFactorField = new JTextField(6);
    private transient JTextField yFactorField = new JTextField(6);
    private transient JTextField xTranslateField = new JTextField(6);
    private transient JTextField yTranslateField = new JTextField(6);
    private transient JButton scaleTrackDiagramDone;
    private transient JButton scaleTrackDiagramCancel;

    //display dialog for scaling the track diagram
    @InvokeOnGuiThread
    protected void scaleTrackDiagram() {
        if (scaleTrackDiagramOpen) {
            scaleTrackDiagramFrame.setVisible(true);
            return;
        }

        // Initialize if needed
        if (scaleTrackDiagramFrame == null) {
            scaleTrackDiagramFrame = new JmriJFrame(Bundle.getMessage("ScaleTrackDiagram"));
            scaleTrackDiagramFrame.addHelpMenu("package.jmri.jmrit.display.ScaleTrackDiagram", true);
            scaleTrackDiagramFrame.setLocation(70, 30);
            Container theContentPane = scaleTrackDiagramFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            // setup x translate
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            JLabel xTranslateLabel = new JLabel(Bundle.getMessage("XTranslateLabel"));
            panel31.add(xTranslateLabel);
            panel31.add(xTranslateField);
            xTranslateField.setToolTipText(Bundle.getMessage("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yTranslateLabel = new JLabel(Bundle.getMessage("YTranslateLabel"));
            panel32.add(yTranslateLabel);
            panel32.add(yTranslateField);
            yTranslateField.setToolTipText(Bundle.getMessage("YTranslateHint"));
            theContentPane.add(panel32);

            //setup information message 1
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            JLabel message1Label = new JLabel(Bundle.getMessage("Message1Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);

            //setup x factor
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
            JLabel xFactorLabel = new JLabel(Bundle.getMessage("XFactorLabel"));
            panel21.add(xFactorLabel);
            panel21.add(xFactorField);
            xFactorField.setToolTipText(Bundle.getMessage("FactorHint"));
            theContentPane.add(panel21);

            //setup y factor
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
            JLabel yFactorLabel = new JLabel(Bundle.getMessage("YFactorLabel"));
            panel22.add(yFactorLabel);
            panel22.add(yFactorField);
            yFactorField.setToolTipText(Bundle.getMessage("FactorHint"));
            theContentPane.add(panel22);

            //setup information message 2
            JPanel panel23 = new JPanel();
            panel23.setLayout(new FlowLayout());
            JLabel message2Label = new JLabel(Bundle.getMessage("Message2Label"));
            panel23.add(message2Label);
            theContentPane.add(panel23);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(scaleTrackDiagramDone = new JButton(Bundle.getMessage("ScaleTranslate")));
            scaleTrackDiagramDone.addActionListener((ActionEvent event) -> {
                scaleTrackDiagramDonePressed(event);
            });
            scaleTrackDiagramDone.setToolTipText(Bundle.getMessage("ScaleTranslateHint"));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(scaleTrackDiagramDone);
                rootPane.setDefaultButton(scaleTrackDiagramDone);
            });

            panel5.add(scaleTrackDiagramCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            scaleTrackDiagramCancel.addActionListener((ActionEvent event) -> {
                scaleTrackDiagramCancelPressed(event);
            });
            scaleTrackDiagramCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        // Set up for Entry of Scale and Translation
        xFactorField.setText("1.0");
        yFactorField.setText("1.0");
        xTranslateField.setText("0");
        yTranslateField.setText("0");
        scaleTrackDiagramFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                scaleTrackDiagramCancelPressed(null);
            }
        });
        scaleTrackDiagramFrame.pack();
        scaleTrackDiagramFrame.setVisible(true);
        scaleTrackDiagramOpen = true;
    }

    private void scaleTrackDiagramDonePressed(@Nonnull ActionEvent event) {
        boolean changeFlag = false;
        boolean translateError = false;
        float xTranslation, yTranslation, xFactor, yFactor;

        // get x translation
        String newText = xTranslateField.getText().trim();
        try {
            xTranslation = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(scaleTrackDiagramFrame, e);
            return;
        }

        // get y translation
        newText = yTranslateField.getText().trim();
        try {
            yTranslation = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(scaleTrackDiagramFrame, e);
            return;
        }

        // get x factor
        newText = xFactorField.getText().trim();
        try {
            xFactor = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(scaleTrackDiagramFrame, e);
            return;
        }

        // get y factor
        newText = yFactorField.getText().trim();
        try {
            yFactor = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(scaleTrackDiagramFrame, e);
            return;
        }

        // here when all numbers read in successfully - check for translation
        if ((xTranslation != 0.0F) || (yTranslation != 0.0F)) {
            //apply translation
            if (layoutEditor.translateTrack(xTranslation, yTranslation)) {
                changeFlag = true;
            } else {
                log.error("Error translating track diagram");
                translateError = true;
            }
        }

        if (!translateError && ((xFactor != 1.0) || (yFactor != 1.0))) {
            //apply scale change
            if (layoutEditor.scaleTrack(xFactor, yFactor)) {
                changeFlag = true;
            } else {
                log.error("Error scaling track diagram");
            }
        }
        layoutEditor.clearSelectionGroups();

        scaleTrackDiagramCancelPressed(null);

        if (changeFlag) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    private void scaleTrackDiagramCancelPressed(ActionEvent event) {
        scaleTrackDiagramOpen = false;
        scaleTrackDiagramFrame.setVisible(false);
        scaleTrackDiagramFrame.dispose();
        scaleTrackDiagramFrame = null;
    }

    /*=========================================*\
    |* Dialog box to enter move selection info *|
    \*=========================================*/
    //operational variables for move selection pane
    private transient JmriJFrame moveSelectionFrame = null;
    private boolean moveSelectionOpen = false;
    private transient JTextField xMoveField = new JTextField(6);
    private transient JTextField yMoveField = new JTextField(6);
    private transient JButton moveSelectionDone;
    private transient JButton moveSelectionCancel;

    //display dialog for translation a selection
    @InvokeOnGuiThread
    protected void moveSelection() {
        if (moveSelectionOpen) {
            moveSelectionFrame.setVisible(true);
            return;
        }

        //Initialize if needed
        if (moveSelectionFrame == null) {
            moveSelectionFrame = new JmriJFrame(Bundle.getMessage("TranslateSelection"));
            moveSelectionFrame.addHelpMenu("package.jmri.jmrit.display.TranslateSelection", true);
            moveSelectionFrame.setLocation(70, 30);
            Container theContentPane = moveSelectionFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.PAGE_AXIS));

            //setup x translate
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            JLabel xMoveLabel = new JLabel(Bundle.getMessage("XTranslateLabel"));
            panel31.add(xMoveLabel);
            panel31.add(xMoveField);
            xMoveField.setToolTipText(Bundle.getMessage("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yMoveLabel = new JLabel(Bundle.getMessage("YTranslateLabel"));
            panel32.add(yMoveLabel);
            panel32.add(yMoveField);
            yMoveField.setToolTipText(Bundle.getMessage("YTranslateHint"));
            theContentPane.add(panel32);

            //setup information message
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            JLabel message1Label = new JLabel(Bundle.getMessage("Message3Label"));
            panel33.add(message1Label);
            theContentPane.add(panel33);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(moveSelectionDone = new JButton(Bundle.getMessage("MoveSelection")));
            moveSelectionDone.addActionListener((ActionEvent event) -> {
                moveSelectionDonePressed(event);
            });
            moveSelectionDone.setToolTipText(Bundle.getMessage("MoveSelectionHint"));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(moveSelectionDone);
                rootPane.setDefaultButton(moveSelectionDone);
            });

            panel5.add(moveSelectionCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            moveSelectionCancel.addActionListener((ActionEvent event) -> {
                moveSelectionCancelPressed();
            });
            moveSelectionCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);
        }

        //Set up for Entry of Translation
        xMoveField.setText("0");
        yMoveField.setText("0");
        moveSelectionFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                moveSelectionCancelPressed();
            }
        });
        moveSelectionFrame.pack();
        moveSelectionFrame.setVisible(true);
        moveSelectionOpen = true;
    }

    private void moveSelectionDonePressed(@Nonnull ActionEvent event) {
        String newText = "";
        float xTranslation = 0.0F;
        float yTranslation = 0.0F;

        //get x translation
        newText = xMoveField.getText().trim();
        try {
            xTranslation = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(moveSelectionFrame, e);
            return;
        }

        //get y translation
        newText = yMoveField.getText().trim();
        try {
            yTranslation = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            showEntryErrorDialog(moveSelectionFrame, e);
            return;
        }

        layoutEditor.translate(xTranslation, yTranslation);

        moveSelectionCancelPressed();
    }

    private void moveSelectionCancelPressed() {
        moveSelectionOpen = false;
        moveSelectionFrame.setVisible(false);
        moveSelectionFrame.dispose();
        moveSelectionFrame = null;
    }

    /**
     * showEntryErrorDialog(Component parentComponent, NumberFormatException e)
     *
     * @param parentComponent determines the <code>Frame</code> in which the
     *                        dialog is displayed; if <code>null</code>, or if
     *                        the <code>parentComponent</code> has no
     *                        <code>Frame</code>, a default <code>Frame</code>
     *                        is used
     * @param e               Exception thrown to indicate that the application
     *                        has attempted to convert a string to one of the
     *                        numeric types, but that the string does not have
     *                        the appropriate format.
     */
    private void showEntryErrorDialog(Component parentComponent, NumberFormatException e) {
        JOptionPane.showMessageDialog(parentComponent,
                String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                        e, Bundle.getMessage("TryAgain")),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    private final static Logger log = LoggerFactory.getLogger(
            LayoutEditorDialogs.class);
}
