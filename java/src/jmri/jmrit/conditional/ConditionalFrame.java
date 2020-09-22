package jmri.jmrit.conditional;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.conditional.Bundle;
import jmri.util.JmriJFrame;

/**
 * Basis for ConditionalEditFrame and ConditionalCopyFrame.
 * 
 * @author Pete Cressman Copyright (C) 2020
 */
public class ConditionalFrame extends JmriJFrame {
    protected ConditionalList _parent;
    boolean _dataChanged = false;

    // ------------ Current Conditional Information ------------
    List<ConditionalVariable> _variableList;
    List<ConditionalAction> _actionList;

    Conditional.AntecedentOperator _logicType = Conditional.AntecedentOperator.ALL_AND;
    String _antecedent = null;
    boolean _trigger;

    // ------------------ Common window parts --------------
    JTextField _conditionalUserName;

    static final int STRUT = 10;

    // ------------------------------------------------------------------
    
    ConditionalFrame(String title, Conditional conditional, ConditionalList parent) {
        super(title, false, false);
        _parent = parent;
        _variableList = conditional.getCopyOfStateVariables();
        _actionList = conditional.getCopyOfActions();
        _logicType = conditional.getLogicType();
        _antecedent = conditional.getAntecedentExpression();
    }

    JPanel makeTopPanel(Conditional conditional) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel panel1 = new JPanel();
        panel1.add(new JLabel(Bundle.getMessage("ColumnSystemName") + ":"));  // NOI18N
        JTextField systemName =  new JTextField(30);
        systemName.setText(conditional.getSystemName());
        systemName.setEditable(false);
        panel1.add(systemName);
        panel.add(panel1);
        JPanel panel2 = new JPanel();
        panel2.add(new JLabel(Bundle.getMessage("ColumnUserName") + ":"));  // NOI18N
        _conditionalUserName = new JTextField(30);
        panel2.add(_conditionalUserName);
        _conditionalUserName.setText(conditional.getUserName());
        _conditionalUserName.setToolTipText(Bundle.getMessage("ConditionalUserNameHint"));  // NOI18N
        panel.add(panel2);
        return panel;
    }
    /**
     * Create Variable and Action editing pane center part. (Utility)
     *
     * @param comp  Field or comboBox to include on sub pane
     * @param label property key for label
     * @param hint  property key for tooltip for this sub pane
     * @return JPanel containing interface
     */
    JPanel makeEditPanel(JComponent comp, String label, String hint) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage(label)));
        panel.add(p);
        if (hint != null) {
            panel.setToolTipText(Bundle.getMessage(hint));
        }
        comp.setMaximumSize(comp.getPreferredSize());  // override for text fields
        panel.add(comp);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
     * Respond to the Update Conditional Button in the Edit Conditional window.
     *
     * @param e The event heard
     * @return true if updated
     */
    boolean updateConditionalPressed(ActionEvent e) {
        for (int i = 0; i < _variableList.size(); i++) {
            if (_variableList.get(i).getType() == Conditional.Type.NONE) {
                _variableList.remove(i);
//                _variableTableModel.fireTableRowsDeleted(i, i);
            }
        }
        for (int i = 0; i < _actionList.size(); i++) {
            if (_actionList.get(i).getType() == Conditional.Action.NONE) {
                _actionList.remove(i);
//                _actionTableModel.fireTableRowsDeleted(i, i);
            }
        }
        if (_parent.updateConditional(_conditionalUserName.getText(), _logicType, _trigger, _antecedent)) {
            if (_dataChanged) {
                _parent._showReminder = true;
            }
            cancelConditionalPressed();
            return true;
        }
        return false;
    }

    /**
     * Respond to the Cancel button in the Edit Conditional frame.
     * <p>
     * Does the cleanup from deleteConditionalPressed, updateConditionalPressed
     * and _editConditionalFrame window closer.
     */
    void cancelConditionalPressed() {
        log.debug("cancelConditionalPressed");
        _dataChanged = false;
        _parent.closeConditionalFrame();
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalFrame.class);
}
