package jmri.jmrit.ctc.editor.code;

// https://stackoverflow.com/questions/5091057/how-to-find-a-whole-word-in-a-string-in-java

import java.lang.reflect.Field;
import java.util.ArrayList;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * This object uses reflection to determine all of the fields
 * that have possible references that need to be changed.
 * Anything that is a String in "getCodeButtonHandlerDataArrayList" is searched.
 */
public class FindAndReplace {
    public static class SearchResults {
        public final String _mUserSwitchSignalEtcEntry;   // These 3 variables get displayed to the user in "some" form (JTable for now):
        public final String _mUserFieldName;
        public final String _mUserContent;
        public final int    _mUserTableLine;                // Line displaying this data.
        public final int _mIndexIntoCodeButtonHandlerDataList;  // SUBSCRIPT (not UniqueID) into "codeButtonHandlerDataList"
        public final Field _mField;         // Field within CodeButtonHandlerData where it occurs.
        public final int _mIndexIntoField;  // IndexOf (if search is contains) into field where match starts, else 0 if search is exact.
        public SearchResults (String userSwitchSignalEtcEntry, String userFieldName, String userContent, int userTableLine, int indexIntoCodeButtonHandlerDataList, Field field) { // Used by exact.
            _mUserSwitchSignalEtcEntry = userSwitchSignalEtcEntry;
            _mUserFieldName = userFieldName;
            _mUserContent = userContent;
            _mUserTableLine = userTableLine;
            _mIndexIntoCodeButtonHandlerDataList = indexIntoCodeButtonHandlerDataList;
            _mField = field;
            _mIndexIntoField = 0;
        }
        public SearchResults (String userSwitchSignalEtcEntry, String userFieldName, String userContent, int userTableLine, int indexIntoCodeButtonHandlerDataList, Field field, int indexIntoField) { // Used by contains
            _mUserSwitchSignalEtcEntry = userSwitchSignalEtcEntry;
            _mUserFieldName = userFieldName;
            _mUserContent = userContent;
            _mUserTableLine = userTableLine;
            _mIndexIntoCodeButtonHandlerDataList = indexIntoCodeButtonHandlerDataList;
            _mField = field;
            _mIndexIntoField = indexIntoField;
        }
    }
    
    public static ArrayList<SearchResults> doSearch(CTCSerialData ctcSerialData, String stringToSearchFor, boolean caseSensitive, boolean exactMatch) {
        String actualStringToSearchFor = caseSensitive ? stringToSearchFor : stringToSearchFor.toUpperCase();
        ArrayList<SearchResults> results = new ArrayList<>();
        ArrayList <CodeButtonHandlerData> codeButtonHandlerDataList = ctcSerialData.getCodeButtonHandlerDataArrayList();
        ArrayList <Field> stringFieldsIWantList = CodeButtonHandlerData.getAllStringFields();
        
        for (int index = 0; index < codeButtonHandlerDataList.size(); index++) {
            CodeButtonHandlerData codeButtonHandlerData = codeButtonHandlerDataList.get(index);
            for (Field field : stringFieldsIWantList) {
                try {
                    String unmodifiedContent = (String)field.get(codeButtonHandlerData);
                    String content = caseSensitive ? unmodifiedContent : unmodifiedContent.toUpperCase();
                    if (exactMatch) {
                        if (content.equals(actualStringToSearchFor)) {
                            results.add(new SearchResults(codeButtonHandlerData.myShortStringNoComma(), field.getName(), unmodifiedContent, results.size(), index, field));
                        }
                    } else {
                        int indexIntoField = content.indexOf(actualStringToSearchFor);
                        if (-1 != indexIntoField) { // Found:
                            results.add(new SearchResults(codeButtonHandlerData.myShortStringNoComma(), field.getName(), unmodifiedContent, results.size(), index, field, indexIntoField));
                        }
                    }
                } catch (IllegalAccessException e) {} // Any errors, just skip this entry (shouldn't get this though!)
            }
        }
        return results;
    }
}
