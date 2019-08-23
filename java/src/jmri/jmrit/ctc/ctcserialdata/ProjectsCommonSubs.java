package jmri.jmrit.ctc.ctcserialdata;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.ButtonGroup;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */

public class ProjectsCommonSubs {
    static final public String CSV_SEPARATOR = ","; // NOI18N
    static final public String SSV_SEPARATOR = ";"; // NOI18N
    
    static public ArrayList<String> getArrayListFromCSV(String csvString) { return helper1(csvString, CSV_SEPARATOR);}
    static public ArrayList<String> getArrayListFromSSV(String ssvString) { return helper1(ssvString, SSV_SEPARATOR); }
//  IMHO "split" should return an array size of 0 if passed "".  One can argue that.  Here I compensate for that situation:
    static private ArrayList<String> helper1(String ssvString, String separator) {
        if (ssvString.isEmpty()) return new ArrayList<>();  // Return list with 0 elements as it should be!
        return new ArrayList<String>(Arrays.asList(ssvString.split(separator)));
    }

//  Returns an ArrayList guaranteed to have exactly "returnArrayListSize" entries,
//  and if the passed "csvString" has too few entries, then those missing end values are set to "":
    static public ArrayList<String> getFixedArrayListSizeFromCSV(String csvString, int returnArrayListSize) {
        ArrayList<String> returnArray = getArrayListFromCSV(csvString);
        while (returnArray.size() < returnArrayListSize) returnArray.add("");
        return returnArray;
    }
    
    static public int getIntFromStringNoThrow(String aString, int defaultValueIfProblem) {
        int returnValue = defaultValueIfProblem;    // Default if error
        try { returnValue = Integer.parseInt(aString); } catch (NumberFormatException e) {}
        return returnValue;
    }

    static public String constructCSVStringFromArrayList(ArrayList<String> stringArrayList) { return constructSeparatorStringFromArray(stringArrayList, CSV_SEPARATOR); }
    static public String constructSSVStringFromArrayList(ArrayList<String> stringArrayList) { return constructSeparatorStringFromArray(stringArrayList, ProjectsCommonSubs.SSV_SEPARATOR); }
    @SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION", justification = "I don't want to introduce bugs, CPU no big deal here.")
    static private String constructSeparatorStringFromArray(ArrayList<String> stringArrayList, String separator) {
        String returnString = "";
        if (stringArrayList.size() > 0) { // Safety:
            returnString = stringArrayList.get(0);
            if (returnString == null) returnString = "";        // Safety
            for (int index = 1; index < stringArrayList.size(); index++) {
                String gottenString = stringArrayList.get(index);
                if (gottenString == null) gottenString = "";    // Safety
                returnString += separator + gottenString;
            }
        }
        return returnString;
    }
    
    public static String removeFileExtension(String filename) {
        final int lastIndexOf = filename.lastIndexOf('.');
        return lastIndexOf >= 1 ? filename.substring(0, lastIndexOf) : filename;  
    }

//  Regarding "SuppressFBWarn":    
//  Nothing I can find says it returns "null" in any of these lines.
//  So either Java's documentation is wrong, or SpotBugs is wrong.  I'll let
//  someone in the future deal with this, since it should never happen:    
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Nothing is documented as returning null")
    public static String getFilenameOnly(String path) {
        return Paths.get(path).getFileName().toString(); 
    }

    public static String addExtensionIfMissing(String path, String missingExtension) {
        String filenameOnly = getFilenameOnly(path);
        if (filenameOnly.indexOf('.') >= 0) return path;
        return path + missingExtension;
    }

    public static String changeExtensionTo(String path, String newExtension) {
        return addExtensionIfMissing(removeFileExtension(path), newExtension);
    }    
    
    public static boolean isNullOrEmptyString(String aString) {
        return aString == null || aString.trim().length() == 0;
    }
    
    public static String getSafeTrimmedString(String aString) {
        if (aString == null) return "";
        return aString.trim();
    }
    
//  If you used "CommonSubs.numberButtonGroup" above to setup the button group, then
//  you can call this routine to get the switch value as an int value,
//  since exception "NumberFormatException" should NEVER be thrown!
//  If it does throw because you screwed something up, it will return -1.
    public static int getButtonSelectedInt(ButtonGroup buttonGroup) {
        try { return Integer.parseInt(getButtonSelectedString(buttonGroup)); } catch (NumberFormatException e) { return -1; }
    }
    public static String getButtonSelectedString(ButtonGroup buttonGroup) {
        return buttonGroup.getSelection().getActionCommand();
    }
    
    public static ArrayList<Field> getAllPartialVariableNameStringFields(String partialVariableName, Field[] fields) {
        ArrayList <Field> stringFields = new ArrayList<>();
        for (Field field : fields) {
            if (field.getType() == String.class) {
                if (field.getName().indexOf(partialVariableName) != -1) {
                    stringFields.add(field);
                }
            }
        }
        return stringFields;
    }
}
