package jmri.jmrit.ctc.editor.code;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.TurnoutManager;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

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
        String returnString = "";
        for (int sourceIndex = 0; sourceIndex < defaultTableModel.getRowCount(); sourceIndex++) {
            Object object = defaultTableModel.getValueAt(sourceIndex, 0);
            if (object != null) {
                String entry = object.toString().trim();
                if (!entry.isEmpty()) { // Do a "compact" on the fly:
                    if (!returnString.isEmpty()) {
                        returnString += ProjectsCommonSubs.CSV_SEPARATOR + entry;
                    } else {
                        returnString = entry;
                    }
                }
            }
        }
        return returnString;
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
            if (!codeButtonHandlerData._mSWDI_NormalInternalSensor.isEmpty()) {
                returnValue.add(codeButtonHandlerData._mSWDI_NormalInternalSensor);
            }
            if (!codeButtonHandlerData._mSWDI_ReversedInternalSensor.isEmpty()) {
                returnValue.add(codeButtonHandlerData._mSWDI_ReversedInternalSensor);
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
        boolean panelLoaded = InstanceManager.getDefault(jmri.jmrit.ctc.editor.gui.FrmMainForm.class)._mPanelLoaded;
        jComboBox.removeAllItems();
        if (!panelLoaded) {
            // Configure combo box as a pseudo text field
            jComboBox.setEditable(true);
            jComboBox.addItem(currentSelection == null ? "" : currentSelection);
            return;
        }
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

//  Or you can get the actual string that the user sees in the button (or null if you screwed up by
//  allowing all buttons to be non-selected, for instance if you didn't select one by default).
    public static String getButtonSelectedText(ButtonGroup buttonGroup) {
        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) return button.getText();
        }
        return null;
    }

//  If the passed errors array has entries, put up a dialog and return true, if not no dialog, and return false.
    public static boolean missingFieldsErrorDialogDisplayed(Component parentComponent, ArrayList<String> errors, boolean isCancel) {
        if (errors.isEmpty()) return false;
        StringBuffer stringBuffer = new StringBuffer(errors.size() > 1 ? Bundle.getMessage("CommonSubsFieldsPlural") : Bundle.getMessage("CommonSubsFieldSingular"));     // NOI18N
        for (String error : errors) {
            stringBuffer.append(error);
            stringBuffer.append("\n");    // NOI18N
        }
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

//  Returns the directory only of a directory + filename combination.  The return
//  string has a file separator at the end, so that filenames can just be appended to it.
    public static String getDirectoryOnly(String directoryAndFilename) {
        File file = new File(directoryAndFilename);
        String parent = file.getParent();   // Returns "null" if no parent.
        if (ProjectsCommonSubs.isNullOrEmptyString(parent)) return "";
        return file.getParent() + File.separator;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommonSubs.class);

}
