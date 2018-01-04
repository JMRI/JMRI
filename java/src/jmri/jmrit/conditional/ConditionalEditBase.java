package jmri.jmrit.conditional;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.Audio;
import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.Route;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.picker.PickFrame;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickSinglePanel;
import jmri.util.JmriJFrame;
import jmri.util.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class for the Conditional edit view classes. Contains shared
 * variables and methods.
 * <p>
 * @author Dave Sand copyright (c) 2017
 */
public class ConditionalEditBase {

    /**
     * Set the Logix and Conditional managers and set the selection mode.
     *
     * @param sName the Logix system name being edited
     */
    public ConditionalEditBase(String sName) {
//         _logixManager = InstanceManager.getNullableDefault(jmri.LogixManager.class);
//         _conditionalManager = InstanceManager.getNullableDefault(jmri.ConditionalManager.class);
        _logixManager = InstanceManager.getDefault(jmri.LogixManager.class);
        _conditionalManager = InstanceManager.getDefault(jmri.ConditionalManager.class);
        _curLogix = _logixManager.getBySystemName(sName);
        loadSelectionMode();
    }

    public ConditionalEditBase() {
    }

    // ------------ variable definitions ------------
    ConditionalManager _conditionalManager = null;
    LogixManager _logixManager = null;
    Logix _curLogix = null;

    int _numConditionals = 0;
    boolean _inEditMode = false;

    boolean _showReminder = false;
    boolean _suppressReminder = false;
    boolean _suppressIndirectRef = false;

    JmriBeanComboBox _comboNameBox = null;

    /**
     * Input selection names
     *
     * @since 4.7.3
     */
    public enum SelectionMode {
        /**
         * Use the traditional text field, with the tabbed Pick List available
         * for drag-n-drop
         */
        USEMULTI,
        /**
         * Use the traditional text field, but with a single Pick List that
         * responds with a click
         */
        USESINGLE,
        /**
         * Use combo boxes to select names instead of a text field.
         */
        USECOMBO;
    }
    SelectionMode _selectionMode;

    /**
     * Get the saved mode selection, default to the tranditional tabbed pick
     * list.
     * <p>
     * During the menu build process, the corresponding menu item is set to
     * selected.
     *
     * @since 4.7.3
     */
    void loadSelectionMode() {
        Object modeName = InstanceManager.getDefault(jmri.UserPreferencesManager.class).getProperty("jmri.jmrit.beantable.LogixTableAction", "Selection Mode"); // NOI18N
        if (modeName == null) {
            _selectionMode = SelectionMode.USEMULTI;
        } else {
            String currentMode = (String) modeName;
            switch (currentMode) {
                case "USEMULTI":        // NOI18N
                    _selectionMode = SelectionMode.USEMULTI;
                    break;
                case "USESINGLE":       // NOI18N
                    _selectionMode = SelectionMode.USESINGLE;
                    break;
                case "USECOMBO":        // NOI18N
                    _selectionMode = SelectionMode.USECOMBO;
                    break;
                default:
                    log.warn("Invalid Logix conditional selection mode value, '{}', returned", currentMode);  // NOI18N
                    _selectionMode = SelectionMode.USEMULTI;
            }
        }
    }

    // ------------ PickList components ------------
    JTable _pickTable = null;               // Current pick table
    JTabbedPane _pickTabPane = null;        // The tabbed panel for the pick table
    PickFrame _pickTables;

    PickSinglePanel _pickSingle = null;     // used to build the JFrame, content copied from the table type pick object.
    JFrame _pickSingleFrame = null;
    PickSingleListener _pickListener = null;

    // ------------ Logix Notifications ------------
    // The Conditional views support some direct changes to the parent logix.
    // This custom event is used to notify the parent Logix that changes are requested.
    // When the event occurs, the parent Logix can retrieve the necessary information
    // to carry out the actions.
    //
    // 1) Notify the calling Logix that the Logix user name has been changed.
    // 2) Notify the calling Logix that the conditional view is closing
    // 3) Notify the calling Logix that it is to be deleted
    /**
     * Create a custom listener event
     */
    public interface LogixEventListener extends EventListener {

        void logixEventOccurred();
    }

    /**
     * Maintain a list of listeners -- normally only one.
     */
    List<LogixEventListener> listenerList = new ArrayList<>();

    /**
     * This contains a list of commands to be processed by the listener
     * recipient
     */
    public HashMap<String, String> logixData = new HashMap<>();

    /**
     * Add a listener
     *
     * @param listener The recipient
     */
    public void addLogixEventListener(LogixEventListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove a listener -- not used
     *
     * @param listener The recipient
     */
    public void removeLogixEventListener(LogixEventListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Notify the listeners to check for new data
     */
    void fireLogixEvent() {
        for (LogixEventListener l : listenerList) {
            l.logixEventOccurred();
        }
    }

    // ------------ Shared Conditional Methods ------------
    /**
     * Verify that the user name is not a duplicate for the selected Logix
     *
     * @param uName is the user name to be checked
     * @param logix is the Logix that is being updated
     * @return true if the name is unique
     */
    boolean checkConditionalUserName(String uName, Logix logix) {
        if (uName != null && uName.length() > 0) {
            Conditional p = _conditionalManager.getByUserName(logix, uName);
            if (p != null) {
                // Conditional with this user name already exists
                log.error("Failure to update Conditional with Duplicate User Name: " // NOI18N
                        + uName);
                javax.swing.JOptionPane.showMessageDialog(
                        null, Bundle.getMessage("Error10"), // NOI18N
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } // else return true;
        return true;
    }

    /**
     * Create a combo name box for Variable and Action name selection.
     *
     * @param itemType The selected variable or action type
     * @return nameBox A combo box based on the item type
     */
    JmriBeanComboBox createNameBox(int itemType) {
        JmriBeanComboBox nameBox;
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:      // 1
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(SensorManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_TURNOUT:     // 2
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(TurnoutManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_LIGHT:       // 3
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(LightManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:  // 4
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(SignalHeadManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:  // 5
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(SignalMastManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_MEMORY:      // 6
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(MemoryManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_LOGIX:       // 7
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(LogixManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_WARRANT:     // 8
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(WarrantManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_OBLOCK:      // 10
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(OBlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:   // 11
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(EntryExitPairs.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            case Conditional.ITEM_TYPE_OTHER:   // 14
                nameBox = new JmriBeanComboBox(
                        InstanceManager.getDefault(jmri.RouteManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                break;
            default:
                return null;             // Skip any other items.
        }
        nameBox.setFirstItemBlank(true);
        JComboBoxUtil.setupComboBoxMaxRows(nameBox);
        return nameBox;
    }

    /**
     * Listen for name combo box selection events.
     * <p>
     * When a combo box row is selected, the user/system name is copied to the
     * Action or Variable name field.
     *
     * @since 4.7.3
     */
    static class NameBoxListener implements ActionListener {

        /**
         * @param textField The target field object when an entry is selected
         */
        public NameBoxListener(JTextField textField) {
            saveTextField = textField;
        }

        JTextField saveTextField;

        @Override
        public void actionPerformed(ActionEvent e) {
            // Get the combo box and display name
            Object src = e.getSource();
            if (!(src instanceof JmriBeanComboBox)) {
                return;
            }
            JmriBeanComboBox srcBox = (JmriBeanComboBox) src;
            String newName = srcBox.getSelectedDisplayName();

            if (log.isDebugEnabled()) {
                log.debug("NameBoxListener: new name = '{}'", newName);  // NOI18N
            }
            saveTextField.setText(newName);
        }
    }

    // ------------ Single Pick List Table Methods ------------
    /**
     * Create a single panel picklist JFrame for choosing action and variable
     * names.
     *
     * @since 4.7.3
     * @param itemType   The selected variable or action type
     * @param listener   The listener to be assigned to the picklist
     * @param actionType True if Action, false if Variable.
     */
    void createSinglePanelPickList(int itemType, PickSingleListener listener, boolean actionType) {
        if (_pickListener != null) {
            int saveType = _pickListener.getItemType();
            if (saveType != itemType) {
                // The type has changed, need to start over
                closeSinglePanelPickList();
            } else {
                // The pick list has already been created
                return;
            }
        }

        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:      // 1
                _pickSingle = new PickSinglePanel(PickListModel.sensorPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_TURNOUT:     // 2
                _pickSingle = new PickSinglePanel(PickListModel.turnoutPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_LIGHT:       // 3
                _pickSingle = new PickSinglePanel(PickListModel.lightPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:  // 4
                _pickSingle = new PickSinglePanel(PickListModel.signalHeadPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:  // 5
                _pickSingle = new PickSinglePanel(PickListModel.signalMastPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_MEMORY:      // 6
                _pickSingle = new PickSinglePanel(PickListModel.memoryPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_LOGIX:      // 7 -- can be either Logix or Conditional
                if (!actionType) {
                    // State Variable
                    return;
                }
                _pickSingle = new PickSinglePanel(PickListModel.logixPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_WARRANT:     // 8
                _pickSingle = new PickSinglePanel(PickListModel.warrantPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_OBLOCK:      // 10
                _pickSingle = new PickSinglePanel(PickListModel.oBlockPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:   // 11
                _pickSingle = new PickSinglePanel(PickListModel.entryExitPickModelInstance());
                break;
            default:
                return;             // Skip any other items.
        }

        // Create the JFrame
        _pickSingleFrame = new JmriJFrame(Bundle.getMessage("SinglePickFrame"));  // NOI18N
        _pickSingleFrame.setContentPane(_pickSingle);
        _pickSingleFrame.pack();
        _pickSingleFrame.setVisible(true);
        _pickSingleFrame.toFront();

        // Set the table selection listener
        _pickListener = listener;
        _pickTable = _pickSingle.getTable();
        _pickTable.getSelectionModel().addListSelectionListener(_pickListener);
    }

    /**
     * Close a single panel picklist JFrame and related items.
     *
     * @since 4.7.3
     */
    void closeSinglePanelPickList() {
        if (_pickSingleFrame != null) {
            _pickSingleFrame.setVisible(false);
            _pickSingleFrame.dispose();
            _pickSingleFrame = null;
            _pickListener = null;
            _pickTable = null;
            _pickSingle = null;
        }
    }

    /**
     * Listen for Pick Single table click events.
     * <p>
     * When a table row is selected, the user/system name is copied to the
     * Action or Variable name field.
     *
     * @since 4.7.3
     */
    class PickSingleListener implements ListSelectionListener {

        /**
         * @param textField The target field object when an entry is selected
         * @param itemType  The current selected table type number
         */
        public PickSingleListener(JTextField textField, int itemType) {
            saveItemType = itemType;
            saveTextField = textField;
        }

        JTextField saveTextField;
        int saveItemType;          // Current table type

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int selectedRow = _pickTable.getSelectedRow();
            if (selectedRow >= 0) {
                int selectedCol = _pickTable.getSelectedColumn();
                String newName = (String) _pickTable.getValueAt(selectedRow, selectedCol);
                if (log.isDebugEnabled()) {
                    log.debug("Pick single panel row event: row = '{}', column = '{}', selected name = '{}'", // NOI18N
                            selectedRow, selectedCol, newName);
                }
                saveTextField.setText(newName);
            }
        }

        public int getItemType() {
            return saveItemType;
        }
    }

    // ------------ Pick List Table Methods ------------
    /**
     * Open a new drag-n-drop Pick List to drag Variable and Action names from
     * to form Logix Conditionals.
     */
    void openPickListTable() {
        if (_pickTables == null) {
            _pickTables = new jmri.jmrit.picker.PickFrame(Bundle.getMessage("TitlePickList"));  // NOI18N
        } else {
            _pickTables.setVisible(true);
        }
        _pickTables.toFront();
    }

    /**
     * Hide the drag-n-drop Pick List if the last detail edit is closing
     */
    void hidePickListTable() {
        if (_pickTables != null) {
            _pickTables.setVisible(false);
        }
    }

    /**
     * Set the pick list tab based on the variable or action type If there is
     * not a corresponding tab, hide the picklist
     *
     * @param curType    is the current type
     * @param actionType True if Action, false if Variable.
     */
    void setPickListTab(int curType, boolean actionType) {
        boolean tabSet = true;
        if (_pickTables == null) {
            return;
        }
        if (_pickTabPane == null) {
            findPickListTabPane(_pickTables.getComponents(), 1);
        }
        if (_pickTabPane != null) {
            // Convert variable/action type to the corresponding tab index
            int tabIndex = 0;
            switch (curType) {
                case Conditional.ITEM_TYPE_SENSOR:    // 1
                    tabIndex = 1;
                    break;
                case Conditional.ITEM_TYPE_TURNOUT:   // 2
                    tabIndex = 0;
                    break;
                case Conditional.ITEM_TYPE_LIGHT:     // 3
                    tabIndex = 6;
                    break;
                case Conditional.ITEM_TYPE_SIGNALHEAD:            // 4
                    tabIndex = 2;
                    break;
                case Conditional.ITEM_TYPE_SIGNALMAST:            // 5
                    tabIndex = 3;
                    break;
                case Conditional.ITEM_TYPE_MEMORY:    // 6
                    tabIndex = 4;
                    break;
                case Conditional.ITEM_TYPE_LOGIX:     // 7 Conditional (Variable) or Logix (Action)
                    if (actionType) {
                        tabIndex = 10;
                    } else {
                        // State Variable
                        tabSet = false;
                    }
                    break;
                case Conditional.ITEM_TYPE_WARRANT:   // 8
                    tabIndex = 7;
                    break;
                case Conditional.ITEM_TYPE_OBLOCK:    // 10
                    tabIndex = 8;
                    break;
                case Conditional.ITEM_TYPE_ENTRYEXIT: // 11
                    tabIndex = 9;
                    break;
                default:
                    // No tab found
                    tabSet = false;
            }
            if (tabSet) {
                _pickTabPane.setSelectedIndex(tabIndex);
            }
        }
        _pickTables.setVisible(tabSet);
        return;
    }

    /**
     * Recursive search for the tab panel
     *
     * @param compList The components for the current Level
     * @param level    The current level in the structure
     */
    void findPickListTabPane(Component[] compList, int level) {
        for (Component compItem : compList) {
            // Safety catch
            if (level > 10) {
                log.warn("findPickListTabPane: safety breaker reached");  // NOI18N
                return;
            }

            if (compItem instanceof JTabbedPane) {
                _pickTabPane = (JTabbedPane) compItem;
            } else {
                int nextLevel = level + 1;
                Container nextItem = (Container) compItem;
                Component[] nextList = nextItem.getComponents();
                findPickListTabPane(nextList, nextLevel);
            }
        }
        return;
    }

    // ------------ Manage Conditional Reference map ------------
    /**
     * Build a tree set from conditional references.
     *
     * @since 4.7.4
     * @param varList The ConditionalVariable list that might contain
     *                conditional references
     * @param treeSet A tree set to be built from the varList data
     */
    void loadReferenceNames(ArrayList<ConditionalVariable> varList, TreeSet<String> treeSet) {
        treeSet.clear();
        for (ConditionalVariable var : varList) {
            if (var.getType() == Conditional.TYPE_CONDITIONAL_TRUE || var.getType() == Conditional.TYPE_CONDITIONAL_FALSE) {
                treeSet.add(var.getName());
            }
        }
    }

    /**
     * Check for conditional references
     *
     * @since 4.7.4
     * @param logixName The Logix under consideration
     * @return true if no references
     */
    boolean checkConditionalReferences(String logixName) {
        Logix x = _logixManager.getLogix(logixName);
        int numConditionals = x.getNumConditionals();
        for (int i = 0; i < numConditionals; i++) {
            String csName = x.getConditionalByNumberOrder(i);

            // If the conditional is a where used target, check scope
            ArrayList<String> refList = InstanceManager.getDefault(jmri.ConditionalManager.class).getWhereUsed(csName);
            if (refList != null) {
                for (String refName : refList) {
                    Logix xRef = _conditionalManager.getParentLogix(refName);
                    String xsName = xRef.getSystemName();
                    if (logixName.equals(xsName)) {
                        // Member of the same Logix
                        continue;
                    }

                    // External references have to be removed before the Logix can be deleted.
                    Conditional c = x.getConditional(csName);
                    Conditional cRef = xRef.getConditional(refName);
                    String[] msgs = new String[]{c.getUserName(), c.getSystemName(), cRef.getUserName(),
                        cRef.getSystemName(), xRef.getUserName(), xRef.getSystemName()};
                    javax.swing.JOptionPane.showMessageDialog(null,
                            java.text.MessageFormat.format(Bundle.getMessage("Error11"), (Object[]) msgs), // NOI18N
                            Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);        // NOI18N
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Update the conditional reference where used.
     * <p>
     * The difference between the saved target names and new target names is
     * used to add/remove where used references.
     *
     * @since 4.7.4
     * @param oldTargetNames The conditional target names before updating
     * @param newTargetNames The conditional target names after updating
     * @param refName        The system name for the referencing conditional
     */
    void updateWhereUsed(TreeSet<String> oldTargetNames, TreeSet<String> newTargetNames, String refName) {
        TreeSet<String> deleteNames = new TreeSet<>(oldTargetNames);
        deleteNames.removeAll(newTargetNames);
        for (String deleteName : deleteNames) {
            InstanceManager.getDefault(jmri.ConditionalManager.class).removeWhereUsed(deleteName, refName);
        }

        TreeSet<String> addNames = new TreeSet<>(newTargetNames);
        addNames.removeAll(oldTargetNames);
        for (String addName : addNames) {
            InstanceManager.getDefault(jmri.ConditionalManager.class).addWhereUsed(addName, refName);
        }
    }

    // ------------ Utility Methods - Data Validation ------------
    /**
     * Display reminder to save.
     */
    void showSaveReminder() {
        if (_showReminder) {
            if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString", // NOI18N
                                Bundle.getMessage("MenuItemLogixTable")), // NOI18N
                                getClassName(),
                                "remindSaveLogix"); // NOI18N
            }
        }
    }

    /**
     * Check if String is an integer or references an integer.
     *
     * @param actionType   Conditional action to check for, i.e.
     *                     ACTION_SET_LIGHT_INTENSITY
     * @param intReference string referencing a decimal for light intensity or
     *                     the name of a memory
     * @return true if either correct decimal format or a memory with the given
     *         name is present
     */
    boolean validateIntensityReference(int actionType, String intReference) {
        if (intReference == null || intReference.trim().length() == 0) {
            displayBadNumberReference(actionType);
            return false;
        }
        try {
            return validateIntensity(Integer.valueOf(intReference).intValue());
        } catch (NumberFormatException e) {
            String intRef = intReference;
            if (intReference.length() > 1 && intReference.charAt(0) == '@') {
                intRef = intRef.substring(1);
            }
            if (!confirmIndirectMemory(intRef)) {
                return false;
            }
            intRef = validateMemoryReference(intRef);
            if (intRef != null) // memory named 'intReference' exists
            {
                Memory m = InstanceManager.memoryManagerInstance().getByUserName(intRef);
                if (m == null) {
                    m = InstanceManager.memoryManagerInstance().getBySystemName(intRef);
                }
                try {
                    if (m == null || m.getValue() == null) {
                        throw new NumberFormatException();
                    }
                    validateIntensity(Integer.valueOf((String) m.getValue()).intValue());
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(
                            null, java.text.MessageFormat.format(Bundle.getMessage("Error24"), // NOI18N
                                    intReference), Bundle.getMessage("WarningTitle"), javax.swing.JOptionPane.WARNING_MESSAGE); // NOI18N
                }
                return true;    // above is a warning to set memory correctly
            }
            displayBadNumberReference(actionType);
        }
        return false;
    }

    /**
     * Check if text represents an integer is suitable for percentage w/o
     * NumberFormatException.
     *
     * @param time value to use as light intensity percentage
     * @return true if time is an integer in range 0 - 100
     */
    boolean validateIntensity(int time) {
        if (time < 0 || time > 100) {
            javax.swing.JOptionPane.showMessageDialog(
                    null, java.text.MessageFormat.format(Bundle.getMessage("Error38"), // NOI18N
                            time, Bundle.getMessage("Error42")), Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);   // NOI18N
            return false;
        }
        return true;
    }

    /**
     * Check if a string is decimal or references a decimal.
     *
     * @param actionType integer representing the Conditional action type being
     *                   checked, i.e. ACTION_DELAYED_TURNOUT
     * @param ref        entry to check
     * @return true if ref is itself a decimal or user will provide one from a
     *         Memory at run time
     */
    boolean validateTimeReference(int actionType, String ref) {
        if (ref == null || ref.trim().length() == 0) {
            displayBadNumberReference(actionType);
            return false;
        }
        try {
            return validateTime(actionType, Float.valueOf(ref).floatValue());
            // return true if ref is decimal within allowed range
        } catch (NumberFormatException e) {
            String memRef = ref;
            if (ref.length() > 1 && ref.charAt(0) == '@') {
                memRef = ref.substring(1);
            }
            if (!confirmIndirectMemory(memRef)) {
                return false;
            }
            memRef = validateMemoryReference(memRef);
            if (memRef != null) // memory named 'intReference' exists
            {
                Memory m = InstanceManager.memoryManagerInstance().getByUserName(memRef);
                if (m == null) {
                    m = InstanceManager.memoryManagerInstance().getBySystemName(memRef);
                }
                try {
                    if (m == null || m.getValue() == null) {
                        throw new NumberFormatException();
                    }
                    validateTime(actionType, Float.valueOf((String) m.getValue()).floatValue());
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(
                            null, java.text.MessageFormat.format(Bundle.getMessage("Error24"), // NOI18N
                                    memRef), Bundle.getMessage("WarningTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);   // NOI18N
                }
                return true;    // above is a warning to set memory correctly
            }
            displayBadNumberReference(actionType);
        }
        return false;
    }

    /**
     * Range check time entry (assumes seconds).
     *
     * @param actionType integer representing the Conditional action type being
     *                   checked, i.e. ACTION_DELAYED_TURNOUT
     * @param time       value to be checked
     * @return false if time &gt; 3600 (seconds) or too small
     */
    boolean validateTime(int actionType, float time) {
        float maxTime = 3600;     // more than 1 hour
        float minTime = 0.020f;
        if (time < minTime || time > maxTime) {
            String errorNum = " ";
            switch (actionType) {
                case Conditional.ACTION_DELAYED_TURNOUT:
                    errorNum = "Error39";       // NOI18N
                    break;
                case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                    errorNum = "Error41";       // NOI18N
                    break;
                case Conditional.ACTION_DELAYED_SENSOR:
                    errorNum = "Error23";       // NOI18N
                    break;
                case Conditional.ACTION_RESET_DELAYED_SENSOR:
                    errorNum = "Error27";       // NOI18N
                    break;
                case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                    errorNum = "Error29";       // NOI18N
                    break;
                default:
                    break;
            }
            javax.swing.JOptionPane.showMessageDialog(
                    null, java.text.MessageFormat.format(Bundle.getMessage("Error38"), // NOI18N
                            time, Bundle.getMessage(errorNum)), Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);       // NOI18N
            return false;
        }
        return true;
    }

    /**
     * Display an error message to user when an invalid number is provided in
     * Conditional set up.
     *
     * @param actionType integer representing the Conditional action type being
     *                   checked, i.e. ACTION_DELAYED_TURNOUT
     */
    void displayBadNumberReference(int actionType) {
        String errorNum = " ";
        switch (actionType) {
            case Conditional.ACTION_DELAYED_TURNOUT:
                errorNum = "Error39";       // NOI18N
                break;
            case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                errorNum = "Error41";       // NOI18N
                break;
            case Conditional.ACTION_DELAYED_SENSOR:
                errorNum = "Error23";       // NOI18N
                break;
            case Conditional.ACTION_RESET_DELAYED_SENSOR:
                errorNum = "Error27";       // NOI18N
                break;
            case Conditional.ACTION_SET_LIGHT_INTENSITY:
                javax.swing.JOptionPane.showMessageDialog(
                        null, Bundle.getMessage("Error43"), // NOI18N
                        Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);       // NOI18N
                return;
            case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                errorNum = "Error29";       // NOI18N
                break;
            default:
                log.warn("Unexpected action type {} in displayBadNumberReference", actionType);  // NOI18N
        }
        javax.swing.JOptionPane.showMessageDialog(
                null, java.text.MessageFormat.format(Bundle.getMessage("Error9"), // NOI18N
                        Bundle.getMessage(errorNum)), Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);       // NOI18N
    }

    /**
     * Check Memory reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Memory, null if not
     *         found
     */
    String validateMemoryReference(String name) {
        Memory m = null;
        if (name != null) {
            if (name.length() > 0) {
                m = InstanceManager.memoryManagerInstance().getByUserName(name);
                if (m != null) {
                    return name;
                }
            }
            m = InstanceManager.memoryManagerInstance().getBySystemName(name);
        }
        if (m == null) {
            messageInvalidActionItemName(name, "Memory"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check if user will provide a valid item name in a Memory variable
     *
     * @param memName Memory location to provide item name at run time
     * @return false if user replies No
     */
    boolean confirmIndirectMemory(String memName) {
        if (!_suppressIndirectRef) {
            int response = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                    Bundle.getMessage("ConfirmIndirectReference"), memName), // NOI18N
                    Bundle.getMessage("ConfirmTitle"), JOptionPane.YES_NO_CANCEL_OPTION, // NOI18N
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
                return false;
            } else if (response == JOptionPane.CANCEL_OPTION) {
                _suppressIndirectRef = true;
            }
        }
        return true;
    }

    /**
     * Check Turnout reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Turnout, null if not
     *         found
     */
    String validateTurnoutReference(String name) {
        Turnout t = null;
        if (name != null) {
            if (name.length() > 0) {
                t = InstanceManager.turnoutManagerInstance().getByUserName(name);
                if (t != null) {
                    return name;
                }
            }
            t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
        }
        if (t == null) {
            messageInvalidActionItemName(name, "Turnout"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check SignalHead reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding SignalHead, null if
     *         not found
     */
    String validateSignalHeadReference(String name) {
        SignalHead h = null;
        if (name != null) {
            if (name.length() > 0) {
                h = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(name);
                if (h != null) {
                    return name;
                }
            }
            h = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
        }
        if (h == null) {
            messageInvalidActionItemName(name, "SignalHead"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check SignalMast reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Signal Mast, null if
     *         not found
     */
    String validateSignalMastReference(String name) {
        SignalMast h = null;
        if (name != null) {
            if (name.length() > 0) {
                h = InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName(name);
                if (h != null) {
                    return name;
                }
            }
            try {
                h = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(name);
            } catch (IllegalArgumentException ex) {
                h = null; // tested below
            }
        }
        if (h == null) {
            messageInvalidActionItemName(name, "SignalMast"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Warrant reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Warrant, null if not
     *         found
     */
    String validateWarrantReference(String name) {
        Warrant w = null;
        if (name != null) {
            if (name.length() > 0) {
                w = InstanceManager.getDefault(WarrantManager.class).getByUserName(name);
                if (w != null) {
                    return name;
                }
            }
            w = InstanceManager.getDefault(WarrantManager.class).getBySystemName(name);
        }
        if (w == null) {
            messageInvalidActionItemName(name, "Warrant"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check OBlock reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding OBlock, null if not
     *         found
     */
    String validateOBlockReference(String name) {
        OBlock b = null;
        if (name != null) {
            if (name.length() > 0) {
                b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getByUserName(name);
                if (b != null) {
                    return name;
                }
            }
            b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(name);
        }
        if (b == null) {
            messageInvalidActionItemName(name, "OBlock"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Sensor reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Sensor, null if not
     *         found
     */
    String validateSensorReference(String name) {
        Sensor s = null;
        if (name != null) {
            if (name.length() > 0) {
                s = InstanceManager.getDefault(jmri.SensorManager.class).getByUserName(name);
                if (s != null) {
                    return name;
                }
            }
            s = InstanceManager.getDefault(jmri.SensorManager.class).getBySystemName(name);
        }
        if (s == null) {
            messageInvalidActionItemName(name, "Sensor"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Light reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Light, null if not
     *         found
     */
    String validateLightReference(String name) {
        Light l = null;
        if (name != null) {
            if (name.length() > 0) {
                l = InstanceManager.lightManagerInstance().getByUserName(name);
                if (l != null) {
                    return name;
                }
            }
            l = InstanceManager.lightManagerInstance().getBySystemName(name);
        }
        if (l == null) {
            messageInvalidActionItemName(name, "Light"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Conditional reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Conditional, null if
     *         not found
     */
    String validateConditionalReference(String name) {
        Conditional c = null;
        if (name != null) {
            if (name.length() > 0) {
                c = _conditionalManager.getByUserName(name);
                if (c != null) {
                    return name;
                }
            }
            c = _conditionalManager.getBySystemName(name);
        }
        if (c == null) {
            messageInvalidActionItemName(name, "Conditional"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Logix reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Logix, null if not
     *         found
     */
    String validateLogixReference(String name) {
        Logix l = null;
        if (name != null) {
            if (name.length() > 0) {
                l = _logixManager.getByUserName(name);
                if (l != null) {
                    return name;
                }
            }
            l = _logixManager.getBySystemName(name);
        }
        if (l == null) {
            messageInvalidActionItemName(name, "Logix"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Route reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Route, null if not
     *         found
     */
    String validateRouteReference(String name) {
        Route r = null;
        if (name != null) {
            if (name.length() > 0) {
                r = InstanceManager.getDefault(jmri.RouteManager.class).getByUserName(name);
                if (r != null) {
                    return name;
                }
            }
            r = InstanceManager.getDefault(jmri.RouteManager.class).getBySystemName(name);
        }
        if (r == null) {
            messageInvalidActionItemName(name, "Route"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check an Audio reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding AudioManager, null
     *         if not found
     */
    String validateAudioReference(String name) {
        Audio a = null;
        if (name != null) {
            if (name.length() > 0) {
                a = InstanceManager.getDefault(jmri.AudioManager.class).getByUserName(name);
                if (a != null) {
                    return name;
                }
            }
            a = InstanceManager.getDefault(jmri.AudioManager.class).getBySystemName(name);
        }
        if (a == null || (a.getSubType() != Audio.SOURCE && a.getSubType() != Audio.LISTENER)) {
            messageInvalidActionItemName(name, "Audio"); // NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check an EntryExit reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system name of the corresponding EntryExit pair, null if not
     *         found
     */
    String validateEntryExitReference(String name) {
        NamedBean nb = null;
        if (name != null) {
            if (name.length() > 0) {
                nb = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(name);
                if (nb != null) {
                    return name;
                }
            }
        }
        messageInvalidActionItemName(name, "EntryExit"); // NOI18N
        return null;
    }

    /**
     * Get Light instance.
     * <p>
     * Show a message if not found.
     *
     * @param name user or system name of an existing light
     * @return the Light object
     */
    Light getLight(String name) {
        if (name == null) {
            return null;
        }
        Light l = null;
        if (name.length() > 0) {
            l = InstanceManager.lightManagerInstance().getByUserName(name);
            if (l != null) {
                return l;
            }
            l = InstanceManager.lightManagerInstance().getBySystemName(name);
        }
        if (l == null) {
            messageInvalidActionItemName(name, "Light"); //NOI18N
        }
        return l;
    }

    int parseTime(String s) {
        int nHour = 0;
        int nMin = 0;
        boolean error = false;
        int index = s.indexOf(':');
        String hour = null;
        String minute = null;
        try {
            if (index > 0) { // : after start
                hour = s.substring(0, index);
                if (index + 1 < s.length()) { // check for : at end
                    minute = s.substring(index + 1);
                } else {
                    minute = "0";
                }
            } else if (index == 0) { // : at start
                hour = "0";
                minute = s.substring(index + 1);
            } else {
                hour = s;
                minute = "0";
            }
        } catch (IndexOutOfBoundsException ioob) {
            error = true;
        }
        if (!error) {
            try {
                nHour = Integer.valueOf(hour);
                if ((nHour < 0) || (nHour > 24)) {
                    error = true;
                }
                nMin = Integer.valueOf(minute);
                if ((nMin < 0) || (nMin > 59)) {
                    error = true;
                }
            } catch (NumberFormatException e) {
                error = true;
            }
        }
        if (error) {
            // if unsuccessful, print error message
            javax.swing.JOptionPane.showMessageDialog(null,
                    java.text.MessageFormat.format(Bundle.getMessage("Error26"), // NOI18N
                            new Object[]{s}), Bundle.getMessage("ErrorTitle"), // NOI18N
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return (-1);
        }
        // here if successful
        return ((nHour * 60) + nMin);
    }

    /**
     * Format time to hh:mm given integer hour and minute.
     *
     * @param hour   value for time hours
     * @param minute value for time minutes
     * @return Formatted time string
     */
    public static String formatTime(int hour, int minute) {
        String s = "";
        String t = Integer.toString(hour);
        if (t.length() == 2) {
            s = t + ":";
        } else if (t.length() == 1) {
            s = "0" + t + ":";
        }
        t = Integer.toString(minute);
        if (t.length() == 2) {
            s = s + t;
        } else if (t.length() == 1) {
            s = s + "0" + t;
        }
        if (s.length() != 5) {
            // input error
            s = "00:00";
        }
        return s;
    }

    // ------------ Error Dialogs ------------
    /**
     * Send an Invalid Conditional SignalHead state message for Edit Logix pane.
     *
     * @param name       proposed appearance description
     * @param appearance to compare to
     */
    void messageInvalidSignalHeadAppearance(String name, String appearance) {
        javax.swing.JOptionPane.showMessageDialog(null,
                java.text.MessageFormat.format(Bundle.getMessage("Error21"), // NOI18N
                        new Object[]{name, appearance}), Bundle.getMessage("ErrorTitle"), // NOI18N
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Send an Invalid Conditional Action name message for Edit Logix pane.
     *
     * @param name     user or system name to look up
     * @param itemType type of Bean to look for
     */
    void messageInvalidActionItemName(String name, String itemType) {
        javax.swing.JOptionPane.showMessageDialog(null,
                java.text.MessageFormat.format(Bundle.getMessage("Error22"), // NOI18N
                        new Object[]{name, Bundle.getMessage("BeanName" + itemType)}), Bundle.getMessage("ErrorTitle"), // NOI18N
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Send a duplicate Conditional user name message for Edit Logix pane.
     *
     * @param svName proposed name that duplicates an existing name
     */
    void messageDuplicateConditionalUserName(String svName) {
        javax.swing.JOptionPane.showMessageDialog(null,
                java.text.MessageFormat.format(Bundle.getMessage("Error30"), // NOI18N
                        new Object[]{svName}), Bundle.getMessage("ErrorTitle"), // NOI18N
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    protected String getClassName() {
        return ConditionalEditBase.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalEditBase.class);
}
