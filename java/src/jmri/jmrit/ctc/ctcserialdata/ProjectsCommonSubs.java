package jmri.jmrit.ctc.ctcserialdata;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import jmri.*;
import jmri.jmrit.ctc.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */

public class ProjectsCommonSubs {
    static final public String SSV_SEPARATOR = ";";

    static public ArrayList<String> getArrayListFromCSV(String csvString) { return helper1(csvString, CSVFormat.DEFAULT.getDelimiterString());}
    static public ArrayList<String> getArrayListFromSSV(String ssvString) { return helper1(ssvString, SSV_SEPARATOR); }
    static private ArrayList<String> helper1(String ssvString, String separator) {
        ArrayList<String> list = new ArrayList<>();
        try (CSVParser parser = new CSVParser(new StringReader(ssvString),
                CSVFormat.Builder.create(CSVFormat.DEFAULT)
                        .setQuote(null).setDelimiter(separator).setRecordSeparator(null).build())) {
            parser.getRecords().forEach(record -> record.forEach(item -> list.add(item)));
        } catch (IOException ex) {
            log.error("Unable to parse {}", ssvString, ex);
        }
        return list;
    }

    static public ArrayList<String> getArrayListOfSignalNames(ArrayList<NBHSignal> array) {
        ArrayList<String> stringList = new ArrayList<>();
        array.forEach(row -> {
            var handle = (NamedBeanHandle<?>) row.getBeanHandle();
            stringList.add(handle.getName());
        });
        return stringList;
    }

    static public ArrayList<NBHSignal> getArrayListOfSignals(ArrayList<String> signalNames) {
        CtcManager cm = InstanceManager.getDefault(CtcManager.class);
        ArrayList<NBHSignal> newList = new ArrayList<>();
        signalNames.forEach(name -> {
            NBHSignal newSignal = cm.getNBHSignal(name);
            if (newSignal == null) {
                newSignal = new NBHSignal(name);
            }
            if (newSignal.valid()) {
                newList.add(newSignal);
            }
        });
        return newList;
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

    public static boolean isNullOrEmptyString(String aString) {
        return aString == null || aString.trim().length() == 0;
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectsCommonSubs.class);
}
