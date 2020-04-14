package jmri.jmrit.ctc.ctcserialdata;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import jmri.jmrit.ctc.CTCFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CTCSerialData {

    public final static String CTCVersion = "V1.02";
    private OtherData _mOtherData;
    private ArrayList<CodeButtonHandlerData> _mCodeButtonHandlerDataArrayList;
    private final static Logger log = LoggerFactory.getLogger(CTCSerialData.class);

    public CTCSerialData() {
        _mOtherData = new OtherData();
        _mCodeButtonHandlerDataArrayList = new ArrayList<>();
    }

    public OtherData getOtherData() {
        return _mOtherData;
    }

    public int getUniqueNumber() {
        return _mOtherData.getNextUniqueNumber();
    }

    public CodeButtonHandlerData getCodeButtonHandlerDataViaUniqueID(int uniqueID) {
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            if (codeButtonHandlerData._mUniqueID == uniqueID) {
                return codeButtonHandlerData;
            }
        }
        return null;    // In case it's not found.
    }

    public String getMyShortStringNoCommaViaUniqueID(int uniqueID) {
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            if (codeButtonHandlerData._mUniqueID == uniqueID) {
                return codeButtonHandlerData.myShortStringNoComma();
            }
        }
        return "UNKNOWN";    // In case it's not found.
    }

    public int getIndexOfUniqueID(int uniqueID) {
        for (int index = 0; index < _mCodeButtonHandlerDataArrayList.size(); index++) {
            if (_mCodeButtonHandlerDataArrayList.get(index)._mUniqueID == uniqueID) {
                return index;    // That's it.
            }
        }
        return -1;  // not found.
    }

    public void setOtherData(OtherData otherData) {
        _mOtherData = otherData;
    }

    public ArrayList<CodeButtonHandlerData> getCodeButtonHandlerDataArrayList() {
        return _mCodeButtonHandlerDataArrayList;
    }

    public void addCodeButtonHandlerData(CodeButtonHandlerData codeButtonHandlerData) {
        _mCodeButtonHandlerDataArrayList.add(codeButtonHandlerData);
    }

    public void removeCodeButtonHandlerData(int index) {
        _mCodeButtonHandlerDataArrayList.remove(index);
    }

    public CodeButtonHandlerData getCodeButtonHandlerData(int index) {
        return _mCodeButtonHandlerDataArrayList.get(index);
    }

    public int getCodeButtonHandlerDataSize() {
        return _mCodeButtonHandlerDataArrayList.size();
    }

    public void moveUp(int index) {
        try {
            Collections.swap(_mCodeButtonHandlerDataArrayList, index, index - 1);
        } catch (IndexOutOfBoundsException e) {
        }    // Do NOTHING in this case!  Technically should never happen, since buttons aren't enabled for such possibilities
    }

    public void moveDown(int index) {
        try {
            Collections.swap(_mCodeButtonHandlerDataArrayList, index, index + 1);
        } catch (IndexOutOfBoundsException e) {
        }    // Do NOTHING in this case!  Technically should never happen, since buttons aren't enabled for such possibilities
    }

//  In addition, variable "_mTRL_LeftTrafficLockingRulesSSVList" and "_mTRL_RightTrafficLockingRulesSSVList"
//  have buried within them text strings for user viewing of the "CodeButtonHandlerData.myShortStringNoComma()"
//  value.  That occurs at TRL_USERTEXT_TERMINATING_INDEX in all possible records, with TRL_UNIQUEID_TERMINATING_INDEX containing the
//  textual representation of the UniqueID that we should look up and fix with.
    public void updateSwitchAndSignalEtcNumbersEverywhere(int index, int newSwitchNumber, int newSignalEtcNumber, int newGUIColumnNumber, boolean newGUIGeneratedAtLeastOnceAlready) {
        CodeButtonHandlerData codeButtonHandlerData = _mCodeButtonHandlerDataArrayList.get(index);
        codeButtonHandlerData._mSwitchNumber = newSwitchNumber;
        codeButtonHandlerData._mSignalEtcNumber = newSignalEtcNumber;
        codeButtonHandlerData._mGUIColumnNumber = newGUIColumnNumber;
        codeButtonHandlerData._mGUIGeneratedAtLeastOnceAlready = newGUIGeneratedAtLeastOnceAlready;
        int UniqueIDBeingModified = codeButtonHandlerData._mUniqueID;
        String replacementString = codeButtonHandlerData.myShortStringNoComma();
        for (CodeButtonHandlerData temp : _mCodeButtonHandlerDataArrayList) {
            if (temp != codeButtonHandlerData) { // Not us, check:
                temp._mTRL_LeftTrafficLockingRulesSSVList = commonCode1(temp._mTRL_LeftTrafficLockingRulesSSVList, UniqueIDBeingModified, replacementString);
                temp._mTRL_RightTrafficLockingRulesSSVList = commonCode1(temp._mTRL_RightTrafficLockingRulesSSVList, UniqueIDBeingModified, replacementString);
            }
        }
    }

    private String commonCode1(String stringToFix, int uniqueIDBeingModified, String replacementString) {
        ArrayList<String> ssvArrayList = ProjectsCommonSubs.getArrayListFromSSV(stringToFix);
        boolean anyModified = false;
        String returnString;
        for (int ssvArrayIndex = 0; ssvArrayIndex < ssvArrayList.size(); ssvArrayIndex++) {
            TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(ssvArrayList.get(ssvArrayIndex));
            boolean loopAnyModified = false;
            if ((returnString = lazy1(uniqueIDBeingModified, replacementString, trafficLockingEntry._mUniqueID1)) != null) {
                loopAnyModified = true;
                trafficLockingEntry._mUniqueID1 = returnString;
            }
            if ((returnString = lazy1(uniqueIDBeingModified, replacementString, trafficLockingEntry._mUniqueID2)) != null) {
                loopAnyModified = true;
                trafficLockingEntry._mUniqueID2 = returnString;
            }
            if ((returnString = lazy1(uniqueIDBeingModified, replacementString, trafficLockingEntry._mUniqueID3)) != null) {
                loopAnyModified = true;
                trafficLockingEntry._mUniqueID3 = returnString;
            }
            if ((returnString = lazy1(uniqueIDBeingModified, replacementString, trafficLockingEntry._mUniqueID4)) != null) {
                loopAnyModified = true;
                trafficLockingEntry._mUniqueID4 = returnString;
            }
            if ((returnString = lazy1(uniqueIDBeingModified, replacementString, trafficLockingEntry._mUniqueID5)) != null) {
                loopAnyModified = true;
                trafficLockingEntry._mUniqueID5 = returnString;
            }
            anyModified |= loopAnyModified;
            if (loopAnyModified) {
                ssvArrayList.set(ssvArrayIndex, trafficLockingEntry.toCSVString());
            }
        }
        return anyModified ? ProjectsCommonSubs.constructSSVStringFromArrayList(ssvArrayList) : stringToFix;
    }

    private String lazy1(int uniqueIDBeingModified, String replacementString, String stringValueToCheck) {
        int uniqueID = ProjectsCommonSubs.getIntFromStringNoThrow(stringValueToCheck, -1);
        if (uniqueID == uniqueIDBeingModified) { // Fix it:
            return replacementString;
        }
        return null;
    }

    public void setCodeButtonHandlerData(int index, CodeButtonHandlerData codeButtonHandlerData) {
        _mCodeButtonHandlerDataArrayList.set(index, codeButtonHandlerData);
    }

//  If none are found, return -1 (which we'll then increment by 2 to 1, which is a good "add" default starting point):
    public int findHighestSwitchNumberUsedSoFar() {
        int highestSwitchNumber = -1;
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            if (codeButtonHandlerData._mSwitchNumber > highestSwitchNumber) {
                highestSwitchNumber = codeButtonHandlerData._mSwitchNumber;
            }
        }
        return highestSwitchNumber;
    }

//  If none are found, return 0 (which we'll then increment by 1 to 1, which is a good "add" default starting point):
    public int findHighestColumnNumberUsedSoFar() {
        int highestColumnNumber = 0;
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            if (codeButtonHandlerData._mGUIColumnNumber > highestColumnNumber) {
                highestColumnNumber = codeButtonHandlerData._mGUIColumnNumber;
            }
        }
        return highestColumnNumber;
    }

//  Duplicates get ONLY ONE entry in the set (obviously).
    public HashSet<String> getAllInternalSensors() {
        HashSet<String> returnValue = _mOtherData.getAllInternalSensors();
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            returnValue.addAll(codeButtonHandlerData.getAllInternalSensors());
        }
        return returnValue;
    }

    @SuppressWarnings("unchecked") // See below comments:
    public boolean readDataFromXMLFile(String filename) {
        CodeButtonHandlerData.preprocessingUpgradeSelf(filename);
        boolean returnValue = false;    // Assume error
        try {
            try (XMLDecoder xmlDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(filename)))) {
                _mOtherData = (OtherData) xmlDecoder.readObject();
                // triggers unchecked warning
                _mCodeButtonHandlerDataArrayList = (ArrayList<CodeButtonHandlerData>) xmlDecoder.readObject();	// Type safety: Unchecked cast from Object to ArrayList<>
            }
            returnValue = true;
        } catch (IOException e) {
            log.debug("Unable to read {}", filename, e); // debug because missing file is not error
        }
// Safety:
        if (_mOtherData == null) {
            _mOtherData = new OtherData();
        }
        if (_mCodeButtonHandlerDataArrayList == null) {
            _mCodeButtonHandlerDataArrayList = new ArrayList<>();
        }

//  Make all strings "sane" on the way in, in case user used a standard editor to modify our file:
//  This works for both the CTCEditor and JMRI runtime which both call this routine:
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            codeButtonHandlerData.trimAndFixAllStrings();
        }
//  Kludge for new field added but I forgot to init it in "CodeButtonHandlerDataRoutines" (an enum is NOT an integer like other languages, but an object!):
//  Can be removed someday!
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            if (codeButtonHandlerData._mSWDI_GUITurnoutType == null) {
                codeButtonHandlerData._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.TURNOUT;
            }
        }
//  Finally, give each object a chance to upgrade itself BEFORE anything uses it:
        _mOtherData.upgradeSelf();
        for (CodeButtonHandlerData codeButtonHandlerData : _mCodeButtonHandlerDataArrayList) {
            codeButtonHandlerData.upgradeSelf();
        }
        return returnValue;
    }

    /*  This routine properly supports other programs that may poll for this shared
    files existance and attempt to read it at the same time.
Problem:
    If this routine was writing this file while another program was polling for
    it's change and then attempted to immediately read it, that reading program
    could randomly at times get some form of I/O error.
Solution:
    1.) Write the xml file to a temporary filename.
    2.) Delete the existing file.
    3.) Rename the temporary to the proper (formerly existing) filename.

    In this way the file is totally stable when it finally appears in the directory
    with its proper filename that the other program is looking for.
     */
    private static final String TEMPORARY_EXTENSION = ".xmlTMP";        // NOI18N

    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "Any problems, I don't care, it's too late by this point")
    public void writeDataToXMLFile(String filename) {
        String temporaryFilename = ProjectsCommonSubs.changeExtensionTo(filename, TEMPORARY_EXTENSION);
        try { // Write temporary file:
            try (XMLEncoder xmlEncoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(temporaryFilename)))) {
                xmlEncoder.writeObject(_mOtherData);
                xmlEncoder.writeObject(_mCodeButtonHandlerDataArrayList);
            }
            CTCFiles.rotate(filename, false);
            File outputFile = new File(filename);

            outputFile.delete();    // Delete existing old file.
            (new File(temporaryFilename)).renameTo(outputFile);     // Rename temporary filename to proper final file.
        } catch (IOException e) {
        }
    }
}
