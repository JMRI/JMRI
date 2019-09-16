package jmri.jmrit.ctc.editor.code;

import jmri.jmrit.ctc.editor.gui.FrmCB;
import jmri.jmrit.ctc.editor.gui.FrmCO;
import jmri.jmrit.ctc.editor.gui.FrmIL;
import jmri.jmrit.ctc.editor.gui.FrmSIDI;
import jmri.jmrit.ctc.editor.gui.FrmSIDL;
import jmri.jmrit.ctc.editor.gui.FrmSWDI;
import jmri.jmrit.ctc.editor.gui.FrmSWDL;
import jmri.jmrit.ctc.editor.gui.FrmTRL;
import jmri.jmrit.ctc.editor.gui.FrmTUL;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import jmri.jmrit.ctc.ctcserialdata.TrafficLockingEntry;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019

 This represents all of the codeButtonHandlerData data in a CTC machine relating to the GUI
 interface.  It maintains the state of the screen for the higher level functions.
 */
public class Columns {
    public final static String REFERENCES_PRESENT_INDICATOR = " (";
    private final static String ERROR_STRING = " ***ERROR***";

    private final CTCSerialData _mCTCSerialData;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final DefaultListModel<String> _mDefaultListModel;
    private final JButton _mDeleteButton;
    private final JButton _mReapplyNumbersButton;
    private final JButton _mChangeNumbersButton;
    private final JButton _mMoveUpButton;
    private final JButton _mMoveDownButton;
    private final JLabel _mEdit_CB_Prompt;
    private final JLabel _mCB_EditAlwaysEnabled;
    private final JButton _mEdit_CB;
    private final JLabel _mEdit_SIDI_Prompt;
    private final JCheckBox _mSIDI_Enabled;
    private final JButton _mEdit_SIDI;
    private final JLabel _mEdit_SIDL_Prompt;
    private final JCheckBox _mSIDL_Enabled;
    private final JButton _mEdit_SIDL;
    private final JLabel _mEdit_SWDI_Prompt;
    private final JCheckBox _mSWDI_Enabled;
    private final JButton _mEdit_SWDI;
    private final JLabel _mEdit_SWDL_Prompt;
    private final JCheckBox _mSWDL_Enabled;
    private final JButton _mEdit_SWDL;
    private final JLabel _mEdit_CO_Prompt;
    private final JCheckBox _mCO_Enabled;
    private final JButton _mEdit_CO;
    private final JLabel _mEdit_TRL_Prompt;
    private final JCheckBox _mTRL_Enabled;
    private final JButton _mEdit_TRL;
    private final JLabel _mEdit_TUL_Prompt;
    private final JCheckBox _mTUL_Enabled;
    private final JButton _mEdit_TUL;
    private final JLabel _mEdit_IL_Prompt;
    private final JCheckBox _mIL_Enabled;
    private final JButton _mEdit_IL;
    private int _mSelectedCodeButtonHandlerDataIndex;
    private CodeButtonHandlerData _mSelectedCodeButtonHandlerData;

    public Columns( CTCSerialData ctcSerialData, CheckJMRIObject checkJMRIObject, DefaultListModel<String> defaultListModel,
                    JButton deleteButton, JButton reapplyNumbersButton, JButton changeNumbersButton,
                    JButton moveUpButton, JButton moveDownButton,
                    JLabel edit_CB_Prompt, JLabel cb_EditAlwaysEnabled, JButton edit_CB,
                    JLabel edit_SIDI_Prompt, JCheckBox sidi_Enabled,  JButton edit_SIDI,
                    JLabel edit_SIDL_Prompt, JCheckBox sidl_Enabled,  JButton edit_SIDL,
                    JLabel edit_SWDI_Prompt, JCheckBox swdi_Enabled,  JButton edit_SWDI,
                    JLabel edit_SWDL_Prompt, JCheckBox swdl_Enabled,  JButton edit_SWDL,
                    JLabel edit_CO_Prompt, JCheckBox co_Enabled,  JButton edit_CO,
                    JLabel edit_TRL_Prompt,  JCheckBox trl_Enabled,  JButton edit_TRL,
                    JLabel edit_TUL_Prompt, JCheckBox tul_Enabled,  JButton edit_TUL,
                    JLabel edit_IL_Prompt, JCheckBox il_Enabled,  JButton edit_IL) {
        _mCTCSerialData = ctcSerialData;
        _mCheckJMRIObject = checkJMRIObject;
        _mDefaultListModel = defaultListModel;
        _mDeleteButton = deleteButton;
        _mReapplyNumbersButton = reapplyNumbersButton;
        _mChangeNumbersButton = changeNumbersButton;
        _mMoveUpButton = moveUpButton;
        _mMoveDownButton = moveDownButton;
        _mEdit_CB_Prompt = edit_CB_Prompt;
        _mCB_EditAlwaysEnabled = cb_EditAlwaysEnabled;
        _mEdit_CB = edit_CB;
        _mEdit_SIDI_Prompt = edit_SIDI_Prompt;
        _mSIDI_Enabled = sidi_Enabled;
        _mEdit_SIDI = edit_SIDI;
        _mEdit_SIDL_Prompt = edit_SIDL_Prompt;
        _mSIDL_Enabled = sidl_Enabled;
        _mEdit_SIDL = edit_SIDL;
        _mEdit_SWDI_Prompt = edit_SWDI_Prompt;
        _mSWDI_Enabled = swdi_Enabled;
        _mEdit_SWDI = edit_SWDI;
        _mEdit_SWDL_Prompt = edit_SWDL_Prompt;
        _mSWDL_Enabled = swdl_Enabled;
        _mEdit_SWDL = edit_SWDL;
        _mEdit_CO_Prompt = edit_CO_Prompt;
        _mCO_Enabled = co_Enabled;
        _mEdit_CO = edit_CO;
        _mEdit_TRL_Prompt = edit_TRL_Prompt;
        _mTRL_Enabled = trl_Enabled;
        _mEdit_TRL = edit_TRL;
        _mTUL_Enabled = tul_Enabled;
        _mEdit_TUL_Prompt = edit_TUL_Prompt;
        _mEdit_TUL = edit_TUL;
        _mEdit_IL_Prompt = edit_IL_Prompt;
        _mIL_Enabled = il_Enabled;
        _mEdit_IL = edit_IL;
        updateFrame();
    }

    public CodeButtonHandlerData getSelectedCodeButtonHandlerData() { return _mSelectedCodeButtonHandlerData; }

    public final void updateFrame() {
        _mDefaultListModel.clear();
        _mCTCSerialData.getCodeButtonHandlerDataArrayList().forEach((codeButtonHandlerData) -> {
            _mDefaultListModel.addElement(constructSingleColumnDisplayLine(codeButtonHandlerData));
        });
        _mDeleteButton.setEnabled(false);    // None selected.
        _mReapplyNumbersButton.setEnabled(false);
        _mChangeNumbersButton.setEnabled(false);
        _mMoveUpButton.setEnabled(false);
        _mMoveDownButton.setEnabled(false);
        _mSIDI_Enabled.setEnabled(false);
        _mCB_EditAlwaysEnabled.setEnabled(false);
        _mEdit_CB.setEnabled(false);
        _mEdit_SIDI.setEnabled(false);
        _mSIDL_Enabled.setEnabled(false);
        _mEdit_SIDL.setEnabled(false);
        _mSWDI_Enabled.setEnabled(false);
        _mEdit_SWDI.setEnabled(false);
        _mSWDL_Enabled.setEnabled(false);
        _mEdit_SWDL.setEnabled(false);
        _mCO_Enabled.setEnabled(false);
        _mEdit_CO.setEnabled(false);
        _mTRL_Enabled.setEnabled(false);
        _mEdit_TRL.setEnabled(false);
        _mTUL_Enabled.setEnabled(false);
        _mEdit_TUL.setEnabled(false);
        _mIL_Enabled.setEnabled(false);
        _mEdit_IL.setEnabled(false);
    }

    public int getEntrySelectedIndex() { return _mSelectedCodeButtonHandlerDataIndex; }

    public void fixAllErrors() {
        for (CodeButtonHandlerData codeButtonHandlerData : _mCTCSerialData.getCodeButtonHandlerDataArrayList()) {
            if (!FrmSIDI.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mSIDI_Enabled = false;
            if (!FrmSIDL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mSIDL_Enabled = false;
            if (!FrmSWDI.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mSWDI_Enabled = false;
            if (!FrmSWDL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mSWDL_Enabled = false;
            if (!FrmCO.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mCO_Enabled = false;
            if (!FrmTRL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mTRL_Enabled = false;
            if (!FrmTUL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mTUL_Enabled = false;
            if (!FrmIL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, codeButtonHandlerData)) codeButtonHandlerData._mIL_Enabled = false;
        }
        updateFrame();
    }

    public boolean anyErrorsPresent() {
        Enumeration<String> enumerationOfStrings = _mDefaultListModel.elements();
        while (enumerationOfStrings.hasMoreElements()) {
            if (enumerationOfStrings.nextElement().contains(ERROR_STRING)) return true;
        }
        return false;
    }

    public void updateCurrentlySelectedColumnErrorStatus() {
        lazy1(_mEdit_CB_Prompt, FrmCB.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_SIDI_Prompt, FrmSIDI.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_SIDL_Prompt, FrmSIDL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_SWDI_Prompt, FrmSWDI.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_SWDL_Prompt, FrmSWDL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_CO_Prompt, FrmCO.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_TRL_Prompt, FrmTRL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_TUL_Prompt, FrmTUL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        lazy1(_mEdit_IL_Prompt, FrmIL.dialogCodeButtonHandlerDataValid(_mCheckJMRIObject, _mSelectedCodeButtonHandlerData) ? Color.black : Color.red);
        _mDefaultListModel.set(_mSelectedCodeButtonHandlerDataIndex, constructSingleColumnDisplayLine(_mSelectedCodeButtonHandlerData));
    }

    public void clearCurrentlySelectedColumnErrorStatus() {
        lazy1(_mEdit_CB_Prompt, Color.black);
        lazy1(_mEdit_SIDI_Prompt, Color.black);
        lazy1(_mEdit_SIDL_Prompt, Color.black);
        lazy1(_mEdit_SWDI_Prompt, Color.black);
        lazy1(_mEdit_SWDL_Prompt, Color.black);
        lazy1(_mEdit_CO_Prompt, Color.black);
        lazy1(_mEdit_TRL_Prompt, Color.black);
        lazy1(_mEdit_TUL_Prompt, Color.black);
        lazy1(_mEdit_IL_Prompt, Color.black);
    }

    private static void lazy1(JLabel label, Color foreground) {
        label.setForeground(foreground);
    }

    public void setEntrySelected(int selectedIndex) {
        if (selectedIndex >= 0) {
            _mDeleteButton.setEnabled(true);
            _mReapplyNumbersButton.setEnabled(true);
            _mChangeNumbersButton.setEnabled(true);
            _mMoveUpButton.setEnabled(selectedIndex > 0);
            _mMoveDownButton.setEnabled(selectedIndex < _mCTCSerialData.getCodeButtonHandlerDataSize() - 1);
            _mSelectedCodeButtonHandlerDataIndex = selectedIndex;
            _mSelectedCodeButtonHandlerData = _mCTCSerialData.getCodeButtonHandlerData(selectedIndex);
            _mCB_EditAlwaysEnabled.setEnabled(true);
            updateCurrentlySelectedColumnErrorStatus();
            _mEdit_CB.setEnabled(true);
            lazy1(_mSIDI_Enabled, _mEdit_SIDI, _mSelectedCodeButtonHandlerData._mSIDI_Enabled);
            lazy1(_mSIDL_Enabled, _mEdit_SIDL, _mSelectedCodeButtonHandlerData._mSIDL_Enabled);
            lazy1(_mSWDI_Enabled, _mEdit_SWDI, _mSelectedCodeButtonHandlerData._mSWDI_Enabled);
            lazy1(_mSWDL_Enabled, _mEdit_SWDL, _mSelectedCodeButtonHandlerData._mSWDL_Enabled);
            lazy1(_mCO_Enabled, _mEdit_CO, _mSelectedCodeButtonHandlerData._mCO_Enabled);
            lazy1(_mTRL_Enabled, _mEdit_TRL, _mSelectedCodeButtonHandlerData._mTRL_Enabled);
            lazy1(_mTUL_Enabled, _mEdit_TUL, _mSelectedCodeButtonHandlerData._mTUL_Enabled);
            lazy1(_mIL_Enabled, _mEdit_IL, _mSelectedCodeButtonHandlerData._mIL_Enabled);
        } else {
            clearCurrentlySelectedColumnErrorStatus();
        }
    }

    static private void lazy1(JCheckBox jCheckBox, JButton jButton, boolean value) {
        jCheckBox.setEnabled(true);
        jCheckBox.setSelected(value);
        jButton.setEnabled(value);
    }

    public void sidi_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mSIDI_Enabled = newState;
        lazy2(_mEdit_SIDI, newState);
    }

    public void sidl_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mSIDL_Enabled = newState;
        lazy2(_mEdit_SIDL, newState);
    }

    public void swdi_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mSWDI_Enabled = newState;
        lazy2(_mEdit_SWDI, newState);
    }

    public void swdl_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mSWDL_Enabled = newState;
        lazy2(_mEdit_SWDL, newState);
    }

    public void co_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mCO_Enabled = newState;
        lazy2(_mEdit_CO, newState);
    }

    public void trl_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mTRL_Enabled = newState;
        lazy2(_mEdit_TRL, newState);
    }

    public void tul_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mTUL_Enabled = newState;
        lazy2(_mEdit_TUL, newState);
    }

    public void il_EnabledClicked(boolean newState) {
        _mSelectedCodeButtonHandlerData._mIL_Enabled = newState;
        lazy2(_mEdit_IL, newState);
    }

    private void lazy2(JButton jButton, boolean value) {
        jButton.setEnabled(value);
        _mCTCSerialData.setCodeButtonHandlerData(_mSelectedCodeButtonHandlerDataIndex, _mSelectedCodeButtonHandlerData);
        updateCurrentlySelectedColumnErrorStatus();
    }

    public String checkForDups(int newSwitchNumber, int newGUIColumnNumber, boolean isModify, int indexModifying) {
        ArrayList <CodeButtonHandlerData> codeButtonHandlerDataList = _mCTCSerialData.getCodeButtonHandlerDataArrayList();
        int codeButtonHandlerDataListSize = codeButtonHandlerDataList.size();
        for (int index = 0; index < codeButtonHandlerDataListSize; index++) {
            if (!isModify || index != indexModifying) {  // If add, check all, if modify, check all but indexModifying.
                CodeButtonHandlerData codeButtonHandlerData = codeButtonHandlerDataList.get(index);
                if (codeButtonHandlerData._mSwitchNumber == newSwitchNumber) return "Switch #" + newSwitchNumber + " already used";
                if (newGUIColumnNumber > 0) { // Multiple 0's are allowed here:
                    if (codeButtonHandlerData._mGUIColumnNumber == newGUIColumnNumber) return "GUI Column #" + newGUIColumnNumber + " already used";
                }
            }
        }
        return null;
    }

    private String getListOfTrafficLockingRulesOSSectionsReferenced(CodeButtonHandlerData currentCodeButtonHandlerData,
                                                                    ArrayList <CodeButtonHandlerData> codeButtonHandlerDataArrayList) {
        StringBuffer returnStringBuffer = new StringBuffer("");
        TreeSet<String> temp = new TreeSet<>();
        int currentUniqueID = currentCodeButtonHandlerData._mUniqueID;
        for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataArrayList) {
            if (currentCodeButtonHandlerData != codeButtonHandlerData) { // Don't check ourselves
                int otherUniqueID = codeButtonHandlerData._mUniqueID;
                checkThisSSVList(currentUniqueID, otherUniqueID, "L", codeButtonHandlerData._mTRL_LeftTrafficLockingRulesSSVList, temp);    //NOI18N
                checkThisSSVList(currentUniqueID, otherUniqueID, "R", codeButtonHandlerData._mTRL_RightTrafficLockingRulesSSVList, temp);   //NOI18N
            }
        }
        for (String result : temp) returnStringBuffer.append(result);
        if (returnStringBuffer.length() > 0) {
            return "TrL: " + returnStringBuffer.substring(0, returnStringBuffer.length() - 2);    //NOI18N
        } else {
            return "";
        }
//      if (returnStringBuffer.length() > 0) returnStringBuffer.append("TrL: " + returnStringBuffer.substring(0, returnStringBuffer.length() - 2));       //NOI18N
//      return returnStringBuffer.toString();
    }

    private void checkThisSSVList(int ourUniqueID, int otherUniqueID, String lr, String trafficLockingRulesSSVList, TreeSet<String> setOfUniqueIDs) {
        for (String trafficLockingRulesSSV : ProjectsCommonSubs.getArrayListFromSSV(trafficLockingRulesSSVList)) {
            TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(trafficLockingRulesSSV);
            lazy3(ourUniqueID, otherUniqueID, lr, trafficLockingEntry._mUniqueID1, setOfUniqueIDs);
            lazy3(ourUniqueID, otherUniqueID, lr, trafficLockingEntry._mUniqueID2, setOfUniqueIDs);
            lazy3(ourUniqueID, otherUniqueID, lr, trafficLockingEntry._mUniqueID3, setOfUniqueIDs);
            lazy3(ourUniqueID, otherUniqueID, lr, trafficLockingEntry._mUniqueID4, setOfUniqueIDs);
            lazy3(ourUniqueID, otherUniqueID, lr, trafficLockingEntry._mUniqueID5, setOfUniqueIDs);
        }
    }

    private void lazy3(int ourUniqueID, int otherUniqueID, String lr, String value, TreeSet<String> setOfUniqueIDs) {
        int uniqueID = ProjectsCommonSubs.getIntFromStringNoThrow(value, -1);   // Technically should NEVER throw or return default, but for safety.  Default will NEVER be found!
        if (ourUniqueID == uniqueID) {
            setOfUniqueIDs.add(_mCTCSerialData.getMyShortStringNoCommaViaUniqueID(otherUniqueID) + lr + ", ");
        }
    }

    private String getListOfSwitchSlavedToOSSectionsReferenced( CodeButtonHandlerData currentCodeButtonHandlerData,
                                                                ArrayList <CodeButtonHandlerData> codeButtonHandlerDataArrayList) {
        StringBuffer returnStringBuffer = new StringBuffer("");
        TreeSet<String> temp = new TreeSet<>();
        int currentUniqueID = currentCodeButtonHandlerData._mUniqueID;
        for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataArrayList) {
            if (currentCodeButtonHandlerData != codeButtonHandlerData) { // Don't check ourselves
                if (codeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID != CodeButtonHandlerData.SWITCH_NOT_SLAVED)  { // It's referencing someone else:
                    if (currentUniqueID == codeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID) {
                        temp.add(_mCTCSerialData.getMyShortStringNoCommaViaUniqueID(codeButtonHandlerData._mUniqueID) + ", ");
                    }
                }
            }
        }
        for (String result : temp)  returnStringBuffer.append(result);
        if (returnStringBuffer.length() > 0) {
            return "Sw: " + returnStringBuffer.substring(0, returnStringBuffer.length() - 2);   //NOI18N
        } else {
            return "";
        }
//      if (returnStringBuffer.length() > 0)  returnStringBuffer.append("Sw: " + returnStringBuffer.substring(0, returnStringBuffer.length() - 2));   //NOI18N
//      return returnStringBuffer.toString();
    }

//  Anything in error, return ERROR_STRING
    private static String generatePossibleErrorString(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData currentCodeButtonHandlerData) {
        if (!FrmCB.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmSIDI.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmSIDL.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmSWDI.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmSWDL.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmCO.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmTRL.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmTUL.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        if (!FrmIL.dialogCodeButtonHandlerDataValid(checkJMRIObject, currentCodeButtonHandlerData)) return ERROR_STRING;
        return "";                              // No error (string)
    }

    private String constructSingleColumnDisplayLine(CodeButtonHandlerData codeButtonHandlerData) {
        String referencesString1 = getListOfTrafficLockingRulesOSSectionsReferenced(codeButtonHandlerData, _mCTCSerialData.getCodeButtonHandlerDataArrayList());
        String referencesString2 = getListOfSwitchSlavedToOSSectionsReferenced(codeButtonHandlerData, _mCTCSerialData.getCodeButtonHandlerDataArrayList());
        String displayString = codeButtonHandlerData.myString();
        if (!referencesString1.isEmpty() || !referencesString2.isEmpty()) {
            displayString += REFERENCES_PRESENT_INDICATOR + referencesString1 + " " + referencesString2 + ")";
        }
        displayString += generatePossibleErrorString(_mCheckJMRIObject, codeButtonHandlerData);
        return displayString;
    }
}
