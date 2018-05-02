package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * A very specific dialog for editing the properties of a ThrottleFrame object.
 *
 * @author Original Unknown
 * @author Ken Cameron, copyright 2008
 */
public class ThrottleFramePropertyEditor extends JDialog {

    private ThrottleWindow frame;

    private JTextField titleField;

    private JList<String> titleType;

    private JCheckBox borderOff;

    private String[] titleTextTypes = {"address", "text", "textAddress", "addressText", "rosterID"};
    private String[] titleTextTypeNames = {
        Bundle.getMessage("SelectTitleTypeADDRESS"),
        Bundle.getMessage("SelectTitleTypeTEXT"),
        Bundle.getMessage("SelectTitleTypeTEXTADDRESS"),
        Bundle.getMessage("SelectTitleTypeADDRESSTEXT"),
        Bundle.getMessage("SelectTitleTypeROSTERID")
    };

    /*
     * Constructor
     */
    public ThrottleFramePropertyEditor(ThrottleWindow w){
        setThrottleFrame(w);
        setLocation(w.getLocationOnScreen());
        setLocationRelativeTo(w);
    }

    /**
     * Create, initialize, and place the GUI objects.
     */
    private void initGUI() {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle(Bundle.getMessage("EditThrottleFrameTitle"));
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());

        JPanel propertyPanel = new JPanel();
        propertyPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 2, 2, 2);
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        titleField = new JTextField();
        titleField.setColumns(24);
        titleField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                titleFieldChanged();
            }
        });

        propertyPanel.add(new JLabel(Bundle.getMessage("FrameTitlePrompt")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx++;
        propertyPanel.add(titleField, constraints);

        titleType = new JList<String>(titleTextTypeNames);
        titleType.setVisibleRowCount(titleTextTypeNames.length);
        titleType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < titleTextTypes.length; i++) {
            if (titleTextTypes[i].compareTo(frame.getTitleTextType()) == 0) {
                titleType.setSelectedIndex(i);
                break;
            }
        }
        constraints.gridy++;
        constraints.gridx = 0;
        propertyPanel.add(new JLabel(Bundle.getMessage("SelectTitleTypePrompt")), constraints);
        constraints.gridx++;
        propertyPanel.add(titleType, constraints);

        // add a checkbox for borders off, but only if that's actually possible.
        // this code uses details of internal UI code
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) frame.getCurrentThrottleFrame().getControlPanel().getUI()).getNorthPane() != null) {
            borderOff = new JCheckBox(Bundle.getMessage("FrameBorderOffTitle"), false);
            constraints.gridy++;
            constraints.gridx = 0;
            propertyPanel.add(new JLabel(Bundle.getMessage("FrameDecorationsTitle")), constraints);
            constraints.gridx++;
            propertyPanel.add(borderOff, constraints);
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 4, 4));

        JButton saveButton = new JButton(Bundle.getMessage("ButtonOK"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProperties();
            }
        });

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finishEdit();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(propertyPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Set the ThrottleFrame used here. Does some initialization of the Frame.
     */
    private void setThrottleFrame(ThrottleWindow f) {
        this.frame = f;
        initGUI();
        pack();
        titleField.setText(frame.getTitleText());
        titleField.selectAll();

        if (((javax.swing.plaf.basic.BasicInternalFrameUI) frame.getCurrentThrottleFrame().getControlPanel().getUI()).getNorthPane() != null) {
            Dimension bSize = ((javax.swing.plaf.basic.BasicInternalFrameUI) frame.getCurrentThrottleFrame().getControlPanel().getUI()).getNorthPane().getPreferredSize();
            if (bSize.height == 0) {
                borderOff.setSelected(true);
            } else {
                borderOff.setSelected(false);
            }
        }
    }

    /**
     * TItle field has been changed. If it has text, make sure that's displayed.
     */
    protected void titleFieldChanged() {
        if (titleField.getText().equals("")) {
            return;
        }
        if (titleType.getSelectedValue().equals(Bundle.getMessage("SelectTitleTypeADDRESS"))) {
            titleType.setSelectedValue(Bundle.getMessage("SelectTitleTypeTEXT"), true);
        }
    }

    /**
     * Save the user-modified properties back to the ThrottleFrame.
     */
    private void saveProperties() {
        if (isDataValid()) {
            int bSize = Integer.parseInt(Bundle.getMessage("FrameSize"));
            JInternalFrame myFrame;
            frame.setTitleText(titleField.getText());
            frame.setTitleTextType(titleTextTypes[titleType.getSelectedIndex()]);
            frame.getCurrentThrottleFrame().setFrameTitle();

            if (((javax.swing.plaf.basic.BasicInternalFrameUI) frame.getCurrentThrottleFrame().getControlPanel().getUI()).getNorthPane() != null) {
                if (borderOff.isSelected()) {
                    bSize = 0;
                }
                myFrame = frame.getCurrentThrottleFrame().getControlPanel();
                ((javax.swing.plaf.basic.BasicInternalFrameUI) myFrame.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
                if (myFrame.isVisible()) {
                    myFrame.setVisible(false);
                    myFrame.setVisible(true);
                }
                myFrame = frame.getCurrentThrottleFrame().getFunctionPanel();
                ((javax.swing.plaf.basic.BasicInternalFrameUI) myFrame.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
                if (myFrame.isVisible()) {
                    myFrame.setVisible(false);
                    myFrame.setVisible(true);
                }
                myFrame = frame.getCurrentThrottleFrame().getAddressPanel();
                ((javax.swing.plaf.basic.BasicInternalFrameUI) myFrame.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
                if (myFrame.isVisible()) {
                    myFrame.setVisible(false);
                    myFrame.setVisible(true);
                }
            }
            finishEdit();
        }
    }

    /**
     * Finish the editing process. Hide the dialog.
     */
    private void finishEdit() {
        this.setVisible(false);
    }

    /**
     * Verify the data on the dialog. If invalid, notify user of errors.
     */
    private boolean isDataValid() {
        StringBuffer errors = new StringBuffer();
        int errorNumber = 0;

        if (errorNumber > 0) {
            JOptionPane.showMessageDialog(this, errors,
                    Bundle.getMessage("ErrorOnPage"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

}
