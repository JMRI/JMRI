package jmri.jmrit.ctc.ctcserialdata;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.ButtonGroup;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */

public class ProjectsCommonSubs {
    static final public char SSV_SEPARATOR = ';';
    static final public char CSV_SEPARATOR = ',';   // What "CSVFormat.DEFAULT.getDelimiter()" would return.
    
    static public ArrayList<String> getArrayListFromCSV(String csvString, boolean convert) {
        ArrayList<String> result = helper1(csvString, CSV_SEPARATOR);
        if (convert) {
            for (final ListIterator<String> iterator = result.listIterator(); iterator.hasNext();) {
                final String element = iterator.next();
                iterator.set(convertNonEscapeToEscape(element));
            }
        }
        return result;
    }
    static public ArrayList<String> getArrayListFromSSV(String ssvString) { return helper1(ssvString, SSV_SEPARATOR); }
    static private ArrayList<String> helper1(String ssvString, char separator) {
        ArrayList<String> list = new ArrayList<>();
        try (CSVParser parser = new CSVParser(new StringReader(ssvString), CSVFormat.DEFAULT.withQuote(null).withDelimiter(separator).withRecordSeparator(null))) {
            parser.getRecords().forEach(record -> record.forEach(item -> list.add(item)));
        } catch (IOException ex) {
            log.error("Unable to parse {}", ssvString, ex);
        }
        return list;
    }
    
    static final public String ESCAPE_CHARACTER = "~";
    static final public String ESCAPED_ESCAPE_STRING = "~~";
    static final public String CSV_SEPARATOR_STRING = ",";
    static final public String ESCAPED_CSV_STRING = "~0";
    static final public String SSV_SEPARATOR_STRING = ";";
    static final public String ESCAPED_SSV_STRING = "~1";
    static public String convertNonEscapeToEscape(String nonEscapeString) {
        String tempString = nonEscapeString.replace(ESCAPE_CHARACTER, ESCAPED_ESCAPE_STRING);
        tempString = tempString.replace(CSV_SEPARATOR_STRING, ESCAPED_CSV_STRING);
        return tempString.replace(SSV_SEPARATOR_STRING, ESCAPED_SSV_STRING);
    }
    static public String convertEscapeToNonEscape(String escapedString) {
        String tempString = escapedString.replace(ESCAPED_ESCAPE_STRING, ESCAPE_CHARACTER);
        tempString = tempString.replace(ESCAPED_CSV_STRING, CSV_SEPARATOR_STRING);
        return tempString.replace(ESCAPED_SSV_STRING, SSV_SEPARATOR_STRING);
    }

//  Returns an ArrayList guaranteed to have A MINIMUM of "returnArrayListSize" entries (too many is OK!),
//  and if the passed "csvString" has too few entries, then those missing end values are set to "":
    static public ArrayList<String> getFixedArrayListSizeFromCSV(String csvString, int returnArrayListSize, boolean convert) {
        ArrayList<String> returnArray = getArrayListFromCSV(csvString, convert);
        while (returnArray.size() < returnArrayListSize) returnArray.add("");
        return returnArray;
    }
    
    static public int getIntFromStringNoThrow(String aString, int defaultValueIfProblem) {
        int returnValue = defaultValueIfProblem;    // Default if error
        try { returnValue = Integer.parseInt(aString); } catch (NumberFormatException e) {}
        return returnValue;
    }

    static public String constructCSVStringFromArrayList(ArrayList<String> stringArrayList, boolean convert) { 
        if (convert) {
            String result = constructSeparatorStringFromArray(stringArrayList, ProjectsCommonSubs.CSV_SEPARATOR);
            return convertEscapeToNonEscape(result);
        } else {
            return constructSeparatorStringFromArray(stringArrayList, ProjectsCommonSubs.CSV_SEPARATOR);
        }
    }
    static public String constructSSVStringFromArrayList(ArrayList<String> stringArrayList) { return constructSeparatorStringFromArray(stringArrayList, ProjectsCommonSubs.SSV_SEPARATOR); }
    static private String constructSeparatorStringFromArray(ArrayList<String> list, char separator) {
        try (CSVPrinter printer = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT.withQuote(null).withDelimiter(separator).withRecordSeparator(null))) {
            printer.printRecord(list);
            return printer.getOut().toString();
        } catch (IOException ex) {
            log.error("Unable to create list", ex); 
            return "";
        }
    }
    
    public static String removeFileExtension(String filename) {
        final int lastIndexOf = filename.lastIndexOf('.');
        return lastIndexOf >= 1 ? filename.substring(0, lastIndexOf) : filename;  
    }

    public static String getFilenameOnly(String path) {
        // Paths.get(path) can return null per the Paths documentation
        Path file = Paths.get(path);
        if (file != null){
            Object fileName = file.getFileName();
            if (fileName!=null) {
                return fileName.toString();
            }
        }
        return "";
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
                if (field.getName().contains(partialVariableName)) {
                    stringFields.add(field);
                }
            }
        }
        return stringFields;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectsCommonSubs.class);
}
