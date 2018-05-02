package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import jmri.util.JmriJFrame;

/**
 * Displays and allows user to modify the text display used in a turnout slip
 *
 * This is a modification of CoordinateEdit.java by Dan Boudreau for use with
 * LayoutEditor
 *
 * @author Dan Boudreau Copyright (C) 2007
 * @author Kevin Dickerson (SlipTurnoutTextEditor version);
 */
public class SlipTurnoutTextEdit extends JmriJFrame {

    SlipTurnoutIcon pl;  // layout positional label tracked by this frame
    static final String INIT = null;
    String oldLWUE = INIT;
    String oldUWLE = INIT;
    String oldLWLE = INIT;
    String oldUWUE = INIT;

    // member declarations
    javax.swing.JLabel lableName = new javax.swing.JLabel();
    javax.swing.JLabel nameText = new javax.swing.JLabel();
    javax.swing.JLabel textLWUE = new javax.swing.JLabel();
    javax.swing.JLabel textUWLE = new javax.swing.JLabel();
    javax.swing.JLabel textLWLE = new javax.swing.JLabel();
    javax.swing.JLabel textUWUE = new javax.swing.JLabel();

    // buttons
    javax.swing.JButton okButton = new javax.swing.JButton();
    javax.swing.JButton cancelButton = new javax.swing.JButton();

    // text field
    javax.swing.JTextField UWLETextField = new javax.swing.JTextField(15);
    javax.swing.JTextField LWUETextField = new javax.swing.JTextField(15);
    javax.swing.JTextField LWLETextField = new javax.swing.JTextField(15);
    javax.swing.JTextField UWUETextField = new javax.swing.JTextField(15);

    // for padding out panel
    javax.swing.JLabel space1 = new javax.swing.JLabel();
    javax.swing.JLabel space2 = new javax.swing.JLabel();

    public SlipTurnoutTextEdit() {
        super();
    }

    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
        super.windowClosed(e);
    }

    public void initComponents(SlipTurnoutIcon l, String name) {
        pl = l;

        // the following code sets the frame's initial state
        lableName.setText(Bundle.getMessage("Name") + ": ");
        lableName.setVisible(true);

        nameText.setText(name);
        nameText.setVisible(true);

        textLWUE.setText(Bundle.getMessage("UpperWestToLowerEast") + " = " + pl.getUWLEText());
        textLWUE.setVisible(true);
        textUWLE.setText(Bundle.getMessage("LowerWestToUpperEast") + " = " + pl.getLWUEText());
        textUWLE.setVisible(true);
        textLWLE.setText(Bundle.getMessage("LowerWestToLowerEast") + " = " + pl.getLWLEText());
        textUWUE.setText(Bundle.getMessage("UpperWestToUpperEast") + " = " + pl.getUWUEText());

        UWLETextField.setText(pl.getUWLEText());
        UWLETextField.setToolTipText(Bundle.getMessage("EnterUWLEToolTip"));
        UWLETextField.setMaximumSize(new Dimension(
                UWLETextField.getMaximumSize().width, UWLETextField
                .getPreferredSize().height));

        LWUETextField.setText(pl.getLWUEText());
        LWUETextField.setToolTipText(Bundle.getMessage("EnterLWUEToolTip"));
        LWUETextField.setMaximumSize(new Dimension(
                LWUETextField.getMaximumSize().width, LWUETextField
                .getPreferredSize().height));

        LWLETextField.setText(pl.getLWLEText());
        LWLETextField.setToolTipText(Bundle.getMessage("EnterLWLEToolTip"));
        LWLETextField.setMaximumSize(new Dimension(
                LWLETextField.getMaximumSize().width, LWLETextField
                .getPreferredSize().height));

        UWUETextField.setText(pl.getUWUEText());
        UWUETextField.setToolTipText(Bundle.getMessage("EnterUWUEToolTip"));
        UWUETextField.setMaximumSize(new Dimension(
                UWUETextField.getMaximumSize().width, UWUETextField
                .getPreferredSize().height));

        if (l.getTurnoutType() == SlipTurnoutIcon.DOUBLESLIP) {
            textLWLE.setVisible(true);
            textUWUE.setVisible(true);
        } else if (l.getTurnoutType() == SlipTurnoutIcon.THREEWAY) {
            textLWLE.setVisible(true);
            LWLETextField.setVisible(true);
            textUWUE.setVisible(false);
            UWUETextField.setVisible(false);
            textLWUE.setText(Bundle.getMessage("Upper") + " = " + pl.getLWUEText());
            textUWLE.setText(Bundle.getMessage("Middle") + " = " + pl.getUWLEText());
            textLWLE.setText(Bundle.getMessage("Lower") + " = " + pl.getLWLEText());
            LWLETextField.setToolTipText(Bundle.getMessage("EnterLowerToolTip"));
            UWLETextField.setToolTipText(Bundle.getMessage("EnterMiddleToolTip"));
            LWUETextField.setToolTipText(Bundle.getMessage("EnterUpperToolTip"));
        } else {
            if (l.getSingleSlipRoute()) {
                textUWUE.setVisible(true);
                UWUETextField.setVisible(true);
                textLWLE.setVisible(false);
                LWLETextField.setVisible(false);
            } else {
                textUWUE.setVisible(false);
                UWUETextField.setVisible(false);
                textLWLE.setVisible(true);
                LWLETextField.setVisible(true);
            }
        }

        okButton.setText(Bundle.getMessage("ButtonOK"));
        okButton.setVisible(true);
        okButton.setToolTipText(Bundle.getMessage("SetButtonToolTip"));

        cancelButton.setText(Bundle.getMessage("ButtonCancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(Bundle.getMessage("CancelButtonToolTip"));

        setTitle(Bundle.getMessage("SetTurnoutText"));
        getContentPane().setLayout(new GridBagLayout());

        setSize(250, 220);

        addItem(lableName, 0, 0);
        addItem(nameText, 1, 0);
        addItem(textLWUE, 0, 1);
        addItem(LWUETextField, 1, 1);

        addItem(textUWLE, 0, 2);
        addItem(UWLETextField, 1, 2);
        addItem(textLWLE, 0, 3);
        addItem(LWLETextField, 1, 3);
        addItem(textUWUE, 0, 4);
        addItem(UWUETextField, 1, 4);
        addItem(cancelButton, 0, 5);
        addItem(okButton, 1, 5);

        // setup buttons
        addButtonAction(okButton);
        addButtonAction(cancelButton);
        pack();

    }

    private void addItem(JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        getContentPane().add(c, gc);
    }

    private void addButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonActionPerformed(e);
            }
        });
    }

    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {

        if (ae.getSource() == okButton) {
            // save current values in case user cancels
            if (oldLWUE == INIT) {
                oldLWUE = pl.getLWUEText();
                oldUWLE = pl.getUWLEText();
                oldLWLE = pl.getLWLEText();
                oldUWUE = pl.getUWUEText();
            }
            pl.setUWLEText(UWLETextField.getText());
            pl.setLWUEText(LWUETextField.getText());
            pl.setLWLEText(LWLETextField.getText());
            pl.setUWUEText(UWUETextField.getText());
            textLWUE.setText(Bundle.getMessage("LowerWestToUpperEast") + " = " + pl.getLWUEText());
            textUWLE.setText(Bundle.getMessage("UpperWestToLowerEast") + " = " + pl.getUWLEText());
            textLWLE.setText(Bundle.getMessage("LowerWestToLowerEast") + " = " + pl.getLWLEText());
            textUWUE.setText(Bundle.getMessage("UpperWestToUpperEast") + " = " + pl.getUWUEText());
            if (pl.getTurnoutType() == SlipTurnoutIcon.THREEWAY) {
                textLWUE.setText(Bundle.getMessage("Upper") + " = " + pl.getLWUEText());
                textUWLE.setText(Bundle.getMessage("Middle") + " = " + pl.getUWLEText());
                textLWLE.setText(Bundle.getMessage("Lower") + " = " + pl.getLWLEText());
                /*textLWUE.setText(Bundle.getMessage("Upper") + " = " + pl.getLWLEText());
                 textUWLE.setText(Bundle.getMessage("Middle") + " = " + pl.getLWUEText());
                 textLWLE.setText(Bundle.getMessage("Lower") + " = " + pl.getLWLEText());*/
            }
        }
        if (ae.getSource() == cancelButton) {
            if (oldLWUE != INIT) {
                pl.setUWLEText(oldUWLE);
                pl.setLWUEText(oldLWUE);
                pl.setLWLEText(oldLWLE);
                pl.setUWUEText(oldUWUE);
            }
            dispose();
        }
    }
}
