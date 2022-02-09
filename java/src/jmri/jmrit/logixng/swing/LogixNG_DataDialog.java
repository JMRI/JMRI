package jmri.jmrit.logixng.swing;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.util.JmriJFrame;
import jmri.util.swing.BeanSelectPanel;

/**
 * A dialog
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_DataDialog {

    private final SwingConfiguratorInterface _swi;
    private JmriJFrame _showDialogFrame;


    private JTabbedPane _tabbedPaneTurnout;
    private BeanSelectPanel<? extends NamedBean> _turnoutBeanPanel;
    private JComboBox<? extends Object> _turnoutComboBox;
    private JPanel _panelTurnoutDirect;
    private JPanel _panelTurnoutReference;
    private JPanel _panelTurnoutLocalVariable;
    private JPanel _panelTurnoutFormula;
    private JTextField _turnoutReferenceTextField;
    private JTextField _turnoutLocalVariableTextField;
    private JTextField _turnoutFormulaTextField;



    public LogixNG_DataDialog(SwingConfiguratorInterface swi) {
        _swi = swi;
    }

    public void showDialog(BeanSelectPanel<? extends NamedBean> turnoutBeanPanel) {
        if (checkOpenDialog()) {
            return;
        }

        _turnoutBeanPanel = turnoutBeanPanel;
        _turnoutComboBox = null;
        showDialog();
    }

    public void showDialog(JComboBox<? extends Object> turnoutComboBox) {
        if (checkOpenDialog()) {
            return;
        }

        _turnoutBeanPanel = null;
        _turnoutComboBox = turnoutComboBox;
        showDialog();
    }

    private void showDialog() {
//        _showDialogFrame = new JmriJFrame(Bundle.getMessage(titleId));
        _showDialogFrame = new JmriJFrame("Edit table name");
//        _showDialogFrame.addHelpMenu(
//                "package.jmri.jmrit.beantable.LogixNGTable", true);     // NOI18N
//        _showDialogFrame.setLocation(50, 30);
        _showDialogFrame.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - _showDialogFrame.getWidth() / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - _showDialogFrame.getHeight() / 2);
        Container contentPane = _showDialogFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
//        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));



        _tabbedPaneTurnout = new JTabbedPane();
        _panelTurnoutDirect = new javax.swing.JPanel();
        _panelTurnoutReference = new javax.swing.JPanel();
        _panelTurnoutLocalVariable = new javax.swing.JPanel();
        _panelTurnoutFormula = new javax.swing.JPanel();

        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutDirect);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutReference);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutLocalVariable);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutFormula);

//        _turnoutBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        if (_turnoutBeanPanel != null) _panelTurnoutDirect.add(_turnoutBeanPanel);
        else if (_turnoutComboBox != null) _panelTurnoutDirect.add(_turnoutComboBox);

        _turnoutReferenceTextField = new JTextField();
        _turnoutReferenceTextField.setColumns(30);
        _panelTurnoutReference.add(_turnoutReferenceTextField);

        _turnoutLocalVariableTextField = new JTextField();
        _turnoutLocalVariableTextField.setColumns(30);
        _panelTurnoutLocalVariable.add(_turnoutLocalVariableTextField);

        _turnoutFormulaTextField = new JTextField();
        _turnoutFormulaTextField.setColumns(30);
        _panelTurnoutFormula.add(_turnoutFormulaTextField);


        p.add(_tabbedPaneTurnout);



/*
        p.setLayout(new FlowLayout());
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(_sysNameLabel, c);
        _sysNameLabel.setLabelFor(_systemName);
        c.gridy = 1;
        p.add(_userNameLabel, c);
        _userNameLabel.setLabelFor(_addUserName);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_systemName, c);
        c.gridy = 1;
        p.add(_addUserName, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
        p.add(_autoSystemName, c);
        _addUserName.setToolTipText(Bundle.getMessage("LogixNGUserNameHint"));    // NOI18N
        _systemName.setToolTipText(Bundle.getMessage("LogixNGSystemNameHint"));   // NOI18N
        contentPane.add(p);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        JLabel message1 = new JLabel(Bundle.getMessage(startMessageId + "LogixNGMessage1"));  // NOI18N
        panel31.add(message1);
        JPanel panel32 = new JPanel();
        JLabel message2 = new JLabel(Bundle.getMessage(startMessageId + "LogixNGMessage2"));  // NOI18N
        panel32.add(message2);
        panel3.add(panel31);
        panel3.add(panel32);
        contentPane.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener(this::cancelAddPressed);
        cancel.setToolTipText(Bundle.getMessage("CancelLogixNGButtonHint"));      // NOI18N

        addLogixNGFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelAddPressed(null);
            }
        });
        contentPane.add(panel5);
*/
//        _autoSystemName.addItemListener((ItemEvent e) -> {
//            autoSystemName();
//        });

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener(this::cancelDialogPressed);
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixNGButtonHint"));      // NOI18N

        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
        panel5.add(okButton);
        okButton.addActionListener((ActionEvent e) -> {
            _showDialogFrame.setVisible(false);
            _showDialogFrame.dispose();
            _showDialogFrame = null;
            if (_swi.getJDialog() != null) {
                _swi.getJDialog().setVisible(true);
            }
        });

        _showDialogFrame.add(p);

        _showDialogFrame.add(panel5);

        _showDialogFrame.pack();
        _showDialogFrame.setVisible(true);


    }

    public boolean checkOpenDialog() {
        if (_showDialogFrame != null) {
            _showDialogFrame.requestFocus();
            return true;
        }
        return false;
    }

    /**
     * Respond to the Cancel button in Add bean window.
     * <p>
     * Note: Also get there if the user closes the Add bean window.
     *
     * @param e The event heard
     */
    void cancelDialogPressed(ActionEvent e) {
        _showDialogFrame.setVisible(false);
        _showDialogFrame.dispose();
        _showDialogFrame = null;
        if (_swi.getJDialog() != null) {
            _swi.getJDialog().setVisible(true);
        }
    }

    public void dispose() {
        if (_showDialogFrame != null) {
            _showDialogFrame.setVisible(false);
            _showDialogFrame.dispose();
        }
    }

}
