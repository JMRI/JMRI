// LayoutSensorTextEdit.java
package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays and allows user to modify the text display used in a sensor
 *
 * This is a modification of CoordinateEdit.java by Dan Boudreau for use with
 * LayoutEditor
 *
 * @author Dan Boudreau Copyright (C) 2007
 * @author Kevin Dickerson (SensorTextEditor version);
 * @version $Revision$
 */
public class SensorTextEdit extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -5713312554502509613L;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    SensorIcon pl; 	// layout positional label tracked by this frame
    static final String INIT = null;
    String oldActive = INIT;
    String oldInactive = INIT;
    String oldIncon = INIT;
    String oldUnknown = INIT;

    // member declarations
    javax.swing.JLabel lableName = new javax.swing.JLabel();
    javax.swing.JLabel nameText = new javax.swing.JLabel();
    javax.swing.JLabel textInact = new javax.swing.JLabel();
    javax.swing.JLabel textAct = new javax.swing.JLabel();
    javax.swing.JLabel textIncon = new javax.swing.JLabel();
    javax.swing.JLabel textUnknown = new javax.swing.JLabel();

    // buttons
    javax.swing.JButton okButton = new javax.swing.JButton();
    javax.swing.JButton cancelButton = new javax.swing.JButton();

    // text field
    javax.swing.JTextField inactTextField = new javax.swing.JTextField(8);
    javax.swing.JTextField actTextField = new javax.swing.JTextField(8);
    javax.swing.JTextField inconTextField = new javax.swing.JTextField(8);
    javax.swing.JTextField unknownTextField = new javax.swing.JTextField(8);

    // for padding out panel
    javax.swing.JLabel space1 = new javax.swing.JLabel();
    javax.swing.JLabel space2 = new javax.swing.JLabel();

    public SensorTextEdit() {
        super();
    }

    public void windowClosed(java.awt.event.WindowEvent e) {
        super.windowClosed(e);
    }

    public void initComponents(SensorIcon l, String name) throws Exception {
        pl = l;

        // the following code sets the frame's initial state
        lableName.setText(rb.getString("Name") + ": ");
        lableName.setVisible(true);

        nameText.setText(name);
        nameText.setVisible(true);

        textInact.setText("Inactive= " + pl.getInactiveText());
        textInact.setVisible(true);
        textAct.setText("Active= " + pl.getActiveText());
        textAct.setVisible(true);
        textIncon.setText("Inconsistent= " + pl.getInconsistentText());
        textIncon.setVisible(true);
        textUnknown.setText("Unknown= " + pl.getUnknownText());
        textUnknown.setVisible(true);

        inactTextField.setText(pl.getInactiveText());
        inactTextField.setToolTipText(rb.getString("EnterInActiveToolTip"));
        inactTextField.setMaximumSize(new Dimension(
                inactTextField.getMaximumSize().width, inactTextField
                .getPreferredSize().height));

        actTextField.setText(pl.getActiveText());
        actTextField.setToolTipText(rb.getString("EnterActiveToolTip"));
        actTextField.setMaximumSize(new Dimension(
                actTextField.getMaximumSize().width, actTextField
                .getPreferredSize().height));

        inconTextField.setText(pl.getInconsistentText());
        inconTextField.setToolTipText(rb.getString("EnterInconToolTip"));
        inconTextField.setMaximumSize(new Dimension(
                inconTextField.getMaximumSize().width, inconTextField
                .getPreferredSize().height));

        unknownTextField.setText(pl.getUnknownText());
        unknownTextField.setToolTipText(rb.getString("EnterUnknownToolTip"));
        unknownTextField.setMaximumSize(new Dimension(
                unknownTextField.getMaximumSize().width, unknownTextField
                .getPreferredSize().height));

        okButton.setText(rb.getString("Set"));
        okButton.setVisible(true);
        okButton.setToolTipText(rb.getString("SetButtonToolTipSensor"));

        cancelButton.setText(rb.getString("Cancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(rb.getString("CancelButtonToolTipSensor"));

        setTitle(rb.getString("SetSensorText"));
        getContentPane().setLayout(new GridBagLayout());

        setSize(250, 220);

        addItem(lableName, 0, 0);
        addItem(nameText, 1, 0);
        addItem(textInact, 0, 1);
        addItem(inactTextField, 1, 1);
        addItem(textAct, 0, 2);
        addItem(actTextField, 1, 2);
        addItem(textIncon, 0, 3);
        addItem(inconTextField, 1, 3);
        addItem(textUnknown, 0, 4);
        addItem(unknownTextField, 1, 4);
        addItem(cancelButton, 0, 5);
        addItem(okButton, 1, 5);

        // setup buttons
        addButtonAction(okButton);
        addButtonAction(cancelButton);
        pack();

        /*if (!pl.isBackground()) {
         // Add listener so we know if the label moves
         pl.addMouseListener(ml);
         }*/
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
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonActionPerformed(e);
            }
        });
    }

    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {

        if (ae.getSource() == okButton) {
            // save current values in case user cancels
            if (oldActive == INIT) {
                oldActive = pl.getActiveText();
                oldInactive = pl.getInactiveText();
                oldIncon = pl.getInconsistentText();
                oldUnknown = pl.getUnknownText();
            }
            pl.setInactiveText(inactTextField.getText());
            pl.setActiveText(actTextField.getText());
            pl.setInconsistentText(inconTextField.getText());
            pl.setUnknownText(unknownTextField.getText());
            textInact.setText("Inactive= " + pl.getInactiveText());
            textAct.setText("Active= " + pl.getActiveText());
            textIncon.setText("Inconsistent= " + pl.getInconsistentText());
            textUnknown.setText("Unknown= " + pl.getUnknownText());
        }
        if (ae.getSource() == cancelButton) {
            if (oldActive != INIT) {
                pl.setInactiveText(oldInactive);
                pl.setActiveText(oldActive);
                pl.setInconsistentText(oldIncon);
                pl.setUnknownText(oldUnknown);
            }
            dispose();
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(SensorTextEdit.class.getName());
}
