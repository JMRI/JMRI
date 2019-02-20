package jmri.jmrit.ctc.editor.code;

import static jmri.jmrit.ctc.editor.code.CreateXMLFiles.generateEpilogue;
import static jmri.jmrit.ctc.editor.code.CreateXMLFiles.generateProlog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CreateInternalSensorsXMLFile {
    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not write anything if it fails.")
    public static void writeInternalSensors(String directoryToCreateThemIn, OtherData otherData, ArrayList <CodeButtonHandlerData> codeButtonHandlerDataArrayList) {
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new FileWriter(directoryToCreateThemIn + "InternalSensors.xml")); // NOI18N
        } catch (IOException e) { return; }
        generateProlog(printWriter);
        printWriter.println("  <sensors class=\"jmri.jmrix.internal.configurexml.InternalSensorManagerXml\">"); // NOI18N
        printWriter.println("    <defaultInitialState>unknown</defaultInitialState>");  // NOI18N
//  Create all internal sensors:
        ArrayList<Field> internalSensorStringFields = OtherData.getAllInternalSensorStringFields();
        for (Field field : internalSensorStringFields) {
            try {
                String content = (String)field.get(otherData);
                if (!ProjectsCommonSubs.isNullOrEmptyString(content)) writeInternalSensor(content, printWriter);
            } catch (IllegalAccessException e) {} // Should never happen, print nothing for this entry if so.
        }
        
        internalSensorStringFields = CodeButtonHandlerData.getAllInternalSensorStringFields();
        for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataArrayList) {
            for (Field field : internalSensorStringFields) {
                try {
                    String content = (String)field.get(codeButtonHandlerData);
                    if (!ProjectsCommonSubs.isNullOrEmptyString(content)) writeInternalSensor(content, printWriter);
                } catch (IllegalAccessException e) {} // Should never happen, print nothing for this entry if so.
            }
        }
        printWriter.println("  </sensors>");    // NOI18N
        generateEpilogue(printWriter);
        printWriter.close();
    }
    
/*  As of 4.13.4ish:
    <sensor inverted="false">
      <systemName>ISIS:BLAHBLAH</systemName>
    </sensor>
*/    
    private static void writeInternalSensor(String value, PrintWriter printWriter) {
        value = ProjectsCommonSubs.getSafeTrimmedString(value);
        printWriter.println("    <sensor inverted=\"false\">");                 // NOI18N
        printWriter.println("      <systemName>" + value + "</systemName>");    // NOI18N
        printWriter.println("    </sensor>");                                   // NOI18N
    }
}
