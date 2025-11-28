package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InvokeOnGuiThread;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.JmriJOptionPane;

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
    private JmriJFrame enterGridSizesFrame = null;
    private boolean enterGridSizesOpen = false;
    private boolean gridSizesChange = false;
    private final JTextField primaryGridSizeField = new JTextField(6);
    private final JTextField secondaryGridSizeField = new JTextField(6);
    private final JComboBox<String> scaleComboBox = new JComboBox<>(new String[]{
            "10 mm per unit",
            "5 mm per unit", 
            "2 mm per unit",
            "1 unit per mm",
            "2 units per mm (default)",
            "5 units per mm"
    });
    private JButton gridSizesDone;
    private JButton gridSizesCancel;

    //display dialog for entering grid sizes
    @InvokeOnGuiThread
    public void enterGridSizes() {
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

            //setup scale selection
            JPanel scalePanel = new JPanel();
            scalePanel.setLayout(new FlowLayout());
            JLabel scaleLabel = new JLabel("Scale:");
            scalePanel.add(scaleLabel);
            scaleLabel.setLabelFor(scaleComboBox);
            scalePanel.add(scaleComboBox);
            scaleComboBox.setToolTipText("Select the scale for converting millimeters to layout units");
            theContentPane.add(scalePanel);

            //set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(gridSizesDone = new JButton(Bundle.getMessage("ButtonDone")));
            gridSizesDone.addActionListener(this::gridSizesDonePressed);
            gridSizesDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //Cancel
            panel5.add(gridSizesCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            gridSizesCancel.addActionListener(this::gridSizesCancelPressed);
            gridSizesCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel5);

            //make this button the default button (return or enter activates)
            JRootPane rootPane = SwingUtilities.getRootPane(gridSizesDone);
            rootPane.setDefaultButton(gridSizesDone);
        }

        //Set up for Entry of Track Widths
        primaryGridSizeField.setText(Integer.toString(layoutEditor.gContext.getGridSize()));
        secondaryGridSizeField.setText(Integer.toString(layoutEditor.gContext.getGridSize2nd()));
        
        //Set up current scale selection
        double currentUnitsPerMM = layoutEditor.getLayoutUnitsPerMM();
        setScaleComboSelection(currentUnitsPerMM);
        
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
            JmriJOptionPane.showMessageDialog(enterGridSizesFrame,
                    MessageFormat.format(Bundle.getMessage("Error2a"), String.format(" %s ", siz)),
                    Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);

            return;
        }

        if (!MathUtil.equals(layoutEditor.gContext.getGridSize2nd(), siz)) {
            layoutEditor.gContext.setGridSize2nd((int) siz);
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
            JmriJOptionPane.showMessageDialog(enterGridSizesFrame,
                    MessageFormat.format(Bundle.getMessage("Error2a"), String.format(" %s ", siz)),
                    Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
        } else {
            if (!MathUtil.equals(layoutEditor.gContext.getGridSize(), siz)) {
                layoutEditor.gContext.setGridSize((int) siz);
                gridSizesChange = true;
            }
            
            // Handle scale changes
            double newUnitsPerMM = getScaleFromComboSelection();
            if (!MathUtil.equals(layoutEditor.getLayoutUnitsPerMM(), newUnitsPerMM)) {
                layoutEditor.setLayoutUnitsPerMM((float) newUnitsPerMM);
                gridSizesChange = true;
            }
            
            gridSizesCancelPressed(null);
        }
    }
    
    /**
     * Set the scale combo box to match the current units per mm value
     */
    private void setScaleComboSelection(double unitsPerMM) {
        if (MathUtil.equals(unitsPerMM, 0.1)) {
            scaleComboBox.setSelectedIndex(0); // 10 mm per unit
        } else if (MathUtil.equals(unitsPerMM, 0.2)) {
            scaleComboBox.setSelectedIndex(1); // 5 mm per unit  
        } else if (MathUtil.equals(unitsPerMM, 0.5)) {
            scaleComboBox.setSelectedIndex(2); // 2 mm per unit
        } else if (MathUtil.equals(unitsPerMM, 1.0)) {
            scaleComboBox.setSelectedIndex(3); // 1 unit per mm
        } else if (MathUtil.equals(unitsPerMM, 2.0)) {
            scaleComboBox.setSelectedIndex(4); // 2 units per mm (default)
        } else if (MathUtil.equals(unitsPerMM, 5.0)) {
            scaleComboBox.setSelectedIndex(5); // 5 units per mm
        } else {
            scaleComboBox.setSelectedIndex(4); // default to 2 units per mm
        }
    }
    
    /**
     * Get the units per mm value from the current combo box selection
     */
    private double getScaleFromComboSelection() {
        int selectedIndex = scaleComboBox.getSelectedIndex();
        switch (selectedIndex) {
            case 0: return 0.1; // 10 mm per unit
            case 1: return 0.2; // 5 mm per unit
            case 2: return 0.5; // 2 mm per unit
            case 3: return 1.0; // 1 unit per mm
            case 4: return 2.0; // 2 units per mm (default)
            case 5: return 5.0; // 5 units per mm
            default: return 2.0; // default
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
        JmriJOptionPane.showMessageDialog(parentComponent,
                String.format("%s: %s %s", Bundle.getMessage("EntryError"),
                        e, Bundle.getMessage("TryAgain")),
                Bundle.getMessage("ErrorTitle"),
                JmriJOptionPane.ERROR_MESSAGE);
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnterGridSizesDialog.class);

}
