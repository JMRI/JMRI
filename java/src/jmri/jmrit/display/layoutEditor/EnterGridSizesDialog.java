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
import jmri.InvokeOnGuiThread;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;

/**
 * Layout Editor Dialogs implements some dialogs for the Layout Editor
 *
 * @author George Warner Copyright (c) 2019
 */
public class EnterGridSizesDialog {

    // operational instance variables shared between dialogs
    private LayoutEditor layoutEditor = null;

    // constructor method
    public EnterGridSizesDialog(@Nonnull LayoutEditor thePanel) {
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
            JLabel primaryGridSizeLabel = new JLabel(Bundle.getMessage("PrimaryGridSize"));
            panel3.add(primaryGridSizeLabel);
            primaryGridSizeLabel.setLabelFor(primaryGridSizeField);
            panel3.add(primaryGridSizeField);
            primaryGridSizeField.setToolTipText(Bundle.getMessage("PrimaryGridSizeHint"));
            theContentPane.add(panel3);

            //setup side track width
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel secondaryGridSizeLabel = new JLabel(Bundle.getMessage("SecondaryGridSize"));
            panel2.add(secondaryGridSizeLabel);
            secondaryGridSizeLabel.setLabelFor(secondaryGridSizeField);
            panel2.add(secondaryGridSizeField);
            secondaryGridSizeField.setName("SecondaryGridSize");
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

            //Cancel
            panel5.add(gridSizesCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            gridSizesCancel.addActionListener((ActionEvent event) -> {
                gridSizesCancelPressed(event);
            });
            gridSizesCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);

            //make this button the default button (return or enter activates)
            JRootPane rootPane = SwingUtilities.getRootPane(gridSizesDone);
            rootPane.setDefaultButton(gridSizesDone);
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

//    private final static Logger log = LoggerFactory.getLogger(
//            EnterGridSizesDialog.class);
}
