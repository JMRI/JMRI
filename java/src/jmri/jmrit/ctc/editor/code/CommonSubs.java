package jmri.jmrit.ctc.editor.code;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;
import jmri.InstanceManager;
import jmri.BlockManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.TurnoutManager;
import jmri.jmrit.ctc.CtcManager;
import jmri.jmrit.ctc.NBHSensor;
import jmri.jmrit.ctc.NBHSignal;
import jmri.jmrit.ctc.NBHTurnout;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CommonSubs {

//  For GUI editor routines that need this:
    public static void setMillisecondsEdit(JFormattedTextField formattedTextField) {
        NumberFormatter numberFormatter = (NumberFormatter) formattedTextField.getFormatter();
        numberFormatter.setMinimum(0);
        numberFormatter.setMaximum(120000);
        numberFormatter.setAllowsInvalid(false);
    }

//  This routine will return 0 if error encountered parsing string.  Since all GUI int fields that use this
//  routine already should have a NumberFormatter attached to it already (see "setMillisecondsEdit" above),
//  technically these should NEVER throw!
    public static int getIntFromJTextFieldNoThrow(JTextField textField) { return ProjectsCommonSubs.getIntFromStringNoThrow(textField.getText(), 0); }

    public static boolean allowClose(Component parentComponent, boolean dataChanged) {
        if (dataChanged) {
            return JOptionPane.showConfirmDialog(parentComponent, Bundle.getMessage("CommonSubsDataModified"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;  // NOI18N
        }
        return true;    // NO change, ok to exit
    }

    /**
     * Add a standard help menu, including the window specific help item.
     *
     * @param frame  The frame receiving the help menu.
     * @param ref    JHelp reference for the desired window-specific help page
     * @param direct true if the help main-menu item goes directly to the help system,
     *               such as when there are no items in the help menu
     */
    public static void addHelpMenu(javax.swing.JFrame frame, String ref, boolean direct) {
        javax.swing.JMenuBar bar = frame.getJMenuBar();
        if (bar == null) {
            bar = new javax.swing.JMenuBar();
        }
        jmri.util.HelpUtil.helpMenu(bar, ref, direct);
        frame.setJMenuBar(bar);
    }

//  If the table model value is null, that is the same as "".  This also "compacts"
//  the entries also (i.e. blank line(s) between entries are removed):
    public static String getCSVStringFromDefaultTableModel(DefaultTableModel defaultTableModel) {
        ArrayList<String> entries = new ArrayList<>();
        for (int sourceIndex = 0; sourceIndex < defaultTableModel.getRowCount(); sourceIndex++) {
            Object object = defaultTableModel.getValueAt(sourceIndex, 0);
            if (object != null) {
                String entry = object.toString().trim();
                if (!entry.isEmpty()) { // Do a "compact" on the fly:
                    entries.add(entry);
                }
            }
        }
        try (CSVPrinter printer = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT.withQuote(null).withRecordSeparator(null))) {
            printer.printRecord(entries);
            return printer.getOut().toString();
        } catch (IOException ex) {
            log.error("Unable to create CSV", ex);
            return "";
        }
    }

//  If the table model value is null, that is the same as "".  This also "compacts"
//  the entries also (i.e. blank line(s) between entries are removed):
    public static ArrayList<String> getStringArrayFromDefaultTableModel(DefaultTableModel defaultTableModel) {
        ArrayList<String> entries = new ArrayList<>();
        for (int sourceIndex = 0; sourceIndex < defaultTableModel.getRowCount(); sourceIndex++) {
            Object object = defaultTableModel.getValueAt(sourceIndex, 0);
            if (object != null) {
                String entry = object.toString().trim();
                if (!entry.isEmpty()) { // Do a "compact" on the fly:
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    public static int compactDefaultTableModel(DefaultTableModel defaultTableModel) {
        int destIndex = 0;
        int lastSourceIndexNonEmpty = -1;   // Indicate none found
        int count = 0;
        for (int sourceIndex = 0; sourceIndex < defaultTableModel.getRowCount(); sourceIndex++) {
            Object object = defaultTableModel.getValueAt(sourceIndex, 0);
            if (object != null) {
                String entry = object.toString().trim();
                entry = entry.trim();
                if (!entry.isEmpty()) {
                    lastSourceIndexNonEmpty = sourceIndex;
                    defaultTableModel.setValueAt(entry, destIndex++, 0);
                    count++;
                }
            }
        }
        if (-1 != lastSourceIndexNonEmpty) { // Something in table, MAY need to clear out rows at end:
            while (destIndex <= lastSourceIndexNonEmpty) {
                defaultTableModel.setValueAt("", destIndex++, 0);
            }
        }
        return count; // Return number of entries encountered.
    }

//  This creates a sorted ArrayList (so that we can easily load it into a "DefaultComboBoxModel") containing all
//  Switch Direction Indicators:
    public static ArrayList<String> getArrayListOfSelectableSwitchDirectionIndicators(ArrayList<CodeButtonHandlerData> codeButtonHandlerDataList) {
        ArrayList<String> returnValue = new ArrayList<>();
        for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataList) {
            if (!codeButtonHandlerData._mSWDI_NormalInternalSensor.getHandleName().isEmpty()) {
                returnValue.add(codeButtonHandlerData._mSWDI_NormalInternalSensor.getHandleName());
            }
            if (!codeButtonHandlerData._mSWDI_ReversedInternalSensor.getHandleName().isEmpty()) {
                returnValue.add(codeButtonHandlerData._mSWDI_ReversedInternalSensor.getHandleName());
            }
        }
//      Collections.sort(returnValue);
        return returnValue;
    }

    public static ArrayList<Integer> getArrayListOfSelectableOSSectionUniqueIDs(ArrayList<CodeButtonHandlerData> codeButtonHandlerDataList) {
        ArrayList<Integer> returnValue = new ArrayList<>();
        for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataList) {
            returnValue.add(codeButtonHandlerData._mUniqueID);
        }
        return returnValue;
    }

    public static void populateJComboBoxWithColumnDescriptionsAndSelectViaUniqueID(JComboBox<String> jComboBox, CTCSerialData ctcSerialData, int uniqueID) {
        populateJComboBoxWithColumnDescriptions(jComboBox, ctcSerialData);
        setSelectedIndexOfJComboBoxViaUniqueID(jComboBox, ctcSerialData, uniqueID);
    }

//  A blank entry will ALWAYS appear as the first selection.
    public static void populateJComboBoxWithColumnDescriptions(JComboBox<String> jComboBox, CTCSerialData ctcSerialData) {
        ArrayList<String> userDescriptions = new ArrayList<>();
        userDescriptions.add("");   // None can be specified.
        ArrayList<Integer> arrayListOfSelectableOSSectionUniqueIDs = getArrayListOfSelectableOSSectionUniqueIDs(ctcSerialData.getCodeButtonHandlerDataArrayList());
        for (Integer uniqueID : arrayListOfSelectableOSSectionUniqueIDs) {
            userDescriptions.add(ctcSerialData.getMyShortStringNoCommaViaUniqueID(uniqueID));
        }
//      Collections.sort(userDescriptions);
        jComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(userDescriptions)));
    }

//  NO blank entry as the first selection, returns true if any in list, else false.
//  Also populates "uniqueIDS" with corresponding values.
    public static boolean populateJComboBoxWithColumnDescriptionsExceptOurs(JComboBox<String> jComboBox, CTCSerialData ctcSerialData, int ourUniqueID, ArrayList<Integer> uniqueIDS) {
        ArrayList<String> userDescriptions = new ArrayList<>();
        uniqueIDS.clear();
        ArrayList<Integer> arrayListOfSelectableOSSectionUniqueIDs = getArrayListOfSelectableOSSectionUniqueIDs(ctcSerialData.getCodeButtonHandlerDataArrayList());
        for (Integer uniqueID : arrayListOfSelectableOSSectionUniqueIDs) {
            if (ourUniqueID != uniqueID) {
                userDescriptions.add(ctcSerialData.getMyShortStringNoCommaViaUniqueID(uniqueID));
                uniqueIDS.add(uniqueID);
            }
        }
//      Collections.sort(userDescriptions);
        jComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(userDescriptions)));
        return !userDescriptions.isEmpty();
    }

    /**
     * Populate a combo box with bean names using getDisplayName().
     * <p>
     * If a panel xml file has not been loaded, the combo box will behave as a
     * text field (editable), otherwise it will behave as standard combo box (not editable).
     * @param jComboBox The string based combo box to be populated.
     * @param beanType The bean type to be loaded.  It has to be in the switch list.
     * @param currentSelection The current item to be selected, none if null.
     * @param firstRowBlank True to create a blank row. If the selection is null or empty, the blank row will be selected.
     */
    public static void populateJComboBoxWithBeans(JComboBox<String> jComboBox, String beanType, String currentSelection, boolean firstRowBlank) {
        jComboBox.removeAllItems();
        jComboBox.setEditable(false);
        ArrayList<String> list = new ArrayList<>();
        switch (beanType) {
            case "Sensor":  // NOI18N
                InstanceManager.getDefault(SensorManager.class).getNamedBeanSet().forEach((s) -> {
                    list.add(s.getDisplayName());
                });
                break;
            case "Turnout": // NOI18N
                InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().forEach((t) -> {
                    list.add(t.getDisplayName());
                });
                break;
            case "SignalHead":  // NOI18N
                InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().forEach((h) -> {
                    list.add(h.getDisplayName());
                });
                break;
            case "SignalMast":  // NOI18N
                InstanceManager.getDefault(SignalMastManager.class).getNamedBeanSet().forEach((m) -> {
                    list.add(m.getDisplayName());
                });
                break;
            case "Block":   // NOI18N
                InstanceManager.getDefault(BlockManager.class).getNamedBeanSet().forEach((b) -> {
                    list.add(b.getDisplayName());
                });
                break;
            default:
                log.error(Bundle.getMessage("CommonSubsBeanType"), beanType);   // NOI18N
        }
        list.sort(new jmri.util.AlphanumComparator());
        list.forEach((item) -> {
            jComboBox.addItem(item);
        });
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(jComboBox);
        jComboBox.setSelectedItem(currentSelection);
        if (firstRowBlank) {
            jComboBox.insertItemAt("", 0);
            if (currentSelection == null || currentSelection.isEmpty()) {
                jComboBox.setSelectedIndex(0);
            }
        }
    }

    public static void setSelectedIndexOfJComboBoxViaUniqueID(JComboBox<String> jComboBox, CTCSerialData ctcSerialData, int uniqueID) {
        int index = ctcSerialData.getIndexOfUniqueID(uniqueID) + 1;   // Can be -1 if not found, index becomes 0, which is spaces!
        jComboBox.setSelectedIndex(index);
    }

/*  Someday I'll create a better "ButtonGroup" than provided.  Until then:

    Cheat: We know that the implementation of ButtonGroup uses a Vector when elements
    are added to it.  Therefore the order is guaranteed.  Check your individual order
    by searching for all X.add (where X is the ButtonGroup variable name).
    This routine will "number" each button in order using the "setActionCommand".
    Then you can switch on either this string by doing:
    switch (getButtonSelectedString(X))
        case "0":
            break;
        case "1":
    ....
    Or faster CPU wise:
    switch (getButtonSelectedInt(X))
        case 0:
    ....
*/
    public static void numberButtonGroup(ButtonGroup buttonGroup) {
        int entry = 0;
        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            button.setActionCommand(Integer.toString(entry++));
        }
    }

    public static void setButtonSelected(ButtonGroup buttonGroup, int selected) {
        ArrayList<AbstractButton> buttons = Collections.list(buttonGroup.getElements());
        if (buttons.isEmpty()) return;    // Safety: The moron forgot to put radio buttons into this group!  Don't select any!
        if (selected < 0 || selected >= buttons.size()) selected = 0;   // Default is zero if you pass an out of range value.
        AbstractButton buttonSelected = buttons.get(selected);
        buttonSelected.setSelected(true);
//  Be consistent, when set, do this also:
        ActionEvent actionEvent = new ActionEvent(buttonSelected, ActionEvent.ACTION_PERFORMED, buttonSelected.getActionCommand());
        for (ActionListener actionListener : buttonSelected.getActionListeners()) {
            actionListener.actionPerformed(actionEvent);
        }
    }

//  If the passed errors array has entries, put up a dialog and return true, if not no dialog, and return false.
    public static boolean missingFieldsErrorDialogDisplayed(Component parentComponent, ArrayList<String> errors, boolean isCancel) {
        if (errors.isEmpty()) return false;
        StringBuilder stringBuffer = new StringBuilder(errors.size() > 1 ? Bundle.getMessage("CommonSubsFieldsPlural") : Bundle.getMessage("CommonSubsFieldSingular"));     // NOI18N
        errors.forEach(error -> stringBuffer.append(error).append("\n")); // NOI18N
        if (!isCancel) {
            stringBuffer.append(Bundle.getMessage("CommonSubsPleaseFix1")); // NOI18N
        } else {
            stringBuffer.append(Bundle.getMessage("CommonSubsPleaseFix2")); // NOI18N
        }
        JOptionPane.showMessageDialog(parentComponent, stringBuffer.toString(),
                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);   // NOI18N
        return true;
    }

//  Simple sub to see if field is not empty.  If empty, then it takes the prompt text and adds it to the end of the errors array.
    public static void checkJTextFieldNotEmpty(javax.swing.JTextField field, javax.swing.JLabel promptName, ArrayList<String> errors) {
        if (!isJTextFieldNotEmpty(field)) errors.add(promptName.getText());
    }

    public static boolean isJTextFieldNotEmpty(javax.swing.JTextField field) {
        return !(field.getText().trim().isEmpty());
    }

//  Simple sub to see if combo selection is not empty.  If empty, then it takes the prompt text and adds it to the end of the errors array.
    public static void checkJComboBoxNotEmpty(javax.swing.JComboBox<String> combo, javax.swing.JLabel promptName, ArrayList<String> errors) {
        if (!isJComboBoxNotEmpty(combo)) errors.add(promptName.getText());
    }

    public static boolean isJComboBoxNotEmpty(javax.swing.JComboBox<String> combo) {
        return !((String) combo.getSelectedItem()).trim().isEmpty();
    }

    /**
     * Get a NBHSensor from the CtcManager NBHSensor map or create a new one.
     * @param newName The new name to be retrieved from the map or created.
     * @param isInternal True if an internal sensor is being requested.  Internal will create the sensor if necessary using provide(String).
     * @return a NBHSensor or null.
     */
    public static NBHSensor getNBHSensor(String newName, boolean isInternal) {
        NBHSensor sensor = null;
        if (!ProjectsCommonSubs.isNullOrEmptyString(newName)) {
            sensor = InstanceManager.getDefault(CtcManager.class).getNBHSensor(newName);
            if (sensor == null) {
                if (isInternal) {
                    sensor = new NBHSensor("CommonSubs", "new sensor = ", newName, newName);
                } else {
                    sensor = new NBHSensor("CommonSubs", "new sensor = ", newName, newName, false);
                }
            }
        }
        return sensor;
    }

    /**
     * Get a NBHTurnout from the CtcManager NBHTurnout map or create a new one.
     * @param newName The new name to be retrieved from the map or created.
     * @param feedbackDifferent The feedback different state.
     * @return a valid NBHTurnout or an empty NBHTurnout.
     */
    public static NBHTurnout getNBHTurnout(String newName, boolean feedbackDifferent) {
        NBHTurnout turnout = null;
        if (!ProjectsCommonSubs.isNullOrEmptyString(newName)) {
            turnout = InstanceManager.getDefault(CtcManager.class).getNBHTurnout(newName);
            if (turnout == null) {
                turnout = new NBHTurnout("CommonSubs", "new turnout = ", newName, newName, feedbackDifferent);
            }
        }
        if (turnout == null) {
            // Create a dummy NBHTurnout
            turnout = new NBHTurnout("CommonSubs", "Empty turnout", "");
        }
        return turnout;
    }

    /**
     * Get a NBHSignal from the CtcManager NBHSignal map or create a new one.
     * @param newName The new name to be retrieved from the map or created.
     * @return a valid NBHSignal or null.
     */
    public static NBHSignal getNBHSignal(String newName) {
        NBHSignal signal = null;
        if (!ProjectsCommonSubs.isNullOrEmptyString(newName)) {
            signal = InstanceManager.getDefault(CtcManager.class).getNBHSignal(newName);
            if (signal == null) {
                signal = new NBHSignal(newName);
            }
        }
        return signal;
    }

    /**
     * Add a valid NBHSensor entry to an ArrayList.  The sensor name has to match an existing
     * sensor in the JMRI sensor table.
     * @param list The NBHSensor array list.
     * @param sensorName The proposed sensor name.
     */
    public static void addSensorToSensorList(ArrayList<NBHSensor> list, String sensorName) {
        NBHSensor sensor = getNBHSensor(sensorName, false);
        if (sensor != null && sensor.valid()) list.add(sensor);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommonSubs.class);
}
