package jmri.jmrit.conditional;

import java.awt.event.ActionEvent;
import java.util.Iterator;
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

import jmri.util.JmriJFrame;

/**
 * Basis for ConditionalEditFrame and ConditionalCopyFrame.
 * Holds the common features.
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
    String _antecedent;
    boolean _trigger;
    boolean _referenceByMemory;

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
        panel1.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnSystemName"))));
        JTextField systemName =  new JTextField(30);
        systemName.setText(conditional.getSystemName());
        systemName.setEditable(false);
        panel1.add(systemName);
        panel.add(panel1);
        JPanel panel2 = new JPanel();
        panel2.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnUserName"))));
        _conditionalUserName = new JTextField(30);
        panel2.add(_conditionalUserName);
        _conditionalUserName.setText(conditional.getUserName());
        _conditionalUserName.setToolTipText(Bundle.getMessage("ConditionalUserNameHint"));
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
        log.debug("updateConditionalPressed");
        String pref = InstanceManager.getDefault(jmri.LogixManager.class).getSystemPrefix();
        // The IX:RTXINITIALIZER keyword has a type of NONE
        // Safely remove elements from the list  (or use array.remove) if more than 1
        _variableList.removeIf(cvar -> (cvar.getType() == Conditional.Type.NONE && !cvar.getName().equals(pref + "X:RTXINITIALIZER")));
        // and actions
        _actionList.removeIf(cact -> cact.getType() == Conditional.Action.NONE);
        if (_parent.updateConditional(_conditionalUserName.getText(), _logicType, _trigger, _antecedent)) {
            if (_dataChanged) {
                _parent._showReminder = true;
            }
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

    boolean checkReferenceByMemory(String name) {
        _referenceByMemory = false;
        if (name.length() > 0 && name.charAt(0) == '@') {
            String memName = name.substring(1);
            if (!_parent.confirmIndirectMemory(memName)) {
                return false;
            }
            memName = _parent.validateMemoryReference(memName);
            if (memName == null) {
                return false;
            }
            _referenceByMemory = true;
        }
        return true;
    }

    /**
     * Check that a state variable is not also used as an action
     * @param name of the state variable
     * @param itemType item type of the state variable
     * @return true if action is not an action of if the user OK's
     * its use as such.
     */
    boolean checkIsAction(String name, Conditional.ItemType itemType) {
        String actionName = null;
        for (ConditionalAction action : _actionList) {
            Conditional.ItemType actionType = action.getType().getItemType();
            if (itemType == actionType) {
                if (name.equals(action.getDeviceName())) {
                    actionName = action.getDeviceName();
                } else {
                    NamedBean bean  = action.getBean();
                    if (bean != null &&
                        (name.equals(bean.getSystemName()) ||
                                name.equals(bean.getUserName()))) {
                        actionName = action.getDeviceName();
                   }
                }
            }
            if (actionName != null) {
                return _parent.confirmActionAsVariable(actionName, name);
            }
        }
        return true;
    }

    /**
     * Check that an action is not also used as a state variable
     * @param name of the action
     * @param itemType item type of the action
     * @return true if action is not a state variable of if the user OK's
     * its use as such.
     */
    boolean checkIsVariable(String name, Conditional.ItemType itemType) {
        String varName = null;
        for (ConditionalVariable var : _variableList) {
            Conditional.ItemType varType = var.getType().getItemType();
            if (itemType == varType) {
                if (name.equals(var.getName())) {
                    varName = var.getName();
                } else {
                    NamedBean bean  = var.getBean();
                    if (bean != null &&
                        (name.equals(bean.getSystemName()) ||
                                name.equals(bean.getUserName()))) {
                        varName = var.getName();
                   }
                }
            }
            if (varName != null) {
                return _parent.confirmActionAsVariable(name, varName);
            }
        }
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalFrame.class);

}
