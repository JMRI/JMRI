package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import jmri.InvokeOnGuiThread;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout Editor Dialogs implements some dialogs for the Layout Editor
 *
 * @author George Warner Copyright (c) 2019
 */
public class ScaleTrackDiagramDialog {

    // operational instance variables shared between tools
    private LayoutEditor layoutEditor = null;

    // constructor method
    public ScaleTrackDiagramDialog(@Nonnull LayoutEditor thePanel) {
        layoutEditor = thePanel;
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
            xTranslateLabel.setLabelFor(xTranslateField);
            panel31.add(xTranslateField);
            xTranslateField.setToolTipText(Bundle.getMessage("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yTranslateLabel = new JLabel(Bundle.getMessage("YTranslateLabel"));
            panel32.add(yTranslateLabel);
            yTranslateLabel.setLabelFor(yTranslateField);
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
            xFactorLabel.setLabelFor(xFactorField);
            panel21.add(xFactorField);
            xFactorField.setToolTipText(Bundle.getMessage("FactorHint"));
            theContentPane.add(panel21);

            //setup y factor
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
            JLabel yFactorLabel = new JLabel(Bundle.getMessage("YFactorLabel"));
            panel22.add(yFactorLabel);
            yFactorLabel.setLabelFor(yFactorField);
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

            panel5.add(scaleTrackDiagramCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            scaleTrackDiagramCancel.addActionListener((ActionEvent event) -> {
                scaleTrackDiagramCancelPressed(event);
            });
            scaleTrackDiagramCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);

            //make this button the default button (return or enter activates)
            JRootPane rootPane = SwingUtilities.getRootPane(scaleTrackDiagramDone);
            rootPane.setDefaultButton(scaleTrackDiagramDone);
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
            ScaleTrackDiagramDialog.class);
}
