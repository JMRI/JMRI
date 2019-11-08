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

/**
 * Implements the move selection dialog for the Layout Editor
 *
 * @author George Warner Copyright (c) 2019
 */
public class MoveSelectionDialog {

    // operational instance variables
    private LayoutEditor layoutEditor = null;

    // constructor method
    public MoveSelectionDialog(@Nonnull LayoutEditor thePanel) {
        layoutEditor = thePanel;
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
            xMoveLabel.setLabelFor(xMoveField);
            xMoveField.setName("XTranslateLabel");
            xMoveField.setToolTipText(Bundle.getMessage("XTranslateHint"));
            theContentPane.add(panel31);

            //setup y translate
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            JLabel yMoveLabel = new JLabel(Bundle.getMessage("YTranslateLabel"));
            panel32.add(yMoveLabel);
            yMoveLabel.setLabelFor(yMoveField);
            panel32.add(yMoveField);
            yMoveField.setName("YTranslateLabel");
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
            panel5.add(moveSelectionCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            moveSelectionCancel.addActionListener((ActionEvent event) -> {
                moveSelectionCancelPressed();
            });
            moveSelectionCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);

            //make this button the default button (return or enter activates)
            JRootPane rootPane = SwingUtilities.getRootPane(moveSelectionDone);
            rootPane.setDefaultButton(moveSelectionDone);
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

    //private final static Logger log = LoggerFactory.getLogger(
    //        MoveSelectionDialog.class);
}
