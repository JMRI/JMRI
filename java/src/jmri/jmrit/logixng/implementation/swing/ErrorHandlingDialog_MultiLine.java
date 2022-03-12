package jmri.jmrit.logixng.implementation.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.LogixNG_Manager;

/**
 *
 * @author Daniel Bergqvist 2021
 */
public class ErrorHandlingDialog_MultiLine {
    
    private Base _item;
    private JDialog _errorDialog;
    
    private final JCheckBox _disableConditionalNGCheckBox =
            new JCheckBox(Bundle.getMessage("ErrorHandlingDialog_DisableConditionalNG"));   // NOI18N
    
    private final JCheckBox _disableLogixNGCheckBox =
            new JCheckBox(Bundle.getMessage("ErrorHandlingDialog_DisableLogixNG"));   // NOI18N
    
    private final JCheckBox _stopAllLogixNGCheckBox =
            new JCheckBox(Bundle.getMessage("ErrorHandlingDialog_StopAllLogixNGs"));   // NOI18N
    
    private boolean _abortExecution = false;
    
    
    public boolean showDialog(Base item, String errorMessage, List<String> errorMessageList) {
        
        _item = item;
        
        _errorDialog = new JDialog(
                (JDialog)null,
                Bundle.getMessage("ErrorHandlingDialog_Title"),
                true);
        
        Container contentPanel = _errorDialog.getContentPane();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        contentPanel.add(new JLabel(Bundle.getMessage(
                "ErrorHandlingDialog_Name", item.getShortDescription())));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(new JLabel(errorMessage));
        contentPanel.add(Box.createVerticalStrut(10));
        String errorMsg = String.join("<br>", errorMessageList);
        contentPanel.add(new JLabel("<html>"+errorMsg+"</html>"));
        contentPanel.add(Box.createVerticalStrut(10));
        
        contentPanel.add(_disableConditionalNGCheckBox);
        _disableConditionalNGCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(_disableLogixNGCheckBox);
        _disableLogixNGCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(_stopAllLogixNGCheckBox);
        _stopAllLogixNGCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        
        contentPanel.add(panel3);
        
        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        
        // Abort
        JButton abortButton = new JButton(Bundle.getMessage("ErrorHandlingDialog_Abort"));  // NOI18N
        panel5.add(abortButton);
        abortButton.addActionListener((ActionEvent e) -> {
            abortPressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
//        abortButton.setToolTipText("CancelLogixButtonHint");      // NOI18N
        
        // Continue
        JButton continueButton = new JButton(Bundle.getMessage("ErrorHandlingDialog_Continue"));    // NOI18N
        panel5.add(continueButton);
        continueButton.addActionListener((ActionEvent e) -> {
            continuePressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
//        continueButton.setToolTipText("CancelLogixButtonHint");      // NOI18N
        
        _errorDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                continuePressed(null);
            }
        });
/*        
        _create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
        panel5.add(_create);
        _create.addActionListener((ActionEvent e) -> {
            cancelAddPressed(null);
            
            SwingConfiguratorInterface swingConfiguratorInterface =
                    _swingConfiguratorComboBox.getItemAt(_swingConfiguratorComboBox.getSelectedIndex());
//            System.err.format("swingConfiguratorInterface: %s%n", swingConfiguratorInterface.getClass().getName());
            createAddFrame(femaleSocket, path, swingConfiguratorInterface);
        });
*/        
        contentPanel.add(panel5);
        
//        addLogixNGFrame.setLocationRelativeTo(component);
        _errorDialog.setLocationRelativeTo(null);
        _errorDialog.pack();
        _errorDialog.setVisible(true);
        
        return _abortExecution;
    }
    
    private void handleCheckBoxes() {
        if (_disableConditionalNGCheckBox.isSelected()) {
            _item.getConditionalNG().setEnabled(false);
        }
        if (_disableLogixNGCheckBox.isSelected()) {
            _item.getLogixNG().setEnabled(false);
        }
        if (_stopAllLogixNGCheckBox.isSelected()) {
            InstanceManager.getDefault(LogixNG_Manager.class).deActivateAllLogixNGs();
        }
    }
    
    private void abortPressed(ActionEvent e) {
        _errorDialog.setVisible(false);
        _errorDialog.dispose();
        _errorDialog = null;
        _abortExecution = true;
        handleCheckBoxes();
    }
    
    private void continuePressed(ActionEvent e) {
        _errorDialog.setVisible(false);
        _errorDialog.dispose();
        _errorDialog = null;
        handleCheckBoxes();
    }
    
}
