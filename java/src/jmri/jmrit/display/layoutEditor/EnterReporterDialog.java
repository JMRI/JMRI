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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout Editor Dialogs implements some dialogs for the Layout Editor
 *
 * @author George Warner Copyright (c) 2019
 */
public class EnterReporterDialog {

    // operational instance variables shared between tools
    private LayoutEditor layoutEditor = null;

    // constructor method
    public EnterReporterDialog(@Nonnull LayoutEditor thePanel) {
        layoutEditor = thePanel;
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
            reporterLabel.setLabelFor(reporterNameField);
            panel2.add(reporterNameField);
            reporterNameField.setToolTipText(Bundle.getMessage("ReporterNameHint"));
            theContentPane.add(panel2);

            //setup coordinates entry
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());

            JLabel xCoordLabel = new JLabel(Bundle.getMessage("ReporterLocationX"));
            panel3.add(xCoordLabel);
            xCoordLabel.setLabelFor(xPositionField);
            panel3.add(xPositionField);
            xPositionField.setToolTipText(Bundle.getMessage("ReporterLocationXHint"));

            JLabel yCoordLabel = new JLabel(Bundle.getMessage("ReporterLocationY"));
            panel3.add(yCoordLabel);
            yCoordLabel.setLabelFor(yPositionField);
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

            //Cancel
            panel5.add(reporterCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            reporterCancel.addActionListener((ActionEvent event) -> {
                reporterCancelPressed();
            });
            reporterCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);

            //make this button the default button (return or enter activates)
            JRootPane rootPane = SwingUtilities.getRootPane(reporterDone);
            rootPane.setDefaultButton(reporterDone);
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
            log.error("invalid x: " + xx + ", LayoutWidth: " + layoutEditor.getLayoutWidth());
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
            log.error("invalid y: " + yy + ", LayoutWidth: " + layoutEditor.getLayoutHeight());
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
            EnterReporterDialog.class);
}
