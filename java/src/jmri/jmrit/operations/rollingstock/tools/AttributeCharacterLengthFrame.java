package jmri.jmrit.operations.rollingstock.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;

import jmri.jmrit.operations.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for editing attribute maximum length.
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class AttributeCharacterLengthFrame extends OperationsFrame {

    // major buttons
    public JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    JComboBox<Integer> comboBox;

    public AttributeCharacterLengthFrame() {
        initComponents();
    }

    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("ChangeCharLength"));
        getContentPane().setLayout(new GridBagLayout());

        comboBox = new JComboBox<Integer>();
        // allow user to select 4 to 20 characters
        for (int i = 4; i < 21; i++) {
            comboBox.addItem(i);
        }
        OperationsPanel.padComboBox(comboBox, 3);
        comboBox.setSelectedItem(Control.max_len_string_attibute);
        addItem(comboBox, 0, 0);
        addItem(saveButton, 1, 0);

        addButtonAction(saveButton);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight100));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        // the only button is save
        Control.setMaxCharLength((int) comboBox.getSelectedItem());
        OperationsXml.save();
    }

    //    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CarAttributeEditFrame.class);
}
