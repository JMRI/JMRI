package jmri.jmrit.conditional;

import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basis for ConditionalListEdit and ConditionalListCopy
 *
 * @author Pete Cressman Copyright (C) 2020
 */
abstract public class ConditionalList extends ConditionalEditBase {

    Conditional _curConditional = null;
    ConditionalFrame _conditionalFrame = null;
    boolean _newConditional = false;
    TreeSet<String> _oldTargetNames = new TreeSet<>();

    /**
     * Create a new Conditional List View editor.
     *
     * @param sName name of the Logix being edited
     */
    public ConditionalList(String sName) {
        super(sName);
    }

    public ConditionalList() {
    }

    // ------------ Methods for Edit Conditional Pane ------------

    Conditional makeNewConditional(Logix logix) {
        log.debug("makeNewConditional(Logix)");
        // make system name for new conditional
        int num = logix.getNumConditionals() + 1;
        Conditional conditional = null;
        String cName = null;
        while (conditional == null) {
            cName = logix.getSystemName() + "C" + Integer.toString(num);
            conditional = _conditionalManager.createNewConditional(cName, "");
            num++;
            if (num == 1000) {
                break;
            }
        }
        _newConditional = (conditional != null);
        return conditional;
    }

    /**
     * Make the bottom panel for _conditionalFrame to hold buttons for
     * Update/Save, Cancel, Delete/FullEdit
     *
     * @return the panel
     */
    abstract JPanel makeBottomPanel();

    abstract void updateConditionalTableModel();

    /**
     * Update _curConditional, the current Conditional.
     * Checks for being well formed rules and registers its usage.
     *
     * @param uName Conditiona's user name
     * @param logicType Logic type od antecedent
     * @param trigger Trigger on variablr change action choice
     * @param antecedent the antecedent
     * @return true, if update is made
     */
    abstract boolean updateConditional(String uName, Conditional.AntecedentOperator logicType, boolean trigger, String antecedent);

    boolean updateConditional(String uName, Logix logix,
            Conditional.AntecedentOperator logicType, boolean trigger, String antecedent) {
        log.debug("updateConditional");

        // Check if the User Name has been changed
        if (!uName.equals(_curConditional.getUserName())) {
            // user name has changed - check if already in use
            if (!checkConditionalUserName(uName, logix)) {
                return false;
            }
            // user name is unique or blank, change it
            _curConditional.setUserName(uName);
        }
        if (_conditionalFrame._variableList.size() <= 0 && !_suppressReminder) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Warn5", _curConditional.getUserName(), _curConditional.getSystemName()),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
        // complete update
        _curConditional.setStateVariables(_conditionalFrame._variableList);
        _curConditional.setAction(_conditionalFrame._actionList);
        _curConditional.setTriggerOnChange(trigger);
        _curConditional.setLogicType(logicType, antecedent);

        if (_newConditional) {
            // add to Logix at the end of the calculate order
            logix.addConditional(_curConditional.getSystemName(), -1);
            _showReminder = true;
            _newConditional = false;
            updateConditionalTableModel();
        }
        TreeSet<String> newTargetNames = new TreeSet<String>();
        loadReferenceNames(_conditionalFrame._variableList, newTargetNames);
        updateWhereUsed(_oldTargetNames, newTargetNames, _curConditional.getSystemName());
        closeConditionalFrame();
        return true;
    }

    PickSingleListener getPickSingleListener(JTextField textField, Conditional.ItemType itemType) {
        return new PickSingleListener(textField, itemType);
    }

    abstract void closeConditionalFrame();

    void closeConditionalFrame(Logix logix) {
        log.debug("closeConditionalFrame(Logix)");
        try {
            logix.activateLogix();
        } catch (NumberFormatException nfe) {
            log.debug("NumberFormatException on activation of Logix ", nfe);  // NOI18N
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error4") + nfe.toString() + Bundle.getMessage("Error7"), // NOI18N
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);  // NOI18N
        }
        if (_pickTables != null) {
            _pickTables.dispose();
            _pickTables = null;
        }
        // when user uses the escape key and returns to editing, interaction with
        // window closing event create strange environment

        if (_conditionalFrame != null) {
            _conditionalFrame.dispose();
            _conditionalFrame = null;
        }
        if (_editLogixFrame != null) {
            _editLogixFrame.setVisible(true);
        }
    }

    @Override
    protected String getClassName() {
        return ConditionalListEdit.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalList.class);
}
