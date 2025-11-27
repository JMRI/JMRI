package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.*;

import jmri.InvokeOnGuiThread;
import jmri.NamedBean;
import jmri.util.swing.WrapLayout;

/**
 * Swing Action to display an Add/Edit dialong for a specific NamedBean
 *
 * @author Bob Jacobsen   Copyright (C) 2025
 */
 
public class PropertyAddEditAction extends AbstractAction {

    /**
     * Ctor captures a reference to the bean to add/edit properties in
     *
     * @param bean The NamedBean that will be editied
     */
    public PropertyAddEditAction(NamedBean bean) {
        thisBean = bean;
    }

    NamedBean thisBean;
    
    JComboBox<String> selector;
    JTextField valueField;
    
    @Override
    @InvokeOnGuiThread
    public void actionPerformed(ActionEvent evt) {
        // Launch a custom frame for editing
        var frame = new JFrame("Property Add/Edit: "+thisBean.getDisplayName());
        frame.setLayout(new WrapLayout());
        
        Vector<String> properties = new Vector<>(thisBean.getPropertyKeys());
        
        var pane1 = new JPanel();
        frame.add(pane1);
        pane1.add(new JLabel("Property Name: "));

        selector = new JComboBox<>(properties);
        selector.setEditable(true);
        selector.setPrototypeDisplayValue("Reserve a long property name");
        selector.addActionListener( (ActionEvent e1) -> selectionChanged());
        pane1.add(selector);
        
        var pane2 = new JPanel();
        frame.add(pane2);  
        pane2.add(new JLabel("Value: "));
        
        valueField = new JTextField();
        valueField.setColumns(10);
        pane2.add(valueField);
        
        var setButton = new JButton("Set");
        setButton.addActionListener( (ActionEvent e1) -> setButtonPressed());
        frame.add(setButton);
        
        frame.pack();
        frame.setVisible(true);
    }
    
    // Show the value corresponding to the selected property name
    void selectionChanged() {
        String property = (String)selector.getSelectedItem();    
        log.debug("Selection Changed to {}", property);
        valueField.setText((String)thisBean.getProperty(property));
    }
    
    // make the change
    void setButtonPressed() {
        // set the provided value for the selected property
        log.trace("Selected index {}", selector.getSelectedIndex());
        String property = (String)selector.getSelectedItem();
        log.debug("Set property {} to {}", property, valueField.getText());
        thisBean.setProperty(property, valueField.getText());
        
        // reload the selector box
        Vector<String> properties = new Vector<>(thisBean.getPropertyKeys());
        selector.setModel(new DefaultComboBoxModel<String>(properties)); 
        selector.setEditable(true);
        
        // and select the entry we just made
        selector.setSelectedItem(property);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyAddEditAction.class);
}
