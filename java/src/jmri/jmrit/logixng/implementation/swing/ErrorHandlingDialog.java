package jmri.jmrit.logixng.implementation.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrit.logixng.Base;

/**
 *
 * @author Daniel Bergqvist 2020
 */
public class ErrorHandlingDialog {
    
    private JDialog _selectItemTypeDialog;
    
    private final JCheckBox _disableConditionalNGCheckBox =
            new JCheckBox(Bundle.getMessage("ErrorHandlingDialog_DisableConditionalNG") + ":");   // NOI18N
    
    private final JCheckBox _disableLogixNGCheckBox =
            new JCheckBox(Bundle.getMessage("ErrorHandlingDialog_DisableLogixNG") + ":");   // NOI18N
    
    private final JCheckBox _disableAllLogixNGCheckBox =
            new JCheckBox(Bundle.getMessage("ErrorHandlingDialog_DisableAllLogixNG") + ":");   // NOI18N
    
    private boolean _abortExecution = false;
    
    /*.*
     * 
     * 
     * 
     * An error has occurred in the ConditionalNG aaa.
     * An exception has occurred in NNNN  (show the short description of the action/expression)
     * Buffer overflow.  (show the error message here)
     * 
     * [x] Disable ConditionalNG
     * [x] Disable LogixNG
     * [x] Disable all LogixNGs
     * 
     * Abort will abort execution of the ConditionalNG by throwing
     * AbortConditionalNGExecutionException.
     * 
     * Continue will continue the execution of the ConditionalNG.
     * 
     * Abort | Continue
     * 
     * 
     */
    
    public boolean showDialog(Base item, String errorMessage) {
        
//        _functionComboBox.addActionListener((ActionEvent e) -> {
//        });
        
        _selectItemTypeDialog  = new JDialog(
                (JDialog)null,
                Bundle.getMessage("FunctionsHelpDialogTitle"),
                true);
        
        
        Container contentPanel = _selectItemTypeDialog.getContentPane();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        contentPanel.add(new JLabel(Bundle.getMessage(
                "ErrorHandlingDialog_Name", item.getShortDescription())));
        contentPanel.add(new JLabel(errorMessage));
        
        contentPanel.add(_disableConditionalNGCheckBox);
        contentPanel.add(_disableConditionalNGCheckBox);
        _disableConditionalNGCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(_disableLogixNGCheckBox);
        _disableLogixNGCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(_disableAllLogixNGCheckBox);
        _disableAllLogixNGCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        
        contentPanel.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        
        // Continue
        JButton continueButton = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(continueButton);
        continueButton.addActionListener((ActionEvent e) -> {
            continuePressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        continueButton.setToolTipText("CancelLogixButtonHint");      // NOI18N
        
        // Abort
        JButton abortButton = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(abortButton);
        abortButton.addActionListener((ActionEvent e) -> {
            abortPressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        abortButton.setToolTipText("CancelLogixButtonHint");      // NOI18N
        
        _selectItemTypeDialog.addWindowListener(new java.awt.event.WindowAdapter() {
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
        _selectItemTypeDialog.setLocationRelativeTo(null);
        _selectItemTypeDialog.pack();
        _selectItemTypeDialog.setVisible(true);
        
        return _abortExecution;
    }
    
    private void abortPressed(ActionEvent e) {
        _selectItemTypeDialog.setVisible(false);
        _selectItemTypeDialog.dispose();
        _selectItemTypeDialog = null;
        _abortExecution = true;
    }
    
    private void continuePressed(ActionEvent e) {
        _selectItemTypeDialog.setVisible(false);
        _selectItemTypeDialog.dispose();
        _selectItemTypeDialog = null;
    }
    
}
